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
import de.shop.kundenverwaltung.domain.Reklamation;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.AbstractAssert;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ReklamationenAssert extends AbstractAssert<ReklamationenAssert, Optional<List<Reklamation>>> {
    private ReklamationenAssert(Optional<List<Reklamation>> actual) {
        super(actual, ReklamationenAssert.class);
    }
    
    public static ReklamationenAssert assertThatReklamationen(Optional<List<Reklamation>> actual) {
        return new ReklamationenAssert(actual);
    }

    public ReklamationenAssert isNotEmpty() {
        assertThat(actual.get())
            .overridingErrorMessage("Es gibt keine Reklamationen")
            .isNotEmpty();
        return this;
    }

    public ReklamationenAssert notExisting() {
        assertThat(actual.isPresent())
            .overridingErrorMessage("Es gibt keine Reklamationen")
            .isFalse();
        return this;
    }

    public ReklamationenAssert eachHasKunde(AbstractKunde kunde) {
		assertThat(actual.get()
                         .stream()
		                 .map(Reklamation::getKunde)
                         .collect(toList()))
            .overridingErrorMessage("Jede Reklamation muss zum Kunden mit der ID %d gehoeren", kunde.getId())
            .containsOnly(kunde);
        return this;
    }
}
