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

import de.shop.artikelverwaltung.business.KatalogBroker;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.util.interceptor.Log;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.tagcloud.TagCloudItem;

import static java.util.logging.Level.FINEST;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@ConversationScoped
@Log
public class Warenkorb implements Serializable {
    private static final long serialVersionUID = -1981070683990640854L;

    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private static final String JSF_VIEW_WARENKORB = "/p/katalog/viewWarenkorb?init=true";
    
    // Field Injection, weil transient wegen @ConversationScoped notwendig ist,
    // aber bei Constructor Injection nicht erkannt wird.
    
    @Inject
    private transient Conversation conversation;
    
    @Inject
    private transient Instance<KatalogBroker> katalogBrokerInstance;

    private transient Map<Long, Bestellposition> positionen;
    
    // fuer selectArtikel.xhtml
    private transient Artikel artikel;
    
    /**
     * Package-private Default-Konstruktor f&uuml;r JSF
     */
    Warenkorb() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param conversation injiziertes Objekt fuer CDI-Conversation
     * @param katalogBrokerInstance injiziertes Instance-Objekt fuer KatalogBroker
     */
    Warenkorb(Conversation conversation, Instance<KatalogBroker> katalogBrokerInstance) {
        super();
        this.conversation = conversation;
        this.katalogBrokerInstance = katalogBrokerInstance;
    }

    public Map<Long, Bestellposition> getPositionen() {
        return positionen;
    }
        
    public void setArtikel(Artikel artikel) {
        this.artikel = artikel;
    }

    public Artikel getArtikel() {
        return artikel;
    }

    @Override
    public String toString() {
        return "Warenkorb " + positionen;
    }
    
    /**
     * Den selektierten Artikel zum Warenkorb hinzufuergen
     * @param artikel Der selektierte Artikel
     * @return Pfad zur Anzeige des aktuellen Warenkorbs
     */
    public String add(Artikel artikel) {
        if (artikel == null) {
            LOGGER.warning("Zum Warenkorb soll null hinzugefuegt werden.");
            return null;
        }
        
        Bestellposition bp = null;
        
        if (positionen == null) {
            // neuer Warenkorb
            positionen = new HashMap<>();
        } else {
            // Warenkorb existiert: ist der Artikel bereits enthalten?
            bp = positionen.get(artikel.getId());
        }
        
        if (bp == null) {
            // neuer Artikel fuer den (evtl. neuen) Warenkorb
            bp = new Bestellposition(artikel);
            positionen.put(artikel.getId(), bp);
        } else {
            // bereits im Warenkorb
            final int vorhandeneAnzahl = bp.getAnzahl();
            bp.setAnzahl(vorhandeneAnzahl + 1);
        }
        
        return JSF_VIEW_WARENKORB;
    }
    
    /**
     * Den selektierten Artikel zum Warenkorb hinzufuergen
     * @return Pfad zur Anzeige des aktuellen Warenkorbs
     */
    public String add() {
        final String outcome = add(artikel);
        artikel = null;
        return outcome;
    }
        
    public void addCloud(SelectEvent event) {
        final TagCloudItem item = (TagCloudItem) event.getObject();
        final String artikelBezeichnung = item.getLabel();
        
        final Optional<Artikel> artikelOpt = katalogBrokerInstance.get().findByBezeichnungExakt(artikelBezeichnung);
        if (!artikelOpt.isPresent()) {
            LOGGER.warning("Kein Artikel mit dem Cloud-Label " + artikelBezeichnung + " gefunden.");
            return;
        }
        
        add(artikelOpt.get());
    }
    
    public void endConversation() {
        conversation.end();
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Conversation beendet");
        }
    }
    
    /**
     * Eine potenzielle Bestellposition entfernen
     * @param artikelId Artikel-ID zur zu entfernenden Bestellposition
     */
    public void remove(Long artikelId) {
        positionen.remove(artikelId);
        if (positionen.isEmpty()) {
            endConversation();
        }
    }
    
    public void remove() {
        positionen = null;
    }

    // ActionListener, damit die set-Methode fuer ggf. geaenderte Anzahl aufgerufen wird
    public void apply() {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Positionen: " + positionen);
        }
    }
}
