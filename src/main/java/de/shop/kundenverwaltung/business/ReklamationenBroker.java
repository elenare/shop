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

import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Reklamation;
import de.shop.util.interceptor.Log;
import de.shop.util.persistence.DefaultEM;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.NUR_KUNDE;
import static java.util.Optional.empty;
import static java.util.Optional.of;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Dependent
@Log
public class ReklamationenBroker {
    private final EntityManager em;
    private final Instance<KundenBroker> kundenBrokerInstance;
    
    @Inject
    ReklamationenBroker(@DefaultEM EntityManager em, Instance<KundenBroker> kundenBrokerInstance) {
        super();
        this.em = em;
        this.kundenBrokerInstance = kundenBrokerInstance;
    }
    
    /**
     * Die Reklamationen eines Kunden ermitteln
     * @param kundeId Die ID des Kunden
     * @return Die Liste mit seinen Reklamationen
     */
    public Optional<List<Reklamation>> findByKundeId(long kundeId) {
        final List<Reklamation> reklamationen = em.createNamedQuery(Reklamation.FIND_BY_KUNDE_ID, Reklamation.class)
                                                  .setParameter(Reklamation.PARAM_KUNDE_ID, kundeId)
                                                  .getResultList();
        return reklamationen.isEmpty()? empty() : of(reklamationen);
    }

    
    /**
     * Eine neue Reklamation anlegen
     * @param reklamation Die neue Reklamation
     * @param kunde Der zugehoerige Kunde
     * @return Die neue Reklamation einschliesslich ggf. generierter ID
     */
    public Reklamation save(Reklamation reklamation, AbstractKunde kunde) {
        kundenBrokerInstance.get().findById(kunde.getId(), NUR_KUNDE);
        em.persist(reklamation);
        return reklamation;
    }
}
