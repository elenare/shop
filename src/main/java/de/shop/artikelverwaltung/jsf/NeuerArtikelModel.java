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

import de.shop.artikelverwaltung.business.ArtikelBroker;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.interceptor.Log;
import de.shop.util.jsf.RequestMap;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import org.picketlink.authorization.annotations.RolesAllowed;

import static de.shop.iam.domain.RolleType.ADMIN_STRING;
import static de.shop.iam.domain.RolleType.MITARBEITER_STRING;


/**
 * Dialogsteuerung fuer createArtikel
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@ViewScoped
@Transactional
@Log
public class NeuerArtikelModel implements Serializable {
    private static final long serialVersionUID = 8721670566770108448L;

    private static final String OK_NEUER_ARTIKEL = "/p/artikelverwaltung/okNeuerArtikel";
    private static final String REQUEST_ARTIKEL_ID = "artikelId";
    
    // Field Injection: ArtikelBroker ist nicht serialisierbar,
    // weshalb transient wegen @ViewScoped notwendig ist. Kap. 6.6.5 der Spezifikation:
    // "If a managed bean which declares a passivating scope, ...:
    // * is not passivation capable,
    // * has an injection point that is not passivation capable,
    // * ...
    // then the container automatically detects the problem and treats it as a deployment problem."

    @Inject
    private transient ArtikelBroker artikelBroker;
    
    @Inject
    @RequestMap
    private transient Map<String, Object> requestMap;
    
    private transient Artikel artikel;

    /**
     * Package-private Default-Konstruktor f&uuml;r JSF
     */
    NeuerArtikelModel() {
        super();
    }

    @PostConstruct
    private void postConstruct() {
        artikel = new Artikel();
    }

    public Artikel getArtikel() {
        return artikel;
    }

    public void setArtikel(Artikel artikel) {
        this.artikel = artikel;
    }
    
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public String createArtikel() {
        artikelBroker.save(artikel);
        
        // Aufbereitung fuer okNeuerArtikel.xhtml
        requestMap.put(REQUEST_ARTIKEL_ID, artikel.getId());
        return OK_NEUER_ARTIKEL;
    }
    
    @Override
    public String toString() {
        return "NeuerArtikelModel {artikel=" + artikel + '}';
    }
}
