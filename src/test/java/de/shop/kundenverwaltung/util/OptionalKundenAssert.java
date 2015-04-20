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
package de.shop.kundenverwaltung.util;

import de.shop.kundenverwaltung.domain.AbstractKunde;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class OptionalKundenAssert extends AbstractAssert<OptionalKundenAssert, Optional<List<AbstractKunde>>> {
    private OptionalKundenAssert(Optional<List<AbstractKunde>> actual) {
        super(actual, OptionalKundenAssert.class);
    }
    
    public static OptionalKundenAssert assertThatOptionalKunden(Optional<List<AbstractKunde>> actual) {
        return new OptionalKundenAssert(actual);
    }

    public OptionalKundenAssert isPresent() {
        assertThat(actual.isPresent())
            .overridingErrorMessage("Es gibt keine Liste von Kunden")
            .isTrue();
        return this;
    }

    public OptionalKundenAssert isNotPresent() {
        assertThat(actual.isPresent())
            .overridingErrorMessage("Es gibt eine Liste von Kunden, aber es darf keine geben")
            .isFalse();
        return this;
    }
}
