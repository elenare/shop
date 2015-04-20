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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.transaction.Transactional;



/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Model
@Transactional
@Log
public class NichtBestellteArtikelModel {
    private static final int ANZAHL_NICHT_BESTELLT = 5;
    private static final String NICHT_BESTELLT = "nichtBestellt";

    private KatalogBroker katalogBroker;
    private Map<String, Object> sessionMap;

    /**
     * Package-private Default-Konstruktor f&uuml;r JSF
     */
    NichtBestellteArtikelModel() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param katalogBroker zu injizierendes Objekt f&uuml;r KatalogBroker
     * @param sessionMap zu injizierende Map mit den Objekten in der HttpSession
     */
    @Inject
    NichtBestellteArtikelModel(KatalogBroker katalogBroker, @SessionMap Map<String, Object> sessionMap) {
        super();
        this.katalogBroker = katalogBroker;
        this.sessionMap = sessionMap;
    }

    public void load() {
        @SuppressWarnings("unchecked")
        List<Artikel> nichtBestellt = (List<Artikel>) sessionMap.get(NICHT_BESTELLT);
        if (nichtBestellt != null) {
            // bereits geladen
            return;
        }
        
        final Optional<List<Artikel>> nichtBestelltOpt = katalogBroker.findNichtBestellt(ANZAHL_NICHT_BESTELLT);
        if (!nichtBestelltOpt.isPresent()) {
            // nichts gefunden
            return;
        }
        
        nichtBestellt = nichtBestelltOpt.get();
        sessionMap.put(NICHT_BESTELLT, new ArrayList<>(nichtBestellt));
    }
}
