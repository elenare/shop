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

package de.shop.bestellverwaltung.business;

import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Warenkorbposition;
import de.shop.bestellverwaltung.domain.Warenkorbposition_;
import de.shop.kundenverwaltung.business.KundenBroker;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.interceptor.Log;
import de.shop.util.persistence.DefaultEM;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.jboss.weld.experimental.ExperimentalEvent;

import static de.shop.bestellverwaltung.domain.Bestellung.GRAPH_LIEFERUNGEN;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.MIT_BESTELLUNGEN;
import static de.shop.util.Constants.LOADGRAPH;
import static java.math.BigDecimal.ZERO;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.logging.Level.FINEST;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Dependent
@Log
public class BestellungenBroker {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    public enum FetchType {
        NUR_BESTELLUNG,
        MIT_LIEFERUNGEN
    }
    
    private final EntityManager em;
    private final Instance<KundenBroker> kundenBrokerInstance;
    private final Instance<Event<Bestellung>> event;
    
    @Inject
    BestellungenBroker(@DefaultEM EntityManager em,
                       Instance<KundenBroker> kundenBrokerInstance,
                       @NeueBestellung Instance<Event<Bestellung>> event) {
        super();
        this.em = em;
        this.kundenBrokerInstance = kundenBrokerInstance;
        this.event = event;
    }
    
    /**
     * Bestellung zu gegebener ID suchen.
     * @param id Gegebene ID
     * @param fetch Welche Objekte sollen mitgeladen werden, z.B. Lieferungen
     * @return Die gefundene Bestellung oder null
     */
    public Optional<Bestellung> findBestellungById(long id, FetchType fetch) {
        Bestellung bestellung;
        EntityGraph<?> entityGraph;
        switch (fetch) {
            case NUR_BESTELLUNG:
                bestellung = em.find(Bestellung.class, id);
                break;
                
            case MIT_LIEFERUNGEN:
                entityGraph = em.getEntityGraph(GRAPH_LIEFERUNGEN);
                bestellung = em.find(Bestellung.class, id, Collections.singletonMap(LOADGRAPH, entityGraph));
                break;
                
            default:
                bestellung = em.find(Bestellung.class, id);
                break;
        }
        
        return ofNullable(bestellung);
    }

    /**
     * Bestellungen zu einem gegebenen Kunden suchen.
     * @param <K> abgeleitete Klasse von AbstractKunde
     * @param kunde Der gegebene Kunde
     * @param fetch Welche Objekte sollen mitgeladen werden, z.B. Lieferungen
     * @return Die gefundenen Bestellungen
     */
    public <K extends AbstractKunde> Optional<List<Bestellung>> findBestellungenByKunde(K kunde, FetchType fetch) {
        if (kunde == null) {
            return empty();
        }
        
        final TypedQuery<Bestellung> query = em.createNamedQuery(Bestellung.BY_KUNDEID, Bestellung.class)
                                               .setParameter(Bestellung.PARAM_KUNDEID, kunde.getId());
                
        switch (fetch) {
            case NUR_BESTELLUNG:
                break;
                
            case MIT_LIEFERUNGEN:
                final EntityGraph<?> entityGraph = em.getEntityGraph(GRAPH_LIEFERUNGEN);
                query.setHint(LOADGRAPH, entityGraph);
                break;
                
            default:
                break;
        }
        
        final List<Bestellung> bestellungen = query.getResultList();
        return bestellungen.isEmpty() ? empty() : of(bestellungen);
    }


    /**
     * Zuordnung einer neuen, transienten Bestellung zu einem existierenden, persistenten Kunden,
     * identifiziert durch den Username.
     * @param bestellung Die neue Bestellung
     * @return Die neue Bestellung einschliesslich generierter ID
     */
    public Bestellung save(Bestellung bestellung) {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Bestellpositionen: " + bestellung.getBestellpositionen());
        }
        
        // Kunde muss existieren wegen @RolesAllowed
        final AbstractKunde kunde = kundenBrokerInstance.get().findByLoginname(MIT_BESTELLUNGEN).get();
        return save(bestellung, kunde);
    }
    
    /**
     * Neue Bestellung aus dem persistenten Warenkorb zum eingeloggten Kunden anlegen.
     * @return Die neue Bestellung einschliesslich generierter ID
     */
    public Bestellung save() {
        // Den persistenten Kunden mit der transienten Bestellung verknuepfen
        final Optional<? extends AbstractKunde> kundeOpt = kundenBrokerInstance.get().findByLoginname(MIT_BESTELLUNGEN);
        // Kunde muss existieren wegen @RolesAllowed
        final AbstractKunde kunde = kundeOpt.get();
        final List<Warenkorbposition> positionen = em.createNamedQuery(Warenkorbposition.FIND_POSITIONEN_BY_KUNDE,
                                                                       Warenkorbposition.class)
                                                     .setParameter(Warenkorbposition.PARAM_KUNDE, kunde)
                                                     .getResultList();
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest(positionen.toString());
        }
        if (positionen.isEmpty()) {
            return null;
        }
        final List<Bestellposition> bpList = positionen.stream()
                                                       .map(wp -> new Bestellposition(wp.getArtikel(), wp.getAnzahl()))
                                                       .collect(Collectors.toList());
        Bestellung bestellung = new Bestellung(bpList);
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest(bestellung.toString());
        }
        bestellung = save(bestellung, kunde);
        
        // Warenkorbpositionen des Kunden loeschen
        //    DELETE
        //    FROM   Warenkorbposition wp
        //    WHERE  wp.kunde = ?    
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaDelete<Warenkorbposition> criteriaDelete = builder.createCriteriaDelete(Warenkorbposition.class);
        final Root<Warenkorbposition> wpRoot = criteriaDelete.from(Warenkorbposition.class);
        final Path<? extends AbstractKunde> kundePath = wpRoot.get(Warenkorbposition_.kunde);
        final Predicate pred = builder.equal(kundePath, kunde);
        criteriaDelete.where(pred);
        em.createQuery(criteriaDelete).executeUpdate();
        
        return bestellung;
    }
    
    /**
     * Zuordnung einer neuen, transienten Bestellung zu einem existierenden, persistenten Kunden.
     * Der Kunde ist fuer den EntityManager bekannt, die Bestellung dagegen nicht. Das Zusammenbauen
     * wird sowohl fuer einen Web Service aus auch fuer eine Webanwendung benoetigt.
     * @param <K> abggeleitete Klasse von AbstractKunde
     * @param bestellung Die neue Bestellung
     * @param kunde Der existierende Kunde
     * @return Die neue Bestellung einschliesslich generierter ID
     */
    public <K extends AbstractKunde> Bestellung save(Bestellung bestellung, K kunde) {
        // Den persistenten Kunden mit der transienten Bestellung verknuepfen
        final AbstractKunde kundeDb = em.contains(kunde)
                                      ? kunde
                                      : kundenBrokerInstance.get().findById(kunde.getId(), MIT_BESTELLUNGEN).get();
        kundeDb.addBestellung(bestellung);
        bestellung.setKunde(kundeDb);
        
        final BigDecimal gesamtbetrag = bestellung.getBestellpositionen()
                                                  .stream()
                                                  .map(bp -> bp.getArtikel()
                                                               .getPreis()
                                                               .multiply(new BigDecimal(bp.getAnzahl())))
                                                  .reduce(ZERO, BestellungenBroker::sum);
        bestellung.setGesamtbetrag(gesamtbetrag);
        
        em.persist(bestellung);

        // FIXME Java EE 8: Event statt ExperimentalEvent
        ((ExperimentalEvent<Bestellung>) event.get()).fireAsync(bestellung);
        
        return bestellung;
    }
    
    // fuer den Lambda-Ausdruck in der Methode createBestellung (s.o.)
    private static BigDecimal sum(BigDecimal i, BigDecimal j) {
        if (i == null || BigDecimal.ZERO.equals(i)) {
            return j;
        }
        if (j == null || BigDecimal.ZERO.equals(j)) {
            return i;
        }
        return i.add(j);
    }
    
    public Warenkorbposition createWarenkorbposition(Warenkorbposition warenkorbposition) {
        if (warenkorbposition == null) {
            return null;
        }
        
        em.persist(warenkorbposition);
        return warenkorbposition;
    }
}
