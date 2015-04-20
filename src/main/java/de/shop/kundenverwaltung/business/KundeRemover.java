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

import de.shop.iam.business.IdentityAccessManagement;
import de.shop.iam.domain.RolleType;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.persistence.DefaultEM;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Stateless
public class KundeRemover {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    @Inject
    @DefaultEM
    EntityManager em;
    
    @Inject
    KundenBroker kundenBroker;
    
    @Inject
    IdentityAccessManagement iam;
    
    @Schedule(dayOfMonth = "*", hour = "2", minute = "0", year = "*", persistent = false)
    public void deleteKundenOhneBestellungen() {
        // Bulk Delete ist wegen cascade=DELETE in AbstractKunde.adresse nicht moeglich. Zitat aus der Spezifikation:
        // "A delete operation only applies to entities of the specified class and its subclasses.
        // It does not cascade to related entities."
        
        // FIXME https://hibernate.atlassian.net/browse/HHH-8993 CriteriaDelete ignoriert cascade=DELETE
        // DELETE
        // FROM   AbstractKunde k
        // WHERE  k.bestellungen IS EMPTY
        //SONAR_COMMENTED_OUT_BEGIN
        //final CriteriaBuilder builder = em.getCriteriaBuilder();
        //final CriteriaDelete<AbstractKunde> criteriaDelete = builder.createCriteriaDelete(AbstractKunde.class);
        //final Root<AbstractKunde> kunden = criteriaDelete.from(AbstractKunde.class);
        //final Expression<List<Bestellung>> bestellungenPath = kunden.get(AbstractKunde_.bestellungen);
        //final Predicate pred = builder.isEmpty(bestellungenPath);
        //criteriaDelete.where(pred);
        //final int anzahl = em.createQuery(criteriaDelete).executeUpdate();
        //SONAR_COMMENTED_OUT_END
        
        final List<AbstractKunde> kunden =
                                  em.createNamedQuery(AbstractKunde.OHNE_BESTELLUNGEN, AbstractKunde.class)
                                    .getResultList();
        kunden.forEach(k -> {
            final String loginname = k.getLoginname();
            final Optional<List<RolleType>> rollen = iam.findRollen(loginname);
            if (!rollen.isPresent() || !rollen.get().contains(RolleType.ADMIN)) {
                kundenBroker.delete(k);
            }
        });
        
        LOGGER.info("" + kunden.size() + " Kunden ohne Bestellungen wurden geloescht");
    }
}
