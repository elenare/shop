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

import de.shop.bestellverwaltung.domain.Warenkorbposition;
import de.shop.bestellverwaltung.domain.Warenkorbposition_;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.persistence.DefaultEM;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Stateless
public class WarenkorbRemover {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private static final int VERGANGENE_TAGE = 30;
    // Std * Min * Sek * Millisek
    private static final long TAG_IN_MILLIS = 24 * 60 * 60 * 1000L;
    
    @Inject
    @DefaultEM
    private EntityManager em;
    
    @Schedule(dayOfMonth = "*", hour = "3", minute = "0", year = "*", persistent = false)
    public void deleteAlteWarenkoerbe() {
        // DELETE
        // FROM Warenkorbposition wp
        // WHERE wp.kunde IN (SELECT DISTINCT wpsub.kunde
        //                    FROM     Warenkorbposition wpsub
        //                    GROUP BY wpsub.kunde
        //                    HAVING   MAX(wpsub.aktualisiert) < ...)
        
        // DELETE-Anweisung
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaDelete<Warenkorbposition> criteriaDelete = builder.createCriteriaDelete(Warenkorbposition.class);
        final Root<Warenkorbposition> positionen = criteriaDelete.from(Warenkorbposition.class);
        
        // Subquery zur DELETE-Anweisung
        final Subquery<AbstractKunde> subquery = criteriaDelete.subquery(AbstractKunde.class);
        final Root<Warenkorbposition> positionenSub = subquery.from(Warenkorbposition.class);
        final Path<AbstractKunde> kunde = positionenSub.get(Warenkorbposition_.kunde);
        subquery.select(kunde);
        subquery.distinct(true);
        
        // GROUP BY und HAVING-Klausel der Subquery
        subquery.groupBy(kunde);
        final Path<Date> aktualisiert = positionenSub.get(Warenkorbposition_.aktualisiert);
        final Expression<Date> maxAktualisiert = builder.greatest(aktualisiert);
        final long aktuellesDatumMillis = new Date().getTime();
        final Date letztesDatum = new Date(aktuellesDatumMillis - (VERGANGENE_TAGE * TAG_IN_MILLIS));
        final Predicate havingKlausel = builder.lessThan(maxAktualisiert, letztesDatum);
        subquery.having(havingKlausel);
        
        // WHERE-Klausel fuer die DELETE-Anweisung
        final Expression<Boolean> whereKlausel = positionen.get(Warenkorbposition_.kunde)
                                                           .in(subquery);
        criteriaDelete.where(whereKlausel);
        
        final int anzahl = em.createQuery(criteriaDelete).executeUpdate();
        
        LOGGER.info("" + anzahl + " alte Warenkoerbe wurden geloescht");
    }
}
