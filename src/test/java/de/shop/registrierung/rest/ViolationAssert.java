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
package de.shop.registrierung.rest;

import java.util.Collection;
import java.util.Optional;
import org.assertj.core.api.AbstractAssert;
import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ViolationAssert extends AbstractAssert<ViolationAssert, Collection<ResteasyConstraintViolation>> {
    private ViolationAssert(Collection<ResteasyConstraintViolation> actual) {
        super(actual, ViolationAssert.class);
    }
    
    public static ViolationAssert assertThatViolations(Collection<ResteasyConstraintViolation> actual) {
        return new ViolationAssert(actual);
    }

    public ViolationAssert haveAtLeastOneViolation() {
        assertThat(actual)
            .overridingErrorMessage("Es muss mindestens 1 Verletzung der Bean Validation geben")
            .isNotEmpty();
        return this;
    }
    
    public ViolationAssert havePasswordViolation() {
        final String expectedMsg = "Passwords are not equal.";
        final Optional<ResteasyConstraintViolation> violation = actual.stream()
                                                                      .filter(v -> v.getMessage().equals(expectedMsg))
                                                                      .findFirst();
        assertThat(violation.isPresent())
            .overridingErrorMessage("Der Meldung %s bei der Verletzung der beiden Passwoerter muss vorhanden sein", expectedMsg)
            .isTrue();
        final String violationValue = violation.get().getValue().toLowerCase();
        assertThat(violationValue)
            .overridingErrorMessage("Der Wert bei der Verletzung der beiden Passwoerter muss false sein, ist aber %s", violationValue)
            .isEqualTo(String.valueOf(false));
        
        return this;
    }
    
    public ViolationAssert haveAgbViolation() {
        final String expectedMsg = "The terms were not accepted.";
        final Optional<ResteasyConstraintViolation> violation = actual.stream()
                                                                      .filter(v -> v.getMessage().equals(expectedMsg))
                                                                      .findFirst();
        assertThat(violation.isPresent())
            .overridingErrorMessage("Der Meldung <%s> bei der Verletzung der AGB muss vorhanden sein", expectedMsg)
            .isTrue();
        final String violationValue = violation.get().getValue().toLowerCase();
        assertThat(violationValue)
            .overridingErrorMessage("Der Wert bei der Verletzung der AGB muss false sein, ist aber %s", violationValue)
            .isEqualTo(String.valueOf(false));
        
        return this;
    }
    
    
    public ViolationAssert havePlzViolation(String plz) {
        final String expectedMsg = "The ZIP code " + plz + " doesn't have 5 digits.";
        final Optional<ResteasyConstraintViolation> violation = actual.stream()
                                                                      .filter(v -> v.getMessage().equals(expectedMsg))
                                                                      .findFirst();
        assertThat(violation.isPresent())
            .overridingErrorMessage("Der Meldung <%s> bei der Verletzung der PLZ muss vorhanden sein", expectedMsg)
            .isTrue();
        assertThat(violation.get().getValue())
            .overridingErrorMessage("Der Wert bei der Verletzung der PLZ muss %s sein, ist aber %s", plz, violation.get().getValue())
            .isEqualTo(plz);
        
        return this;
    }
}
