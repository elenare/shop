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

import de.shop.kundenverwaltung.business.KundenBroker;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.jsf.Client;
import de.shop.util.jsf.Messages;
import de.shop.util.persistence.File;
import de.shop.util.persistence.FileHelper;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.inject.Instance;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import org.picketlink.authorization.annotations.RolesAllowed;

import static de.shop.iam.domain.RolleType.ADMIN_STRING;
import static de.shop.iam.domain.RolleType.MITARBEITER_STRING;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.NUR_KUNDE;
import static de.shop.util.Constants.DEFAULT_PU;
import static de.shop.util.Strings.isNullOrEmpty;
import static java.util.logging.Level.FINEST;
import static java.util.stream.Collectors.toList;
import static javax.persistence.PersistenceContextType.EXTENDED;

/**
 * Anzeige eines Kunden
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@ViewScoped
@Stateful
public class ViewKundeModel implements Serializable {
    private static final long serialVersionUID = 6103980953494699310L;
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private static final String JSF_VIEW_KUNDE = "/p/kundenverwaltung/viewKunde";
    private static final String REQUEST_KUNDE_ID = "id";
    private static final String CLIENT_ID_KUNDEID = "kundeIdSuche";
    private static final String MSG_KEY_KUNDE_NOT_FOUND_BY_ID = "kunde.notFound.id";
    
    private String kundeIdStr;
    private transient List<String> kundenIds;
    private transient AbstractKunde kunde;
    
    // Extended PersistenceContext erforderlich, damit Bestellungen in einem neuen Tab angezeigt werden koennen
    @PersistenceContext(type = EXTENDED, unitName = DEFAULT_PU)
    private transient EntityManager em;
    
    private transient Instance<KundenBroker> kundenBrokerInstance;
    private transient Instance<HttpServletRequest> requestInstance;
    private transient Instance<Messages> messagesInstance;
    private transient Instance<Locale> localeInstance;
    private transient Instance<FileHelper> fileHelperInstance;
    
    /**
     * Default-Konstruktor f&uuml;r EJB
     */
    public ViewKundeModel() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param kundenBrokerInstance zu injizierendes Instance-Objekt f&uuml;r KundenBroker
     * @param requestInstance zu injizierendes Instance-Objekt f&uuml;r HttpServletRequest
     * @param messagesInstance zu injizierendes Instance-Objekt f&uuml;r Messages
     * @param localeInstance zu injizierendes Instance-Objekt f&uuml;r Locale
     * @param fileHelperInstance zu injizierendes Instance-Objekt f&uuml;r FileHelper
     */
    @Inject
    ViewKundeModel(Instance<KundenBroker> kundenBrokerInstance,
                   Instance<HttpServletRequest> requestInstance,
                   Instance<Messages> messagesInstance,
                   @Client Instance<Locale> localeInstance,
                   Instance<FileHelper> fileHelperInstance) {
        this.kundenBrokerInstance = kundenBrokerInstance;
        this.requestInstance = requestInstance;
        this.messagesInstance = messagesInstance;
        this.localeInstance = localeInstance;
        this.fileHelperInstance = fileHelperInstance;
    }

    @Remove
    private void remove() {
        if (kunde != null) {
            em.detach(kunde);
        }
    }
    
    @Override
    public String toString() {
        return "ViewKundeModel {kundeIdStr=" + kundeIdStr + '}';
    }
    
    public String getKundeIdStr() {
        return kundeIdStr;
    }

    public void setKundeIdStr(String kundeIdStr) {
        this.kundeIdStr = kundeIdStr;
    }

    public AbstractKunde getKunde() {
        return kunde;
    }

    /**
     * ActionListener, um einen Kunden zu gegebener ID mit einem Ajax-Request zu suchen
     */
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public void findKundeById() {
        if (isNullOrEmpty(kundeIdStr)) {
            return;
        }
        
        long kundeId;
        try {
            kundeId = Long.parseLong(kundeIdStr);
        } catch (NumberFormatException e) {
            findKundeByIdErrorMsg(kundeIdStr);
            return;
        }
        
        final Optional<? extends AbstractKunde> kundeOpt = kundenBrokerInstance.get().findById(kundeId, NUR_KUNDE);
        if (!kundeOpt.isPresent()) {
            findKundeByIdErrorMsg(String.valueOf(kundeId));
            return;
        }

        kunde = kundeOpt.get();
        if (kunde.getFile() != null) {
            // nachladen
            kunde.getFile().getId();
        }
    }
    

    private void findKundeByIdErrorMsg(String id) {
        messagesInstance.get().error(MSG_KEY_KUNDE_NOT_FOUND_BY_ID, localeInstance.get(), CLIENT_ID_KUNDEID, id);
    }
    
    /**
     * Fuer rich:autocomplete
     * @param idPrefix Praefix fuer potenzielle Kunden-IDs
     * @return Liste der potenziellen Kunden
     */
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public List<String> findIdsByPrefix(String idPrefix) {
        if (isNullOrEmpty(idPrefix)) {
            return Collections.emptyList();
        }
        
        long id; 
        try {
            id = Long.parseLong(idPrefix);
        } catch (NumberFormatException e) {
            findKundeByIdErrorMsg(idPrefix);
            return Collections.emptyList();
        }
        
        final List<? extends AbstractKunde> kundenPrefix = kundenBrokerInstance.get().findByIdPrefix(id);
        if (kundenPrefix == null || kundenPrefix.isEmpty()) {
            // Kein Kunde zu gegebenem ID-Praefix vorhanden
            findKundeByIdErrorMsg(idPrefix);
            return Collections.emptyList();
        }
        
        kundenIds = kundenPrefix.stream()
                                .map(AbstractKunde::getId)
                                .map(String::valueOf)
                                .collect(toList());
        return kundenIds;
    }
    
    /**
     * Fuer viewKunde?kundeId=KUNDE_ID, d.h. die Kunde-ID wird als Request-Parameter uebergeben, z.B. fuer Bookmarks
     */
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public void load() {
        // Request-Parameter "kundeId" fuer ID des gesuchten Kunden
        final String paramKundeIdStr = requestInstance.get().getParameter(REQUEST_KUNDE_ID);
        if (isNullOrEmpty(paramKundeIdStr)) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.finest("Der Parameter kundeId ist nicht gesetzt");
            }
            // NICHT kunde auf null setzen, weil ein Kunde auch ueber das Suchformular gesucht werden kann
            return;
        }
        
        long kundeId;
        try {
            kundeId = Long.parseLong(paramKundeIdStr);
        } catch (NumberFormatException e) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.finest("Ungueltige kundeId = " + paramKundeIdStr);
            }
            
            kunde = null;
            return;
        }
        
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("kundeId = " + kundeId);
        }
       
        kunde = kundenBrokerInstance.get().findById(kundeId, NUR_KUNDE).orElse(null);
        //final Optional<AbstractKunde> kundeOpt = kundenBroker.findById(kundeId, MIT_BESTELLUNGEN).orElse(null);   //NOSONAR
        
        if (kunde != null) {
            kundeIdStr = paramKundeIdStr;
        }
    }

    /**
     * Fuer das Kontextmenu in /listKunden
     * @param kundeId ID des Kunden
     * @return Pfad zu /viewKunde
     */
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public String load(long kundeId) {
        kunde = kundenBrokerInstance.get().findById(kundeId, NUR_KUNDE).orElse(null);
        return JSF_VIEW_KUNDE;
    }
    
    public void reset() {
        kundeIdStr = null;
        if (kunde != null) {
            em.detach(kunde);
            kunde = null;
        }
    }
    
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public String getFilename(File file) {
        if (file == null) {
            return "";
        }
        
        fileHelperInstance.get().store(file);
        return file.getFilename();
    }
}
