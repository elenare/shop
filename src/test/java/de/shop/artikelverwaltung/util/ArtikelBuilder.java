/*
 * Copyright (C) 2015 Juergen Zimmermann, Hochschule Karlsruhe
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
package de.shop.artikelverwaltung.util;

import de.shop.artikelverwaltung.domain.Artikel;
import java.math.BigDecimal;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ArtikelBuilder {
    private final Artikel artikel = new Artikel();
    
    public ArtikelBuilder bezeichnung(String bezeichnung) {
        artikel.setBezeichnung(bezeichnung);
        return this;
    }
    
    public ArtikelBuilder preis(BigDecimal preis) {
        artikel.setPreis(preis);
        return this;
    }
    
    public ArtikelBuilder rating(int rating) {
        artikel.setRating(rating);
        return this;
    }
    
    public ArtikelBuilder ausgesondert(boolean ausgesondert) {
        artikel.setAusgesondert(ausgesondert);
        return this;
    }
    
    public Artikel build() {
        return artikel;
    }
}
