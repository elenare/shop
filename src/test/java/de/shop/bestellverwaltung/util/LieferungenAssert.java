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
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.AbstractAssert;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class LieferungenAssert extends AbstractAssert<LieferungenAssert, Optional<List<Lieferung>>> {
    private LieferungenAssert(Optional<List<Lieferung>> actual) {
        super(actual, LieferungenAssert.class);
    }
    
    public static LieferungenAssert assertThatLieferungen(Optional<List<Lieferung>> actual) {
        return new LieferungenAssert(actual);
    }
    
    public LieferungenAssert arePresent() {
        assertThat(actual.isPresent())
            .overridingErrorMessage("Die Liste der Lieferungen muss vorhanden sein")
            .isTrue();
        return this;
    }
    
    public LieferungenAssert areNotEmpty() {
        assertThat(actual.get())
            .overridingErrorMessage("Die Liste der Lieferungen darf nicht leer sein")
            .isNotEmpty();
        return this;
    }
    
    public LieferungenAssert areNotPresent() {
        assertThat(actual.isPresent())
            .overridingErrorMessage("Es darf keine Liste mit Lieferungen vorhanden sein")
            .isFalse();
        return this;
    }
}
