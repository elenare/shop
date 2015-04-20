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

package de.shop.bestellverwaltung.jsf;

import de.shop.bestellverwaltung.business.BestellungenBroker;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.business.KundenBroker;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.interceptor.Log;
import de.shop.util.jsf.RequestMap;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.picketlink.authorization.annotations.RolesAllowed;

import static de.shop.iam.domain.RolleType.KUNDE_STRING;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.MIT_BESTELLUNGEN;
import static de.shop.util.Constants.JSF_DEFAULT_ERROR;
import static java.util.logging.Level.FINEST;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Model
@Transactional
@Log
public class BestellungModel {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final String JSF_VIEW_BESTELLUNG = "/p/bestellverwaltung/viewBestellung";
    
    private Warenkorb warenkorb;
    private BestellungenBroker bestellungenBroker;
    private KundenBroker kundenBroker;
    private Map<String, Object> requestMap;
    
    private Bestellung bestellung;

    /**
     * Package-private Default-Konstruktor f&uuml;r JSF
     */
    BestellungModel() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param warenkorb zu injizierendes Objekt f&uuml;r Warenkorb
     * @param bestellungenBroker zu injizierendes Objekt f&uuml;r BestellungenBroker
     * @param kundenBroker zu injizierendes Objekt f&uuml;r KundenBroker
     * @param request zu injizierendes Objekt f&uuml;r HttpServletRequest
     */
    @Inject
    BestellungModel(Warenkorb warenkorb, BestellungenBroker bestellungenBroker,
                    KundenBroker kundenBroker, @RequestMap Map<String, Object> requestMap) {
        super();
        this.warenkorb = warenkorb;
        this.bestellungenBroker = bestellungenBroker;
        this.kundenBroker = kundenBroker;
        this.requestMap = requestMap;
    }
    
    

    public Bestellung getBestellung() {
        return bestellung;
    }

    @RolesAllowed(KUNDE_STRING)
    public String bestellen() {
        if (warenkorb.getPositionen() == null || warenkorb.getPositionen().isEmpty()) {
            // Darf nicht passieren, wenn der Button zum Bestellen verfuegbar ist
            return JSF_DEFAULT_ERROR;
        }
        
        // Den eingeloggten Kunden mit seinen Bestellungen ermitteln, und dann die neue Bestellung zu ergaenzen
        final AbstractKunde kunde = kundenBroker.findByLoginname(MIT_BESTELLUNGEN).get();
        // Aus dem Warenkorb nur Positionen mit Anzahl > 0
        final List<Bestellposition> neuePositionen = warenkorb.getPositionen()
                                                              .values()
                                                              .stream()
                                                              .filter(bp -> bp.getAnzahl() > 0)
                                                              .collect(toList());
        
        // Warenkorb zuruecksetzen
        warenkorb.endConversation();
        
        // Neue Bestellung mit neuen Bestellpositionen erstellen
        bestellung = new Bestellung(neuePositionen);
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Neue Bestellung: "+ bestellung
                          + "\nBestellpositionen: " +  bestellung.getBestellpositionen());
        }
        
        // Bestellung mit VORHANDENEM Kunden verknuepfen:
        // dessen Bestellungen muessen geladen sein, weil es eine bidirektionale Beziehung ist
        bestellung = bestellungenBroker.save(bestellung, kunde);
        warenkorb.remove();
        
        // Fuer die Darstellung der Bestellung im naechsten Request
        requestMap.put("bestellung", bestellung);
        
        // Redirect nicht notwendig, da der Warenkorb mittlerweile geleert ist
        return JSF_VIEW_BESTELLUNG;
    }
}
