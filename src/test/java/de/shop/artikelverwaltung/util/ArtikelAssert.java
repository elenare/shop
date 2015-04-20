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
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ArtikelAssert extends AbstractAssert<ArtikelAssert, Artikel> {
    private ArtikelAssert(Artikel actual) {
        super(actual, ArtikelAssert.class);
    }
    
    public static ArtikelAssert assertThatArtikel(Artikel actual) {
        return new ArtikelAssert(actual);
    }

    public ArtikelAssert hasPositiveId() {
        assertThat(actual.getId())
            .overridingErrorMessage("Die ID des Artikels muss positiv sein, ist aber %d", actual.getId())
            .isPositive();
        return this;
    }

    public ArtikelAssert hasId(long id) {
        assertThat(actual.getId())
            .overridingErrorMessage("Die ID des Artikels muss %d sein, ist aber %d", id, actual.getId())
            .isEqualTo(id);
        return this;
    }

    public ArtikelAssert hasBezeichnung(String bezeichnung) {
        assertThat(actual.getBezeichnung())
            .overridingErrorMessage("Die Bezeichnung des Artikels muss %s sein, ist aber %s", bezeichnung, actual.getBezeichnung())
            .isEqualTo(bezeichnung);
        return this;
    }

    public ArtikelAssert hasPreis(BigDecimal preis) {
        assertThat(actual.getPreis())
            .overridingErrorMessage("Der Preis des Artikels muss %f sein, ist aber %f", preis, actual.getPreis())
            .isEqualTo(preis);
        return this;
    }

    public ArtikelAssert hasRating(int rating) {
        assertThat(actual.getRating())
            .overridingErrorMessage("Die Bewertung des Artikels muss %d sein, ist aber %d", rating, actual.getRating())
            .isEqualTo(rating);
        return this;
    }
    
    public ArtikelAssert isNichtAusgesondert() {
        assertThat(actual.isAusgesondert())
            .overridingErrorMessage("Der Artikel darf nicht ausgesondert sein")
            .isFalse();
        return this;
    }
    
    public ArtikelAssert isEqualTo(Artikel other) {
        assertThat(actual)
            .overridingErrorMessage("Der Artikel muss %s sein, ist aber %s", other, actual)
            .isEqualTo(other);
        return this;
    }
}
