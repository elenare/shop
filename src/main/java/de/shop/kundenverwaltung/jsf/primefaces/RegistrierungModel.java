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

import de.shop.iam.domain.Adresse;
import de.shop.iam.domain.IdentityVO;
import de.shop.kundenverwaltung.business.EmailExistsException;
import de.shop.kundenverwaltung.business.LoginnameExistsException;
import de.shop.kundenverwaltung.business.RegistrierungBroker;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.util.AbstractShopException;
import de.shop.util.ShopRuntimeException;
import de.shop.util.interceptor.Log;
import de.shop.util.jsf.Client;
import de.shop.util.jsf.Messages;
import de.shop.util.jsf.RequestMap;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import org.jboss.weld.experimental.ExperimentalEvent;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import static de.shop.kundenverwaltung.jsf.primefaces.RegistrierungPush.REGISTRIERUNG_ENDPOINT;
import static java.util.logging.Level.FINER;
import static java.util.stream.Collectors.toSet;

/**
 * Registrierung eines neuen Kunden
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@ViewScoped
@Transactional
@Log
public class RegistrierungModel implements Serializable {
    private static final long serialVersionUID = 2090649500038653979L;
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private static final String CLIENT_ID_CREATE_EMAIL = "email";
    private static final String MSG_KEY_EMAIL_EXISTS = "kunde.emailExists";
    
    private static final String CLIENT_ID_LOGINNAME = "loginname";
    private static final String MSG_KEY_USER_EXISTS = "kunde.loginnameExists";
    
    private static final String OK = "/p/registrierung/okRegistrierung";
    private static final String REQUEST_KUNDE_ID = "kundeId";
    private static final String REQUEST_LOGINNAME = "loginname";
    
    private transient Instance<RegistrierungBroker> registrierungBrokerInstance;
    private transient Instance<Map<String, Object>> requestMapInstance;
    private transient Instance<Messages> messageInstance;
    private transient Instance<Locale> localeInstance;
    private transient Instance<Event<String>> neuerKundeEventInstance;

    private transient List<String> hobbys;
    private transient Privatkunde neuerPrivatkunde;
    
    // fuer Push
    private EventBus eventBus;
    
    /**
     * Package-private Default-Konstruktor f&uuml;r JSF
     */
    RegistrierungModel() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param registrierungBrokerInstance zu injizierendes Instance-Objekt f&uuml;r RegistrierungBroker
     * @param requestInstance zu injizierendes Instance-Objekt f&uuml;r HttpServletRequest
     * @param messageInstances zu injizierendes Instance-Objekt f&uuml;r Messages
     * @param localeInstance zu injizierendes Instance-Objekt f&uuml;r Locale
     * @param neuerKundeEventInstance zu injizierendes Instance-Objekt f&uuml;r Event
     */
    @Inject
    RegistrierungModel(Instance<RegistrierungBroker> registrierungBrokerInstance,
                       @RequestMap Instance<Map<String, Object>> requestMapInstance,
                       Instance<Messages> messageInstances,
                       @Client Instance<Locale> localeInstance,
                       Instance<Event<String>> neuerKundeEventInstance) {
        super();
        this.registrierungBrokerInstance = registrierungBrokerInstance;
        this.requestMapInstance = requestMapInstance;
        this.messageInstance = messageInstances;
        this.localeInstance = localeInstance;
        this.neuerKundeEventInstance = neuerKundeEventInstance;
        
        // Kein CDI-Producer, weil die vom Interface EventBus abgeleitete Klasse nicht Serializable ist
        eventBus = EventBusFactory.getDefault().eventBus();
    }

    @Override
    public String toString() {
        return "RegistrierungModel {neuerPrivatkunde=" + neuerPrivatkunde + '}';
    }
    
    public List<String> getHobbys() {
        return hobbys;
    }
    
    public void setHobbys(List<String> hobbys) {
        this.hobbys = hobbys;
    }

    public Privatkunde getNeuerPrivatkunde() {
        return neuerPrivatkunde;
    }

    public String registrierePrivatkunde() {
        // Liste von Strings als Set von Enums konvertieren
        final Set<HobbyType> hobbysPrivatkunde = hobbys.stream()
                                                       .map(HobbyType::valueOf)
                                                       .collect(toSet());
        neuerPrivatkunde.setHobbys(hobbysPrivatkunde);
        neuerPrivatkunde.setSeit(new Date());
        try {
            neuerPrivatkunde = registrierungBrokerInstance.get().save(neuerPrivatkunde);
        } catch (EmailExistsException | LoginnameExistsException e) {
            if (LOGGER.isLoggable(FINER)) {
                LOGGER.log(FINER, e.getMessage(), e);
            }
            return registrierenErrorMsg(e);
        }

        // Push-Event fuer Webbrowser
        eventBus.publish(REGISTRIERUNG_ENDPOINT, String.valueOf(neuerPrivatkunde.getId()));
        
        // Aufbereitung fuer okRegistrierung.xhtml
        final Map<String, Object> requestMap = requestMapInstance.get();
        requestMap.put(REQUEST_LOGINNAME, neuerPrivatkunde.getLoginname());
        requestMap.put(REQUEST_KUNDE_ID, neuerPrivatkunde.getId());
        
        // zuruecksetzen
        neuerPrivatkunde = null;
        hobbys = null;
        
        return OK;
    }

    private String registrierenErrorMsg(AbstractShopException e) {
        final Messages messages = messageInstance.get();
        final Locale locale = localeInstance.get();
        
        final Class<?> exceptionClass = e.getClass();
        if (EmailExistsException.class.equals(exceptionClass)) {
            @SuppressWarnings("ThrowableResultIgnored")
            final EmailExistsException e2 = EmailExistsException.class.cast(e);
            messages.error(MSG_KEY_EMAIL_EXISTS, locale, CLIENT_ID_CREATE_EMAIL, e2.getEmail());
        } else if (LoginnameExistsException.class.equals(exceptionClass)) {
            @SuppressWarnings("ThrowableResultIgnored")
            final LoginnameExistsException e2 = LoginnameExistsException.class.cast(e);
            messages.error(MSG_KEY_USER_EXISTS, locale, CLIENT_ID_LOGINNAME, e2.getLoginName());
        } else {
            throw new ShopRuntimeException(e);
        }
        
        return null;
    }

    public void createEmptyPrivatkunde() {
        if (neuerPrivatkunde != null) {
            return;
        }

        final Adresse adresse = new Adresse();
        final IdentityVO identity = new IdentityVO();
        identity.setAdresse(adresse);
        neuerPrivatkunde = new Privatkunde();
        neuerPrivatkunde.setIdentity(identity);
        neuerPrivatkunde.setSeit(new java.util.Date());
        
        final int anzahlHobbys = HobbyType.values().length;
        hobbys = new ArrayList<>(anzahlHobbys);
    }
}
