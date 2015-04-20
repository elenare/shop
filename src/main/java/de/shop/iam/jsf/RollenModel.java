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

package de.shop.iam.jsf;

import de.shop.iam.business.IdentityAccessManagement;
import de.shop.util.interceptor.Log;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.picketlink.Identity;
import org.picketlink.idm.model.basic.User;



/**
 * Dialogsteuerung f&uuml;r Authentifizierung (Login und Logout) und Authorisierung (rollenbasierte Berechtigungen).
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Model
@Transactional
@Log
public class RollenModel {
    private Identity identity;
    private Instance<IdentityAccessManagement> iamInstance;

    /**
     * Package-private Default-Konstruktor f&uuml;r JSF
     */
    RollenModel() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param identity zu injizierendes Objekt f&uuml;r Identity
     * @param iamInstance zu injizierendes Instance-Objekt f&uuml;r IdentityAccessManagement
     */
    @Inject
    RollenModel(Identity identity, Instance<IdentityAccessManagement> iamInstance) {
        super();
        this.identity = identity;
        this.iamInstance = iamInstance;
    }
    
    public boolean isInRole(String rolle) {
        if (!identity.isLoggedIn()) {
            return false;
        }
        return iamInstance.get().hasRole(identity.getAccount(), rolle);
    }
    
    public User getUser() {
        if (!identity.isLoggedIn()) {
            return null;
        }
        
        return (User) identity.getAccount();
    }
}
