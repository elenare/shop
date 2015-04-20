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

import de.shop.util.interceptor.Log;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import org.picketlink.Identity;

import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;


/**
 * Dialogsteuerung f&uuml;r Authentifizierung (Login und Logout) und Authorisierung (rollenbasierte Berechtigungen).
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Model
@Transactional
@Log
public class LogoutModel {
    private Identity identity;
    private HttpSession session;

    /**
     * Package-private Default-Konstruktor f&uuml;r JSF
     */
    LogoutModel() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param identity zu injizierendes Objekt f&uuml;r Identity
     * @param session zu injizierendes Objekt f&uuml;r HttpSession
     */
    @Inject
    LogoutModel(Identity identity, HttpSession session) {
        super();
        this.identity = identity;
        this.session = session;
    }
    
    /**
     * Ausloggen und L&ouml;schen der gesamten Session-Daten.
     * @return Pfad zur Startseite einschliesslich Redirect
     */
    public String logout() {
        identity.logout();
        session.invalidate();
        
        // redirect bewirkt neuen Request, der *NACH* der Session ist
        return JSF_INDEX + JSF_REDIRECT_SUFFIX;
    }
}
