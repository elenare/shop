/*
 * Copyright (C) 2013 Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.shop.kundenverwaltung.business;

import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellposition_;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Bestellung_;
import de.shop.bestellverwaltung.domain.Warenkorbposition;
import de.shop.iam.business.IdentityAccessManagement;
import de.shop.iam.domain.IdentityVO;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.AbstractKunde_;
import de.shop.kundenverwaltung.domain.GeschlechtType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.kundenverwaltung.domain.Privatkunde_;
import de.shop.util.NoMimeTypeException;
import de.shop.util.Strings;
import de.shop.util.interceptor.Log;
import de.shop.util.persistence.ConcurrentDeletedException;
import de.shop.util.persistence.DefaultEM;
import de.shop.util.persistence.File;
import de.shop.util.persistence.FileHelper;
import de.shop.util.persistence.MimeType;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.MIT_BESTELLUNGEN;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.NUR_KUNDE;
import static de.shop.kundenverwaltung.domain.AbstractKunde.GRAPH_BESTELLUNGEN;
import static de.shop.kundenverwaltung.domain.AbstractKunde.GRAPH_REKLAMATIONEN;
import static de.shop.util.Constants.LOADGRAPH;
import static de.shop.util.Constants.MAX_AUTOCOMPLETE;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.toList;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Dependent
@Log
public class KundenBroker {
    public enum FetchType {
        NUR_KUNDE,
        MIT_BESTELLUNGEN,
        MIT_ROLES,
        MIT_REKLAMATIONEN
    }
    
    public enum OrderByType {
        UNORDERED,
        ID
    }
    
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private final EntityManager em;
    private final Instance<IdentityAccessManagement> iamInstance;
    private final Instance<FileHelper> fileHelperInstance;
    private final Instance<ManagedExecutorService> managedExecutorServiceInstance;
    
    @Inject
    KundenBroker(@DefaultEM EntityManager em,
                 Instance<IdentityAccessManagement> iamInstance,
                 Instance<FileHelper> fileHelperInstance,
                 Instance<ManagedExecutorService> managedExecutorServiceInstance) {
        super();
        this.em = em;
        this.iamInstance = iamInstance;
        this.fileHelperInstance = fileHelperInstance;
        this.managedExecutorServiceInstance = managedExecutorServiceInstance;
    }
    
    /**
     * Suche nach einem Kunden anhand der ID
     * @param id ID des gesuchten Kunden
     * @param fetch Angabe, welche Objekte mitgeladen werden sollen
     * @return Der gesuchte Kunde oder NotFoundException
     */
    public Optional<AbstractKunde> findById(long id, FetchType fetch) {
        AbstractKunde kunde;
        EntityGraph<?> entityGraph;
        switch (fetch) {
            case NUR_KUNDE:
                kunde = em.find(AbstractKunde.class, id);
                break;
            
            case MIT_BESTELLUNGEN:
                entityGraph = em.getEntityGraph(GRAPH_BESTELLUNGEN);
                kunde = em.find(AbstractKunde.class, id, Collections.singletonMap(LOADGRAPH, entityGraph));
                break;
                
            case MIT_REKLAMATIONEN:
                entityGraph = em.getEntityGraph(GRAPH_REKLAMATIONEN);
                kunde = em.find(AbstractKunde.class, id, Collections.singletonMap(LOADGRAPH, entityGraph));
                break;

            default:
                kunde = em.find(AbstractKunde.class, id);
                break;
        }
        
        return ofNullable(kunde);
    }
    
    /**
     * Potenzielle IDs zu einem gegebenen ID-Praefix suchen. Fuer Autovervollstaendigen: deshalb leere Liste, falls nichts gefunden wurde.
     * @param idPrefix der Praefix zu potenziellen IDs als String
     * @return Liste der passenden Praefixe
     */
    public List<Long> findIdsByPrefix(String idPrefix) {
        if (Strings.isNullOrEmpty(idPrefix)) {
            return Collections.emptyList();
        }
        return em.createNamedQuery(AbstractKunde.IDS_BY_PREFIX, Long.class)
                 .setParameter(AbstractKunde.PARAM_KUNDE_ID_PREFIX, idPrefix + '%')
                 .getResultList();
    }
    
    /**
     * Kunden suchen, deren ID den gleiche Praefix hat.
     * @param id Praefix der ID
     * @return Liste mit Kunden mit passender ID
     */
    public List<AbstractKunde> findByIdPrefix(long id) {
        return em.createNamedQuery(AbstractKunde.BY_ID_PREFIX, AbstractKunde.class)
                 .setParameter(AbstractKunde.PARAM_KUNDE_ID_PREFIX, String.valueOf(id) + '%')
                 .setMaxResults(MAX_AUTOCOMPLETE)
                 .getResultList();
    }
    
    /**
     * Den Kunden zum eingeloggten User suchen.
     * @param fetch Angabe, welche referenzierten Objekte mitgeladen werden sollen
     * @return Der gefundene Kunde
     */
    public Optional<AbstractKunde> findByLoginname(FetchType fetch) {
        final Optional<String> loginname = iamInstance.get().getLoginname();
        return loginname.isPresent() ? findByLoginname(loginname.get(), fetch) : empty();
    }
    
    /**
     * Den Kunden zu einem Loginnamen suchen.
     * @param loginname Der Loginname, zu dem der passende Kunde gesucht wird.
     * @param fetch Angabe, welche referenzierten Objekte mitgeladen werden sollen
     * @return Der gefundene Kunde oder NotFoundException
     */
    public Optional<AbstractKunde> findByLoginname(String loginname, FetchType fetch) {
        final TypedQuery<AbstractKunde> query = em.createNamedQuery(AbstractKunde.BY_LOGINNAME,
                                                                    AbstractKunde.class)
                                                  .setParameter(AbstractKunde.PARAM_LOGINNAME, loginname);

        setEntityGraph(query, fetch);
        AbstractKunde kunde;
        try {
            kunde = query.getSingleResult();
        } catch (NoResultException e) {
            if (LOGGER.isLoggable(FINER)) {
                LOGGER.log(FINER, e.getMessage(), e);
            }
            return empty();
        }
        
        return of(kunde);
    }
    
    private void setEntityGraph(TypedQuery<AbstractKunde> query, FetchType fetch) {
        EntityGraph<?> entityGraph;
        switch (fetch) {
            case NUR_KUNDE:
                break;
                
            case MIT_BESTELLUNGEN:
                entityGraph = em.getEntityGraph(GRAPH_BESTELLUNGEN);
                query.setHint(LOADGRAPH, entityGraph);
                break;
                
            case MIT_REKLAMATIONEN:
                entityGraph = em.getEntityGraph(GRAPH_REKLAMATIONEN);
                query.setHint(LOADGRAPH, entityGraph);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Den Kunden zu einer gegebenen Bestellung-ID suchen.
     * @param bestellungId Bestellung-ID
     * @return Der gefundene Kunde
     */
    public Optional<AbstractKunde> findByBestellungId(long bestellungId) {
        AbstractKunde kunde;
        try {
            kunde = em.createNamedQuery(AbstractKunde.BY_BESTELLUNG_ID, AbstractKunde.class)
                      .setParameter(AbstractKunde.PARAM_BESTELLUNG_ID, bestellungId)
                      .getSingleResult();
        } catch (NoResultException e) {
            if (LOGGER.isLoggable(FINER)) {
                LOGGER.log(FINER, e.getMessage(), e);
            }
            return empty();
        }
        
        return of(kunde);
    }
    
    /**
     * Alle Kunden in einer bestimmten Reihenfolge ermitteln
     * @param fetch Angabe, welche Objekte mitgeladen werden sollen, z.B. Bestellungen.
     * @param order Sortierreihenfolge, z.B. nach aufsteigenden IDs.
     * @return Liste der Kunden
     */
    public Optional<List<AbstractKunde>> findAll(FetchType fetch, OrderByType order) {
        final TypedQuery<AbstractKunde> query = OrderByType.ID.equals(order)
                                                ? em.createNamedQuery(AbstractKunde.ALL_ORDER_BY_ID,
                                                                      AbstractKunde.class)
                                                : em.createNamedQuery(AbstractKunde.ALL,
                                                                      AbstractKunde.class);
        setEntityGraph(query, fetch);
        final List<AbstractKunde> kunden = query.getResultList();
        return kunden.isEmpty() ? empty() : of(kunden);
    }
    

    /**
     * Kunden mit gleichem Nachnamen suchen.
     * @param nachname Der gemeinsame Nachname der gesuchten Kunden
     * @param fetch Angabe, welche Objekte mitgeladen werden sollen, z.B. Bestellungen
     * @return Liste der gefundenen Kunden oder NotFoundException
     */
    public Optional<List<AbstractKunde>> findByNachname(String nachname, FetchType fetch) {
        final Optional<List<String>> loginnamenOpt = iamInstance.get().findLoginnamenByNachname(nachname);
        if (!loginnamenOpt.isPresent()) {
            return empty();
        }
        
        // SELECT k
        // FROM   AbstractKunde k
        // WHERE  k.loginname = ? OR k.loginname = ? OR ...
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<AbstractKunde> criteriaQuery = builder.createQuery(AbstractKunde.class);
        final Root<AbstractKunde> kundeRoot = criteriaQuery.from(AbstractKunde.class);
        
        final Path<String> loginnamePath = kundeRoot.get(AbstractKunde_.loginname);
        Predicate pred;
        final List<String> loginnamen = loginnamenOpt.get();
        if (loginnamen.size() == 1) {
            // Genau 1 id: kein OR notwendig
            pred = builder.equal(loginnamePath, loginnamen.get(0));
        } else {
            // Mind. 2x id, durch OR verknuepft
            final Predicate[] equals = loginnamen.stream()
                                                 .map(id -> builder.equal(loginnamePath, id))
                                                 .collect(toList())
                                                 .toArray(new Predicate[loginnamen.size()]);
            pred = builder.or(equals);
        }
        criteriaQuery.where(pred);

        final TypedQuery<AbstractKunde> query = em.createQuery(criteriaQuery);
        setEntityGraph(query, fetch);
        final List<AbstractKunde> kunden = query.getResultList();
        return kunden.isEmpty() ? empty() : of(kunden);
    }
    
    /**
     * Kunden suchen, die seit einem bestimmten Datum Kunde sind.
     * @param seit Das Datum
     * @return Liste der gefundenen Kunden oder NotFoundException
     */
    public Optional<List<AbstractKunde>> findBySeit(Date seit) {
        final List<AbstractKunde> kunden = em.createNamedQuery(AbstractKunde.BY_SEIT, AbstractKunde.class)
                                             .setParameter(AbstractKunde.PARAM_KUNDE_SEIT, seit)
                                             .getResultList();
        return kunden.isEmpty() ? empty() : of(kunden);
    }
    
    /**
     * Kunden mit gleichem Geschlecht suchen.
     * @param geschlecht Das Geschlecht
     * @return Liste der gefundenen Kunden oder NotFoundException
     */
    public Optional<List<Privatkunde>> findByGeschlecht(GeschlechtType geschlecht) {
        final List<Privatkunde> kunden = em.createNamedQuery(Privatkunde.FIND_BY_GESCHLECHT, Privatkunde.class)
                                           .setParameter(Privatkunde.PARAM_GESCHLECHT, geschlecht)
                                           .getResultList();
        return kunden.isEmpty() ? empty() : of(kunden);
    }
    
    /**
     * Alle Privat- und Firmenkunden suchen.
     * @return Liste der gefundenen Kunden
     */
    public Optional<List<AbstractKunde>> findPrivatkundenFirmenkunden() {
        final List<AbstractKunde> kunden = em.createNamedQuery(AbstractKunde.PRIVATKUNDEN_FIRMENKUNDEN,
                                                               AbstractKunde.class)
                                                       .getResultList();
        return kunden.isEmpty() ? empty() : of(kunden);
    }

    /**
     * Kunden mit einer Mindestbestellmenge suchen
     * @param mindestBestellmenge Die minimale Anzahl bestellter Artikel
     * @return Die gefundenen Kunden oder NotFoundException
     */
    public Optional<List<AbstractKunde>> findByMindestBestellmenge(int mindestBestellmenge) {
        // SELECT DISTINCT k
        // FROM   AbstractKunde k
        //        JOIN k.bestellungen b
        //        JOIN b.bestellpositionen bp
        // WHERE  bp.anzahl >= ?
        
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<AbstractKunde> criteriaQuery = builder.createQuery(AbstractKunde.class);
        final Root<AbstractKunde> kundeRoot = criteriaQuery.from(AbstractKunde.class);

        final Join<AbstractKunde, Bestellung> b = kundeRoot.join(AbstractKunde_.bestellungen);
        final Join<Bestellung, Bestellposition> bp = b.join(Bestellung_.bestellpositionen);
        criteriaQuery.where(builder.gt(bp.<Integer>get(Bestellposition_.anzahl), mindestBestellmenge))
                     .distinct(true);
        
        final List<AbstractKunde> kunden = em.createQuery(criteriaQuery).getResultList();
        return kunden.isEmpty() ? empty() : of(kunden);
    }
    
    /**
     * Kunden zu den Suchkriterien suchen
     * @param seit Datum seit
     * @param geschlecht Geschlecht
     * @param minBestMenge Mindestbestellmenge
     * @return Die gefundenen Kunden oder NotFoundException
     */
    public Optional<List<AbstractKunde>> findByCriteria(Date seit,
                                                        GeschlechtType geschlecht,
                                                        Integer minBestMenge) {
        // SELECT DISTINCT k
        // FROM   AbstractKunde [Privatkunde] k
        // WHERE  email = ? AND nachname = ? AND seit = ? and geschlecht = ?
        
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<AbstractKunde> criteriaQuery = builder.createQuery(AbstractKunde.class);
        final Root<AbstractKunde> kundeRoot = criteriaQuery.from(AbstractKunde.class);
        
        Predicate pred = null;

        if (seit != null) {
            final Path<Date> seitPath = kundeRoot.get(AbstractKunde_.seit);
            pred = builder.equal(seitPath, seit);
        }
        if (minBestMenge != null) {
            final Path<Integer> anzahlPath = kundeRoot.join(AbstractKunde_.bestellungen)
                                                      .join(Bestellung_.bestellpositionen)
                                                      .get(Bestellposition_.anzahl);
            final Predicate tmpPred = builder.gt(anzahlPath, minBestMenge);
            pred = pred == null ? tmpPred : builder.and(pred, tmpPred);
        }
        
        if (geschlecht != null) {
            // Geschlecht gibt es nur bei der abgeleiteten Klasse Privatkunde
            final Root<Privatkunde> pk = builder.treat(kundeRoot, Privatkunde.class);
            final Path<GeschlechtType> geschlechtPath = pk.get(Privatkunde_.geschlecht);
            final Predicate tmpPred = builder.equal(geschlechtPath, geschlecht);
            pred = pred == null ? tmpPred : builder.and(pred, tmpPred);
        }
        
        criteriaQuery.where(pred)
                     .distinct(true);
        
        final TypedQuery<AbstractKunde> query = em.createQuery(criteriaQuery);
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Criteria Query: " + query.unwrap(org.hibernate.Query.class).getQueryString());
            // EclipseLink statt Hibernate:
            //LOGGER.finest("Criteria Query: " + query.unwrap(JpaQuery.class).getDatabaseQuery().getSQLString();   //NOSONAR
        }
        
        final List<AbstractKunde> kunden = query.getResultList();
        return kunden.isEmpty() ? empty() : of(kunden);
    }

    
    /**
     * Einen vorhandenen Kunden aktualisieren
     * @param kunde Der aktualisierte Kunde
     * @param <K> Privatkunde oder Firmenkunde
     * @return Der aktualisierte Kunde
     */
    public <K extends AbstractKunde> Optional<K> update(K kunde) {
        return update(kunde, false);
    }
    
    /**
     * Einen vorhandenen Kunden aktualisieren
     * @param <K> Privatkunde oder Firmenkunde
     * @param kunde Der aktualisierte Kunde
     * @param kundeBereitsGeladen Das Kundenobjekt wurde z.B. in der Resource-Klasse bereits geladen
     * @return Der aktualisierte Kunde
     */
    public <K extends AbstractKunde> Optional<K> update(K kunde, boolean kundeBereitsGeladen) {
        // kunde vom EntityManager trennen, weil anschliessend Suchvorgaenge fuer Plausibilitaetspruefungen erfolgen
        em.detach(kunde);
        
        // Das ValueObject fuer Identity sichern, weil durch Ueberpruefen und Laden von der DB
        // dieses Objekt auch ueberschrieben wird
        final IdentityVO identity = kunde.getIdentity();
        
        if (!kundeBereitsGeladen) {
            // Wurde das Objekt konkurrierend geloescht?
            final Optional<AbstractKunde> tmpKunde = findById(kunde.getId(), NUR_KUNDE);
            if (!tmpKunde.isPresent()) {
                throw new ConcurrentDeletedException(kunde.getId());
            }
            em.detach(tmpKunde.get());
        }
        
        // Gibt es ein anderes Objekt mit gleicher Email-Adresse?
        final String email = identity.getEmail();
        final String loginname = identity.getLoginname();
        final IdentityAccessManagement iam = iamInstance.get();
        final Optional<List<IdentityVO>> identities = iam.findIdentitiesByEmail(email);
        if (identities.isPresent()) {
            // Email bereits verwendet: gleicher oder anderer User
            final Optional<IdentityVO> tmpIdentityOpt = identities.get()
                                                                  .stream()
                                                                  .filter(tmpIdentity -> !Objects.equals(tmpIdentity.getLoginname(), loginname))
                                                                  .findAny();
            
            if (tmpIdentityOpt.isPresent()) {
                // Emailadresse bei einem anderen User verwendet
                throw new EmailExistsException(email);
            }
        }

        // Bei merge() wird wegen der optimistischen Synchronisation der Kundendatensatz gelesen, um die Versionsnummer zu erhalten.
        // Dadurch wird aber der Listener fuer @PostLoad ausgeloest und die neuen Werte im transienten Attribut fuer Identity werden ueberschrieben.
        // Deshalb muessen die Identity-Werte zuerst gespeichert werden, bevor der Kundendatensatz aktualisiert wird.
        // Ausserdem wird ein Listener fuer PreUpdate (oder PostUpdate) erst am TRANSAKTIONSENDE aufgerufen, wenn das Commit durchgefuehrt wird.
        iam.updateIdentity(kunde.getIdentity());
        
        // ggf. OptimisticLockException
        final K result = em.merge(kunde);
        
        return of(result);
    }

    /**
     * Einen Kunden loeschen
     * @param <K> Privatkunde oder Firmenkunde
     * @param kunde zu loeschender Kunden
     */
    public <K extends AbstractKunde> void delete(K kunde) {
        if (kunde == null) {
            LOGGER.log(WARNING, "Zu loeschender Kunde == null");
            return;
        }
        delete(kunde.getId());
    }

    /**
     * Einen Kunden zu gegebener ID loeschen
     * @param kundeId Die ID des zu loeschenden Kunden
     */
    public void delete(long kundeId) {
        final Optional<AbstractKunde> kundeOpt = findById(kundeId, MIT_BESTELLUNGEN);
        if (!kundeOpt.isPresent()) {
            // Der Kunde existiert nicht oder ist bereits geloescht
            return;
        }

        final AbstractKunde kunde = kundeOpt.get();
        if (hasBestellungen(kunde)) {
            throw new KundeDeleteBestellungException(kunde);
        }
        final List<Warenkorbposition> warenkorb = getWarenkorb(kunde);
        if (!warenkorb.isEmpty()) {
            throw new KundeDeleteWarenkorbException(kunde, warenkorb);
        }

        // Kundendaten loeschen
        em.remove(kunde);
    }

    
    /**
     * Einem Kunden eine hochgeladene Datei ohne MIME Type (bei RESTful WS) zuordnen
     * @param <K> Primvatkunde oder Firmenkunde
     * @param kunde Das Kundenobjekt fuer die hochgeladene Datei
     * @param bytes Das Byte-Array der hochgeladenen Datei
     * @return Das Kundenobjekt
     */
    public <K extends AbstractKunde> K setFile(K kunde, byte[] bytes) {
        final MimeType mimeType = fileHelperInstance.get().getMimeType(bytes);
        setFile(kunde, bytes, mimeType);
        return kunde;
    }
    
    /**
     * Einem Kunden eine hochgeladene Datei zuordnen
     * @param <K> Privatkunde oder Firmenkunde
     * @param kunde Der betroffene Kunde
     * @param bytes Das Byte-Array der hochgeladenen Datei
     * @param mimeTypeStr Der MIME-Type als String
     * @return Das Kundenobjekt
     */
    public <K extends AbstractKunde> K setFile(K kunde, byte[] bytes, String mimeTypeStr) {
        final MimeType mimeType = MimeType.build(mimeTypeStr);
        setFile(kunde, bytes, mimeType);
        return kunde;
    }
    
    private <K extends AbstractKunde> void setFile(K kunde, byte[] bytes, MimeType mimeType) {
        if (LOGGER.isLoggable(FINER)) {
            if (bytes == null) {
                LOGGER.warning("setFile: kunde=" + kunde + ", 0 (!) bytes, mimeType=" + mimeType);
            } else {
                LOGGER.finer("setFile: kunde=" + kunde + ", bytes=" + bytes.length + ", mimeType=" + mimeType);
            }
        }

        if (mimeType == null) {
            throw new NoMimeTypeException();
        }
        
        final FileHelper fileHelper = fileHelperInstance.get();
        final String filename = fileHelper.getFilename(kunde.getClass(), kunde.getId(), mimeType);
        
        // Gibt es noch kein (Multimedia-) File
        File file = kunde.getFile();
        if (file == null) {
            file = new File(bytes, filename, mimeType);
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.finest("Neue Datei " + file);
            }
            kunde.setFile(file);
            em.persist(file);
        } else {
            file.set(bytes, filename, mimeType);
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.finest("Ueberschreiben der Datei " + file);
            }
            em.merge(kunde);
            em.merge(file);
        }

        // Hochgeladenes Bild/Video/Audio in einem parallelen Thread als Datei fuer die Web-Anwendung abspeichern
        final File newFile = kunde.getFile();
        managedExecutorServiceInstance.get().execute(() -> fileHelper.store(newFile));
    }

    private <K extends AbstractKunde> boolean hasBestellungen(K kunde) {
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("hasBestellungen BEGINN: " + kunde);
        }
        
        boolean result = false;
        
        // Gibt es den Kunden und hat er mehr als eine Bestellung?
        // Bestellungen nachladen wegen Hibernate-Caching
        if (kunde != null && kunde.getBestellungen() != null && !kunde.getBestellungen().isEmpty()) {
            result = true;
        }
        
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("hasBestellungen ENDE: " + result);
        }
        return result;
    }

    
    private <K extends AbstractKunde> List<Warenkorbposition> getWarenkorb(K kunde) {
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("getWarenkorb BEGINN: " + kunde);
        }
        
        final List<Warenkorbposition> result = em.createNamedQuery(Warenkorbposition.FIND_POSITIONEN_BY_KUNDE, Warenkorbposition.class)
                                                 .setParameter(Warenkorbposition.PARAM_KUNDE, kunde)
                                                 .getResultList();
        
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("getWarenkorb ENDE: " + result);
        }
        return result;
    }
}
