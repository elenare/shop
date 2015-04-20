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
package de.shop.bestellverwaltung.util;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Lieferung;
import org.assertj.core.api.AbstractAssert;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class LieferungAssert extends AbstractAssert<LieferungAssert, Lieferung> {
    private LieferungAssert(Lieferung actual) {
        super(actual, LieferungAssert.class);
    }
    
    public static LieferungAssert assertThatLieferung(Lieferung actual) {
        return new LieferungAssert(actual);
    }

    public LieferungAssert hasLiefernrStartingWith(String lieferNrPraefix) {
        assertThat(actual.getLiefernr())
            .overridingErrorMessage("Die Liefernr. muss mit %s beginnen, ist aber %s", lieferNrPraefix, actual.getLiefernr())
            .startsWith(lieferNrPraefix);
        return this;
    }
    
    public LieferungAssert hasBestellungen() {
        assertThat(actual.getBestellungen())
            .overridingErrorMessage("Die Lieferung muss mindestens 1 Bestellung enthalten, enthaelt aber keine")
            .isNotEmpty();
        return this;
    }
    
    public LieferungAssert hasBestellungenMitKunde() {
        assertThat(actual.getBestellungen()
                         .stream()
			             .map(Bestellung::getKunde)
                         .collect(toList()))
            .overridingErrorMessage("Jede Bestellung der Lieferung muss einen Kunden haben")
            .doesNotContainNull();
        return this;
    }
}
