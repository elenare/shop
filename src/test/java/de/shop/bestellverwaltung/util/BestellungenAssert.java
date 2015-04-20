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
import java.net.URI;
import java.util.List;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class BestellungenAssert extends AbstractAssert<BestellungenAssert, List<Bestellung>> {
    private BestellungenAssert(List<Bestellung> actual) {
        super(actual, BestellungenAssert.class);
    }
    
    public static BestellungenAssert assertThatBestellungen(List<Bestellung> actual) {
        return new BestellungenAssert(actual);
    }

    public BestellungenAssert isNotEmpty() {
        assertThat(actual)
            .overridingErrorMessage("Die Liste der Bestellungen ist leer")
            .isNotEmpty();
        return this;        
    }

    public BestellungenAssert doesNotContainNull() {
        assertThat(actual)
            .overridingErrorMessage("Die Liste der Bestellungen darf nicht null enthalten")
            .doesNotContainNull();
        return this;        
    }
    

    public BestellungenAssert haveKundeUriEndingWith(long kundeId) {
        final String kundeIdStr = String.valueOf(kundeId);
		actual.stream()
              .map(Bestellung::getKundeUri)
              .map(URI::toString)
              .forEach(uri -> assertThat(uri)
                              .overridingErrorMessage("Die URI <%s> muss mit %s enden", uri, kundeIdStr)
                              .endsWith(kundeIdStr));
        return this;        
    }
}
