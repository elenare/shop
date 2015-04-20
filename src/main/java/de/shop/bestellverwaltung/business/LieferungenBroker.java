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

import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.util.interceptor.Log;
import de.shop.util.persistence.DefaultEM;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;

import static de.shop.bestellverwaltung.domain.Lieferung.GRAPH_BESTELLUNGEN;
import static de.shop.util.Constants.LOADGRAPH;
import static java.util.Optional.empty;
import static java.util.Optional.of;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Dependent
@Log
public class LieferungenBroker {
    private final EntityManager em;
    
    @Inject
    LieferungenBroker(@DefaultEM EntityManager em) {
        super();
        this.em = em;
    }

    /**
     * Lieferungen zu gegebenem Praefix der Liefernummer suchen.
     * @param nr Praefix der Liefernummer
     * @return Liste der Lieferungen
     */
    public Optional<List<Lieferung>> findByLiefernr(String nr) {
        final EntityGraph<?> entityGraph = em.getEntityGraph(GRAPH_BESTELLUNGEN);
        final List<Lieferung> lieferungen = em.createNamedQuery(Lieferung.BY_LIEFERNR, Lieferung.class)
                                              .setParameter(Lieferung.PARAM_LIEFER_NR, nr)
                                              .setHint(LOADGRAPH, entityGraph)
                                              .getResultList();
        if (lieferungen.isEmpty()) {
            return empty();
        }
        return of(lieferungen);
    }
    
    /**
     * Eine neue Lieferung in der DB anlegen
     * @param lieferung Die neue Lieferung
     * @return Die neue Lieferung einschliesslich generierter ID
     */
    public Lieferung save(Lieferung lieferung) {
        em.persist(lieferung);
        return lieferung;
    }
}
