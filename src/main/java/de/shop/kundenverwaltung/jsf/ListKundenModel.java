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

import de.shop.iam.business.IdentityAccessManagement;
import de.shop.kundenverwaltung.business.KundenBroker;
import de.shop.kundenverwaltung.business.KundenBroker.OrderByType;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.Strings;
import de.shop.util.jsf.Client;
import de.shop.util.jsf.Messages;
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
import javax.validation.constraints.Pattern;
import org.picketlink.authorization.annotations.RolesAllowed;

import static de.shop.iam.domain.IdentityVO.NACHNAME_PATTERN;
import static de.shop.iam.domain.RolleType.ADMIN_STRING;
import static de.shop.iam.domain.RolleType.MITARBEITER_STRING;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.NUR_KUNDE;
import static de.shop.util.Constants.DEFAULT_PU;
import static javax.persistence.PersistenceContextType.EXTENDED;

/**
 * Ddie Suche nach mehreren Kunden und deren Auflistung
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@ViewScoped
// NICHT: passivationCapable = false wegen @ViewScoped
@Stateful
public class ListKundenModel implements Serializable {
    private static final long serialVersionUID = -4774968905910782920L;
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private static final String MSG_KEY_DELETE_KUNDE_BESTELLUNG = "kunde.deleteMitBestellung";
    private static final String MSG_KEY_DELETE_KUNDE_WARENKORB = "kunde.deleteMitWarenkorb";
    
    @Pattern(regexp = NACHNAME_PATTERN, message = "{identity.nachname.pattern}")
    private String nachname;
    
    private transient List<AbstractKunde> kunden;
    
    // fuer Selektion (Anklicken) in p:dataTable in listKunden.xhtml
    private transient AbstractKunde selectedKunde;

    // Extended PersistenceContext erforderlich, damit Bestellungen im Popup-Fenster angezeigt werden koennen
    @PersistenceContext(type = EXTENDED, unitName = DEFAULT_PU)
    private transient EntityManager em;

    private transient Instance<KundenBroker> kundenBrokerInstance;
    private transient Instance<IdentityAccessManagement> iamInstance;
    private transient Instance<Messages> messagesInstance;
    private transient Instance<Locale> localeInstance;

    
    /**
     * Default-Konstruktor f&uuml;r EJB
     */
    public ListKundenModel() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param kundenBrokerInstance zu injizierendes Instance-Objekt f&uuml;r KundenBroker
     * @param iamInstance zu injizierendes Instance-Objekt f&uuml;r IdentityAccessManagement
     * @param messagesInstance zu injizierendes Instance-Objekt f&uuml;r Messages
     * @param localeInstance zu injizierendes Instance-Objekt f&uuml;r Locale
     */
    @Inject
    ListKundenModel(Instance<KundenBroker> kundenBrokerInstance, Instance<IdentityAccessManagement> iamInstance,
                    Instance<Messages> messagesInstance, @Client Instance<Locale> localeInstance) {
        super();
        this.kundenBrokerInstance = kundenBrokerInstance;
        this.iamInstance = iamInstance;
        this.messagesInstance = messagesInstance;
        this.localeInstance = localeInstance;
    }

    @Remove
    private void remove() {
        if (kunden != null && !kunden.isEmpty()) {
            kunden.forEach(k -> em.detach(k));
        }
    }
    
    @Override
    public String toString() {
        return "ListKundeModel {nachname=" + nachname + '}';
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public List<AbstractKunde> getKunden() {
        return kunden;
    }

    public AbstractKunde getSelectedKunde() {
        return selectedKunde;
    }

    public void setSelectedKunde(AbstractKunde selectedKunde) {
        this.selectedKunde = selectedKunde;
    }

    /**
     * Action-Listener, um Kunden zu gegebenem Nachnamen mit einem Ajax-Request zu suchen
     */
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public void findKundenByNachname() {
        final KundenBroker kundenBroker = kundenBrokerInstance.get();
        final Optional<List<AbstractKunde>> kundenOpt = Strings.isNullOrEmpty(nachname)
                                                        ? kundenBroker.findAll(NUR_KUNDE, OrderByType.UNORDERED)
                                                        : kundenBroker.findByNachname(nachname, NUR_KUNDE);
       
        kunden = kundenOpt.orElse(Collections.emptyList());
    }
    
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public List<String> findNachnamenByPrefix(String nachnamePrefix) {
        return iamInstance.get().findNachnamenByPrefix(nachnamePrefix);
    }
}
