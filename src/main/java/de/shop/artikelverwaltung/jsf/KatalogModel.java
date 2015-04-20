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

package de.shop.artikelverwaltung.jsf;

import de.shop.artikelverwaltung.business.KatalogBroker;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.interceptor.Log;
import de.shop.util.jsf.SessionMap;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudModel;

import static de.shop.util.Constants.BESTELLVORGANG_TIMEOUT;
import static de.shop.util.Constants.TIMEOUT_UNIT;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;


/**
 * Dialogsteuerung fuer listArtikel und selectArtikel
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@ConversationScoped
@Transactional
@Log
public class KatalogModel implements Serializable {
    private static final long serialVersionUID = 1564024850446471639L;
    
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final String JSF_SELECT_ARTIKEL = "/p/katalog/selectArtikel";
    
    // DI: KatalogBroker ist nicht serialisierbar, weshalb transient
    // wegen @ConversationScoped notwendig ist. Kap. 6.6.5 der Spezifikation:
    // "If a managed bean which declares a passivating scope, ...:
    // * is not passivation capable,
    // * has an injection point that is not passivation capable,
    // * ...
    // then the container automatically detects the problem and treats it as a deployment problem."
    // transient wiederum erfordert Field Injection statt Constructor Injection
    
    @Inject
    private transient KatalogBroker katalogBroker;
    
    @Inject
    private transient Conversation conversation;
    
    @Inject
    @SessionMap
    private transient Instance<Map<String, Object>> sessionMap;

    private String bezeichnung;
    private transient List<Artikel> artikel;
    private transient List<Artikel> verfuegbareArtikel;
    
    private TagCloudModel cloudModel;
    
    /**
     * Package-private Default-Konstruktor f&uuml;r JSF
     */
    KatalogModel() {
        super();
    }
    
    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public List<Artikel> getArtikel() {
        return artikel;
    }

    public List<Artikel> getVerfuegbareArtikel() {
        return verfuegbareArtikel;
    }

    public TagCloudModel getCloudModel() {
        return cloudModel;
    }

    private void beginConversation() {
        if (!conversation.isTransient()) {
            if (LOGGER.isLoggable(FINER)) {
                LOGGER.finer("Die Conversation ist bereits gestartet");
            }
            return;
        }
        
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("Neue Conversation wird gestartet");
        }
        conversation.begin();
        conversation.setTimeout(TIMEOUT_UNIT.toMillis(BESTELLVORGANG_TIMEOUT));
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Neue Conversation beginnt");
        }
    }
    
    public void findByBezeichnung() {
        beginConversation();
        artikel = katalogBroker.findByBezeichnung(bezeichnung)
                               .orElse(null);
        
        if (artikel == null) {
            cloudModel = null;
        } else {
            cloudModel = new DefaultTagCloudModel();
            artikel.forEach(a -> cloudModel.addTag(new DefaultTagCloudItem(a.getBezeichnung(), a.getRating())));
        }

    }
    
    // fuer selectArtikel.xhtml
    public String loadVerfuegbareArtikel() {
        beginConversation();
        if (verfuegbareArtikel != null) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, "Verfuegbare Artikel bereits ermittelt");
            }
            return JSF_SELECT_ARTIKEL;
        }
        
        final Optional<List<Artikel>> tmpVerfuegbareArtikel = katalogBroker.findVerfuegbareArtikel();
        if (!tmpVerfuegbareArtikel.isPresent()) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, "Keine verfuegbaren Artikel gefunden");
            }
            return JSF_SELECT_ARTIKEL;
        }
        verfuegbareArtikel = tmpVerfuegbareArtikel.get();
        
        // In der Session speichern: fuer ArtikelConverter
        final Map<Long, Artikel> verfuegbareArtikelMap = new HashMap<>();
        tmpVerfuegbareArtikel.get()
                             .stream()
                             .forEach(a -> verfuegbareArtikelMap.put(a.getId(), a));
        sessionMap.get().put("verfuegbareArtikel", verfuegbareArtikelMap);
        
        return JSF_SELECT_ARTIKEL;
    }
    
    @Override
    public String toString() {
        return "KatalogModel {bezeichnung=" + bezeichnung + ", artikel=" + artikel
                + ", verfuegbareArtikel=" + verfuegbareArtikel + '}';
    }
}
