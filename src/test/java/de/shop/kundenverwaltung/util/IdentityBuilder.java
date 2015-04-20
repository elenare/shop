/*
 * Copyright (C) 2015 Juergen Zimmermann, Hochschule Karlsruhe
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
package de.shop.kundenverwaltung.util;

import de.shop.iam.domain.Adresse;
import de.shop.iam.domain.IdentityVO;
import java.util.Date;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class IdentityBuilder {
    private final IdentityVO identity = new IdentityVO();
    
    public IdentityBuilder loginname(String loginname) {
        identity.setLoginname(loginname);
        return this;
    }
    
    public IdentityBuilder enabled(boolean enabled) {
        identity.setEnabled(enabled);
        return this;
    }
    
    public IdentityBuilder expirationDate(Date expirationDate) {
        identity.setExpirationDate(expirationDate);
        return this;
    }
    
    public IdentityBuilder password(String password) {
        identity.setPassword(password);
        return this;
    }
    
    public IdentityBuilder passwordWdh(String passwordWdh) {
        identity.setPasswordWdh(passwordWdh);
        return this;
    }
    
    public IdentityBuilder nachname(String nachname) {
        identity.setNachname(nachname);
        return this;
    }
    
    public IdentityBuilder vorname(String vorname) {
        identity.setVorname(vorname);
        return this;
    }
    
    public IdentityBuilder email(String email) {
        identity.setEmail(email);
        return this;
    }
    
    public IdentityBuilder adresse(Adresse adresse) {
        identity.setAdresse(adresse);
        return this;
    }
    
    public IdentityVO build() {
        return identity;
    }
}
