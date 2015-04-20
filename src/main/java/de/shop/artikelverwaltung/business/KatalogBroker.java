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

package de.shop.artikelverwaltung.business;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.domain.Artikel_;
import de.shop.util.Strings;
import de.shop.util.interceptor.Log;
import de.shop.util.persistence.DefaultEM;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Dependent
@Log
public class KatalogBroker {
    @SuppressWarnings("FieldMayBeFinal")
    private EntityManager em;
    
    @Inject
    KatalogBroker(@DefaultEM EntityManager em) {
        super();
        this.em = em;
    }
    
    /**
     * Einen Artikel zu gegebener ID suchen,
     * falls kein Artikel gefunden wurde
     * @param id ID des gesuchten Artikels
     * @return Der Artikel zur gegebenen ID.
     */
    public Optional<Artikel> findById(long id) {
        final Artikel artikel = em.find(Artikel.class, id);
        return ofNullable(artikel);
    }
    
    /**
     * Verfuegbare Artikel ermitteln
     * @return Liste der verfuegbaren Artikel
     */
    public Optional<List<Artikel>> findVerfuegbareArtikel() {
        final List<Artikel> artikel = em.createNamedQuery(Artikel.VERFUEGBAR, Artikel.class)
                                        .getResultList();
        return artikel.isEmpty() ? empty() : of(artikel);
    }

    /**
     * Liste mit Artikeln mit gleicher Bezeichnung suchen,
     * falls die Liste leer ist
     * @param bezeichnung Die Bezeichnung der gesuchten Artikel suchen
     * @return Liste der gefundenen Artikel suchen
     */
    public Optional<List<Artikel>> findByBezeichnung(String bezeichnung) {
        if (Strings.isNullOrEmpty(bezeichnung)) {
            return findVerfuegbareArtikel();
        }
        
        final List<Artikel> artikel = em.createNamedQuery(Artikel.BY_BEZ, Artikel.class)
                                        .setParameter(Artikel.PARAM_BEZEICHNUNG, "%" + bezeichnung + "%")
                                        .getResultList();
        return artikel.isEmpty() ? empty() : of(artikel);
    }
    
    /**
     * Artikel mit exakter Bezeichnung suchen
     * @param bezeichnung Die genaue Bezeichnung des gesuchten Artikels
     * @return Gefundener Artikel
     */
    public Optional<Artikel> findByBezeichnungExakt(String bezeichnung) {
        try {
            return Optional.of(em.createNamedQuery(Artikel.BY_BEZ_EXAKT, Artikel.class)
                                 .setParameter(Artikel.PARAM_BEZEICHNUNG, bezeichnung)
                                 .getSingleResult());
        }
        catch (NoResultException e) {                                  //NOSONAR
            return Optional.empty();
        }
    }
    
    /**
     * Artikel zu gegebenen IDs suchen
     * @param ids Liste der IDs
     * @return Liste der gefundenen Artikel
     */
    public Optional<List<Artikel>> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return empty();
        }
        
        // SELECT a
        // FROM   Artikel a
        // WHERE  a.id = ? OR a.id = ? OR ...
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<Artikel> criteriaQuery = builder.createQuery(Artikel.class);
        final Root<Artikel> a = criteriaQuery.from(Artikel.class);

        final Path<Long> idPath = a.get(Artikel_.id);
            
        Predicate pred;
        if (ids.size() == 1) {
            // Genau 1 id: kein OR notwendig
            pred = builder.equal(idPath, ids.get(0));
        } else {
            // Mind. 2x id, durch OR verknuepft
            final Predicate[] equals = ids.stream()
                                          .map(id -> builder.equal(idPath, id))
                                          .collect(toList())
                                          .toArray(new Predicate[ids.size()]);
            pred = builder.or(equals);
        }
        criteriaQuery.where(pred);
            
        final List<Artikel> artikel = em.createQuery(criteriaQuery)
                                        .getResultList();
        return artikel.isEmpty() ? empty() : of(artikel);
    }
    
    /**
     * Liste der nichtbestellten Artikel ermitteln
     * @param anzahl Obergrenze fuer die maximale Anzahl der Artikel
     * @return Liste der gefundenen Artikel
     */
    public Optional<List<Artikel>> findNichtBestellt(int anzahl) {
        final List<Artikel> artikel = em.createNamedQuery(Artikel.NICHT_BESTELLT, Artikel.class)
                                        .setMaxResults(anzahl)
                                        .getResultList();
        return artikel.isEmpty() ? empty() : of(artikel);
    }
}
