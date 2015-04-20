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

package de.shop.kundenverwaltung.jsf.primefaces;

import de.shop.iam.business.IdentityAccessManagement;
import de.shop.kundenverwaltung.business.EmailExistsException;
import de.shop.kundenverwaltung.business.KundenBroker;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.util.ShopRuntimeException;
import de.shop.util.interceptor.Log;
import de.shop.util.jsf.Client;
import de.shop.util.jsf.Messages;
import de.shop.util.persistence.ConcurrentDeletedException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Instance;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.OptimisticLockException;
import javax.transaction.Transactional;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import static de.shop.iam.domain.RolleType.ADMIN_STRING;
import static de.shop.iam.domain.RolleType.KUNDE_STRING;
import static de.shop.iam.domain.RolleType.MITARBEITER_STRING;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.MIT_BESTELLUNGEN;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.NUR_KUNDE;
import static de.shop.kundenverwaltung.jsf.primefaces.UpdateKundePush.UPDATE_ENDPOINT;
import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;
import static java.util.logging.Level.FINEST;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Modifikation eines Kunden
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@ConversationScoped
@Transactional
@Log
public class UpdateKundeModel implements Serializable {
    private static final long serialVersionUID = -8271658546321873251L;

    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private static final String JSF_KUNDENVERWALTUNG = "/p/kundenverwaltung/";
    private static final String JSF_UPDATE_PRIVATKUNDE = JSF_KUNDENVERWALTUNG + "updatePrivatkunde";
    private static final String JSF_UPDATE_FIRMENKUNDE = JSF_KUNDENVERWALTUNG + "updateFirmenkunde";
    
    private static final String MSG_KEY_KUNDE_NOT_FOUND_BY_ID = "kunde.notFound.id";
    private static final String MSG_KEY_EMAIL_EXISTS = ".kunde.emailExists";
    
    private static final String CLIENT_ID_UPDATE_EMAIL = "email";
    private static final String MSG_KEY_CONCURRENT_UPDATE = "persistence.concurrentUpdate";
    private static final String MSG_KEY_CONCURRENT_DELETE = "persistence.concurrentDelete";

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
    private transient Conversation conversation;
    
    @Inject
    private transient Instance<IdentityAccessManagement> iamInstance;
    
    @Inject
    @Client
    private transient Instance<Locale> localeInstance;
    
    @Inject
    private transient Instance<Messages> messagesInstance;
    
    private transient AbstractKunde kunde;
    private transient List<String> hobbys;
    
    // fuer ValueChangeListener
    private boolean geaendertKunde;

    // fuer Push
    private EventBus eventBus;
    
    // fuer Breadcrumbs
    private String vorherigeSeite;

    UpdateKundeModel() {
        super();
    }
    
    @PostConstruct()
    private void postConstruct() {
        // Kein CDI-Producer, weil die vom Interface EventBus abgeleitete Klasse nicht Serializable ist
        eventBus = EventBusFactory.getDefault().eventBus();
    }
    
    @Override
    public String toString() {
        return "UpdateKundeModel {kunde=" + kunde + ", geaendertKunde=" + geaendertKunde + '}';
    }
    
    public AbstractKunde getKunde() {
        return kunde;
    }

    public List<String> getHobbys() {
        return hobbys;
    }
    
    public void setHobbys(List<String> hobbys) {
        this.hobbys = hobbys;
    }
    
    /**
     * Hobbys bei preRenderView als Liste von Strings fuer JSF aufbereiten,
     * wenn ein existierender Privatkunde in updatePrivatkunde.xhtml aktualisiert wird
     */
    public void hobbyTypeToString() {
        if (!kunde.getClass().equals(Privatkunde.class)) {
            return;
        }
        
        final Privatkunde privatkunde = Privatkunde.class.cast(kunde);
        hobbys = privatkunde.getHobbys()
                            .stream()
                            .map(HobbyType::name)
                            .collect(toList());
        
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("hobbys = " + hobbys);
        }
    }
    
    /**
     * Verwendung als ValueChangeListener bei updatePrivatkunde.xhtml und updateFirmenkunde.xhtml
     * @param e Ereignis-Objekt mit der Aenderung in einem Eingabefeld, z.B. inputText
     */
    public void geaendert(ValueChangeEvent e) {
        if (geaendertKunde) {
            return;
        }
        geaendertKunde = !Objects.equals(e.getNewValue(), e.getOldValue());
    }

    public String getVorherigeSeite() {
        return vorherigeSeite;
    }
    
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    public String update() {
        if (!geaendertKunde || kunde == null) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.finest("Keine Aenderung der Kundendaten");
            }
            return JSF_INDEX;
        }
        
        // Hobbys konvertieren: String -> HobbyType
        if (kunde.getClass().equals(Privatkunde.class)) {
            final Privatkunde privatkunde = Privatkunde.class.cast(kunde);
            final Set<HobbyType> hobbysPrivatkunde = hobbys.stream()
                                                           .map(HobbyType::valueOf)
                                                           .collect(toSet());
            privatkunde.setHobbys(hobbysPrivatkunde);
        }
        
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Aktualisierter Kunde: " + kunde);
        }
        
        final Optional<AbstractKunde> kundeOpt;
        try {
            kundeOpt = kundenBroker.update(kunde);
        } catch (EmailExistsException | ConcurrentDeletedException | OptimisticLockException e) {
            conversation.end();
            return updateErrorMsg(e);
        }
        
        kunde = kundeOpt.get();

        // Push-Event fuer Webbrowser
        eventBus.publish(UPDATE_ENDPOINT, String.valueOf(kunde.getId()));
        
        conversation.end();
        return JSF_INDEX + JSF_REDIRECT_SUFFIX;
    }
    
    private String updateErrorMsg(RuntimeException e) {
        final Class<? extends RuntimeException> exceptionClass = e.getClass();
        final Messages messages = messagesInstance.get();
        final Locale locale = localeInstance.get();
        
        if (EmailExistsException.class.equals(exceptionClass)) {
            @SuppressWarnings("ThrowableResultIgnored")
            final EmailExistsException e2 = EmailExistsException.class.cast(e);
            messages.error(MSG_KEY_EMAIL_EXISTS, locale, CLIENT_ID_UPDATE_EMAIL, e2.getEmail());
        } else if (OptimisticLockException.class.equals(exceptionClass)) {
            messages.error(MSG_KEY_CONCURRENT_UPDATE, locale, null);

        } else if (ConcurrentDeletedException.class.equals(exceptionClass)) {
            messages.error(MSG_KEY_CONCURRENT_DELETE, locale, null);
        } else {
            throw new ShopRuntimeException(e);
        }
        return null;
    }
    
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public String load(long kundeId, String vorherigeSeite) {
        final Optional<? extends AbstractKunde> kundeOpt = kundenBroker.findById(kundeId, NUR_KUNDE);
        if (!kundeOpt.isPresent()) {
            return null;
        }
        
        this.vorherigeSeite = vorherigeSeite;
        
        if (!conversation.isTransient()) {
            conversation.end();
        }
        conversation.begin();

        this.kunde = kundeOpt.get();
        final String outcome = Privatkunde.class.equals(kunde.getClass()) ? JSF_UPDATE_PRIVATKUNDE : JSF_UPDATE_FIRMENKUNDE;
        return outcome +  "?cid=" + conversation.getId();
    }
    
    /**
     * ActionListener, um einen Kunden zu gegebener ID mit einem Ajax-Request erneut zu laden, z.B. nach einem Update-Update-Konflikt
     * @param id ID des neu zu ladenden Kundenobjekts
     */
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    public void reload(long id) {
        final Optional<? extends AbstractKunde> kundeOpt = kundenBroker.findById(id, NUR_KUNDE);
        if (kundeOpt.isPresent()) {
            kunde = kundeOpt.get();
        } else {
            messagesInstance.get().error(MSG_KEY_KUNDE_NOT_FOUND_BY_ID, localeInstance.get(), null, id);
        }
    }
    
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    public String editUser() {
        kunde = kundenBroker.findByLoginname(iamInstance.get().getLoginname().get(), MIT_BESTELLUNGEN).get();
        
        if (!conversation.isTransient()) {
            conversation.end();
        }
        conversation.begin();
        
        return kunde.getClass().equals(Privatkunde.class) ? JSF_UPDATE_PRIVATKUNDE : JSF_UPDATE_FIRMENKUNDE;
    }
}
