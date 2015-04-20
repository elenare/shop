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

package de.shop.kundenverwaltung.domain;

import de.shop.iam.business.IdentityAccessManagement;
import de.shop.iam.domain.IdentityVO;
import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.transaction.Transactional;

import static de.shop.iam.domain.RolleType.KUNDE;
import static java.util.logging.Level.FINER;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Dependent
@Transactional
public class KundeListener {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    // FIXME https://issues.jboss.org/browse/WFLY-2387
    //@Inject
    private IdentityAccessManagement iam;
    
    @Inject
    private BeanManager beanManager;
    
    // Wird von EntityManager.merge() aufgerufen, so dass die aktuellen Identity-Werte mit den alten
    // Werten aus dem IAM-System ueberschrieben werden
    @PostLoad
    private void loadIdentity(AbstractKunde kunde) {
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("loadIdentity BEGINN: " + kunde);
        }
        
        workaroundWFLY2387();
        final IdentityVO identity = iam.findIdentityByLoginname(kunde.getLoginname()).get();
        kunde.setIdentity(identity);
        
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("loadIdentity ENDE: " + kunde.getIdentity());
        }
    }
    
    @PrePersist
    private void storeIdentity(AbstractKunde kunde) {
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("storeIdentity BEGINN: " + kunde.getIdentity());
        }

        final IdentityVO identity = kunde.getIdentity();
        kunde.setLoginname(identity.getLoginname());
        
        workaroundWFLY2387();
        iam.createIdentity(identity);
        iam.grant(identity, KUNDE);
        
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("storeIdentity ENDE");
        }
    }
    
    // Bei merge() wird wegen der optimistischen Synchronisation der Kundendatensatz gelesen, um die Versionsnummer zu erhalten.
    // Dadurch wird aber der Listener fuer @PostLoad ausgeloest und die neuen Werte im transienten Attribut fuer Identity werden ueberschrieben.
    // Deshalb muessen die Identity-Werte zuerst gespeichert werden, bevor der Kundendatensatz aktualisiert wird.
    // Ausserdem wird ein Listener fuer PreUpdate (oder PostUpdate) erst am TRANSAKTIONSENDE aufgerufen, wenn das Commit durchgefuehrt wird.
    
    @PreRemove
    private void removeIdentity(AbstractKunde kunde) {
        final IdentityVO identity = kunde.getIdentity();
        
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("removeIdentity BEGINN: " + identity);
        }
        
        workaroundWFLY2387();
        iam.removeIdentity(identity);
        
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("removeIdentity ENDE");
        }
    }
    
    // FIXME https://issues.jboss.org/browse/WFLY-2387
    private void workaroundWFLY2387() {
        if (iam != null) {
            return;
        }
        
        final Set<Bean<?>> beans = beanManager.getBeans(IdentityAccessManagement.class);
        final CreationalContext<IdentityAccessManagement> creationalContext = beanManager.createCreationalContext(null);
        @SuppressWarnings("unchecked")
        final Bean<IdentityAccessManagement> iamServiceBean = (Bean<IdentityAccessManagement>) beans.iterator().next();
        iam = iamServiceBean.create(creationalContext);
    }
}
