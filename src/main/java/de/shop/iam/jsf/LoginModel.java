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
import de.shop.util.jsf.Client;
import de.shop.util.jsf.Messages;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import java.util.logging.Logger;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.picketlink.Identity;
import org.picketlink.Identity.AuthenticationResult;
import org.picketlink.credential.DefaultLoginCredentials;

import static java.util.logging.Level.FINEST;
import static org.picketlink.Identity.AuthenticationResult.FAILED;


/**
 * Dialogsteuerung f&uuml;r Authentifizierung (Login und Logout) und Authorisierung (rollenbasierte Berechtigungen).
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Model
@Transactional
@Log
public class LoginModel {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private static final String MSG_KEY_LOGIN_ERROR = "iam.login.error";
    private static final String CLIENT_ID_LOGINNAME = "loginnameHeader";
    
    private Identity identity;
    private DefaultLoginCredentials credentials;
    private Instance<Messages> messagesInstance;
    private Instance<Locale> localeInstance;
    private Instance<LastPageModel> lastPageModelInstance;

    /**
     * Package-private Default-Konstruktor f&uuml;r JSF
     */
    LoginModel() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param identity zu injizierendes Objekt f&uuml;r Identity
     * @param credentials zu injizierendes Objekt f&uuml;r DefaultLoginCredentials
     * @param messagesInstance zu injizierendes Instance-Objekt f&uuml;r Messages
     * @param localeInstance zu injizierendes Instance-Objekt f&uuml;r Locale
     * @param lastPageModelInstance zu injizierendes Instance-Objekt f&uuml;r LastPageModel
     */
    @Inject
    LoginModel(Identity identity,
               DefaultLoginCredentials credentials,
               Instance<Messages> messagesInstance,
               @Client Instance<Locale> localeInstance,
               Instance<LastPageModel> lastPageModelInstance) {
        super();
        this.identity = identity;
        this.credentials = credentials;
        this.messagesInstance = messagesInstance;
        this.localeInstance = localeInstance;
        this.lastPageModelInstance = lastPageModelInstance;
    }
    
    /**
     * Einloggen eines registrierten Kunden mit Benutzername und Password.
     */
    public void login() {
        final String loginname = credentials.getUserId();
        final String password = credentials.getPassword();
        
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.log(FINEST, "loginname=" + loginname + ", password=" + password);
        }
        
        final AuthenticationResult result = identity.login();
        if (FAILED.equals(result)) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, "Fehlerhaftes Login fuer: " + loginname + "/" + password);
            }
            messagesInstance.get().error(MSG_KEY_LOGIN_ERROR, localeInstance.get(), CLIENT_ID_LOGINNAME);
        }
        
        //HTTP-Request statt Ajax-Request
        //return viewRoot.getViewId();   //NOSONAR
    }

    /**
     * Einloggen eines registrierten Kunden mit Benutzername und Password.
     * @return Pfad zur letzten Seite vor dem FORM-based Login
     */
    public String loginForm() {
        final String loginname = credentials.getUserId();
        final String password = credentials.getPassword();
        
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.log(FINEST, "loginname=" + loginname + ", password=" + password);
        }
        
        final AuthenticationResult result = identity.login();
        if (FAILED.equals(result)) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, "Fehlerhaftes Login fuer: " + loginname + "/" + password);
            }
            messagesInstance.get().error(MSG_KEY_LOGIN_ERROR, localeInstance.get(), CLIENT_ID_LOGINNAME);
        }
        
        final LastPageModel lastPageModel = lastPageModelInstance.get();
        final String lastPage = lastPageModel.getPath();
        lastPageModel.reset();
        
        return lastPage;
    }
}
