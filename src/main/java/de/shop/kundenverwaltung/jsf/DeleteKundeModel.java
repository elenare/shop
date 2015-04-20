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

package de.shop.kundenverwaltung.jsf;

import de.shop.kundenverwaltung.business.KundeDeleteBestellungException;
import de.shop.kundenverwaltung.business.KundeDeleteWarenkorbException;
import de.shop.kundenverwaltung.business.KundenBroker;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.interceptor.Log;
import de.shop.util.jsf.Client;
import de.shop.util.jsf.Messages;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import java.util.logging.Logger;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import org.picketlink.authorization.annotations.RolesAllowed;

import static de.shop.iam.domain.RolleType.ADMIN_STRING;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.WARNING;

/**
 * Loeschen eines Kunden
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@ConversationScoped
@Transactional
@Log
public class DeleteKundeModel implements Serializable {
    private static final long serialVersionUID = -4148240172286380719L;
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private static final String JSF_KUNDENVERWALTUNG = "/p/kundenverwaltung/";
    private static final String JSF_DELETE_OK = JSF_KUNDENVERWALTUNG + "okDelete";
    
    private static final String REQUEST_KUNDE_ID = "kundeId";

    private static final String MSG_KEY_DELETE_KUNDE_BESTELLUNG = "kunde.deleteMitBestellung";
    private static final String MSG_KEY_DELETE_KUNDE_WARENKORB = "kunde.deleteMitWarenkorb";
    
    // Field Injection: KundenBroker ist nicht serialisierbar, weshalb transient
    // wegen @ConversationScoped notwendig ist. Kap. 6.6.5 der Spezifikation:
    // "If a managed bean which declares a passivating scope, ...:
    // * is not passivation capable,
    // * has an injection point that is not passivation capable,
    // * ...
    // then the container automatically detects the problem and treats it as a deployment problem."
    
    @Inject
    private transient KundenBroker kundenBroker;
    
    @Inject
    private transient Instance<Conversation> conversationInstance;
    
    @Inject
    private transient Instance<HttpServletRequest> requestInstance;
    
    @Inject
    private transient Instance<Messages> messagesInstance;
    
    @Inject
    @Client
    private transient Instance<Locale> localeInstance;


    DeleteKundeModel() {
        super();
    }

    @RolesAllowed(ADMIN_STRING)
    public String delete(AbstractKunde kunde) {
        if (kunde == null) {
            LOGGER.log(WARNING, "Zu loeschender Kunde == null");
            return null;
        }

        try {
            kundenBroker.delete(kunde);
        } catch (KundeDeleteBestellungException e) {
            if (LOGGER.isLoggable(FINER)) {
                LOGGER.log(FINER, e.getMessage(), e);
            }
            messagesInstance.get().error(MSG_KEY_DELETE_KUNDE_BESTELLUNG, localeInstance.get(), null, e.getKundeId(), e.getAnzahlBestellungen());
            return null;
        } catch (KundeDeleteWarenkorbException e) {
            if (LOGGER.isLoggable(FINER)) {
                LOGGER.log(FINER, e.getMessage(), e);
            }
            messagesInstance.get().error(MSG_KEY_DELETE_KUNDE_WARENKORB, localeInstance.get(), null, e.getKundeId(), e.getWarenkorb().size());
            return null;
        }

        // Aufbereitung fuer okDelete.xhtml
        requestInstance.get().setAttribute(REQUEST_KUNDE_ID, kunde.getId());
        
        // ViewKundeModel ist ConversationScoped
        final Conversation conversation = conversationInstance.get();
        if (!conversation.isTransient()) {
            conversation.end();
        }
        return JSF_DELETE_OK;
    }
}
