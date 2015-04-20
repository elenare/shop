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
import de.shop.iam.domain.IdentityVO;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.interceptor.Log;
import de.shop.util.persistence.DefaultEM;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.jboss.weld.experimental.ExperimentalEvent;



/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Dependent
@Log
public class RegistrierungBroker {
    private final Instance<EntityManager> emInstance;
    private final IdentityAccessManagement iam;
    private final Instance<Event<AbstractKunde>> eventInstance;

    @Inject
    RegistrierungBroker(@DefaultEM Instance<EntityManager> emInstance,
                        IdentityAccessManagement iam,
                        @NeuerKunde Instance<Event<AbstractKunde>> eventInstance) {
        super();
        this.emInstance = emInstance;
        this.iam = iam;
        this.eventInstance = eventInstance;
    }
    
    /**
     * Einen neuen Kunden anlegen
     * @param kunde Der neue Kunde
     * @param <K> Privatkunde oder Firmenkunde
     * @return Der neue Kunde einschliesslich generierter ID
     */
    public <K extends AbstractKunde> K save(K kunde) {
        if (kunde == null) {
            return null;
        }
    
        // Pruefung, ob ein solcher Kunde schon existiert
        final IdentityVO identity = kunde.getIdentity();
        final String loginname = identity.getLoginname();
        if (iam.findIdentityByLoginname(loginname).isPresent()) {
            throw new LoginnameExistsException(loginname);
        }
        
        final String email = identity.getEmail();
        if (iam.findIdentitiesByEmail(email).isPresent()) {
            // Ein Kunde mit der gleichen Email-Adresse existiert bereits
            throw new EmailExistsException(email);
        }
       
        emInstance.get().persist(kunde);

        // FIXME Java EE 8: Event statt ExperimentalEvent
        ((ExperimentalEvent<AbstractKunde>) eventInstance.get()).fireAsync(kunde);
        
        return kunde;
    }
}
