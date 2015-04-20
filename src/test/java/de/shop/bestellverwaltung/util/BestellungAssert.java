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
import de.shop.kundenverwaltung.domain.AbstractKunde;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class BestellungAssert extends AbstractAssert<BestellungAssert, Bestellung> {
    private BestellungAssert(Bestellung actual) {
        super(actual, BestellungAssert.class);
    }
    
    public static BestellungAssert assertThatBestellung(Bestellung actual) {
        return new BestellungAssert(actual);
    }

    @Override
    public BestellungAssert isNotNull() {
        assertThat(actual)
            .overridingErrorMessage("Die Bestellung muss existieren, ist aber null")
            .isNotNull();
        return this;        
    }
    
    public BestellungAssert hasPositiveId() {
        assertThat(actual.getId())
            .overridingErrorMessage("Die ID der Bestellung muss positiv sein, ist aber %d", actual.getId())
            .isPositive();
        return this;
    }

    public BestellungAssert hasId(long id) {
        assertThat(actual.getId())
            .overridingErrorMessage("Die ID der Bestellung muss %d sein, ist aber %d", id, actual.getId())
            .isEqualTo(id);
        return this;
    }

    public BestellungAssert hasBestellpositionen() {
        assertThat(actual.getBestellpositionen())
            .overridingErrorMessage("Die Liste der Bestellpositionen darf nicht leer sein")
            .isNotEmpty();
        return this;
    }
    
    public <K extends AbstractKunde> BestellungAssert hasKunde(K kunde) {
        final AbstractKunde actualKunde = actual.getKunde();
        assertThat(actualKunde)
            .overridingErrorMessage("Zur Bestellung muss ein Kunde existieren, er ist aber null")
            .isNotNull();
        
        assertThat(actualKunde.getId())
            .overridingErrorMessage("Die ID des Kunden mit der Bestellung muss %d sein, ist aber %d", kunde.getId(), actualKunde.getId())
            .isEqualTo(kunde.getId());
        
        assertThat(actualKunde.getBestellungen())
            .overridingErrorMessage("Der Kunde mit der ID %d darf die Bestellung nur 1-mal enthalten", actualKunde.getId())
            .containsOnlyOnce(actual);
        
        return this;
    }
}
