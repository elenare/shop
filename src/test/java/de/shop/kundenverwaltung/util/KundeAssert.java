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

import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.FamilienstandType;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class KundeAssert extends AbstractAssert<KundeAssert, AbstractKunde> {
    private KundeAssert(AbstractKunde actual) {
        super(actual, KundeAssert.class);
    }
    
    public static KundeAssert assertThatKunde(AbstractKunde actual) {
        return new KundeAssert(actual);
    }

    public KundeAssert hasPositiveId() {
        assertThat(actual.getId())
            .overridingErrorMessage("Die ID des Kunden muss positiv sein, ist aber %d", actual.getId())
            .isPositive();
        return this;
    }

    public KundeAssert hasGreaterIdThan(List<? extends AbstractKunde> kunden) {
        kunden.stream()
              .forEach(k -> assertThat(actual.getId())
                                .overridingErrorMessage("Die ID des Kunden muss groesser als %d sein, ist aber %d", k.getId(), actual.getId())
                                .isGreaterThan(k.getId())                 
              );
        return this;
    }
    
    public KundeAssert hasId(long id) {
        assertThat(actual.getId())
            .overridingErrorMessage("Die ID des Kunden muss %d sein, ist aber %d", id, actual.getId())
            .isEqualTo(id);
        return this;
    }
    
    public KundeAssert hasVersionGreaterThant(int version) {
        assertThat(actual.getVersion())
            .overridingErrorMessage("Die Versionsnummer muss mindestens %d sein, ist aber %d", version, actual.getVersion())
            .isGreaterThan(version);
        return this;
    }

    public KundeAssert hasNewerTimestampThan(Date datumVorher) {
        assertThat(actual.getErzeugt().getTime())
            .overridingErrorMessage("Der Erzeugungszeitpunkt muss groesser als %s sein, ist aber %s", datumVorher, actual.getErzeugt())
            .isGreaterThanOrEqualTo(datumVorher.getTime());
        return this;
    }

    @Override
    public KundeAssert isExactlyInstanceOf(Class<?> clazz) {
        assertThat(actual)
            .overridingErrorMessage("Das Kundenobjekt muss die Klasse %s haben, hat aber %s", clazz.getName(), actual.getClass().getName())
            .isExactlyInstanceOf(clazz);
        return this;
    }
    
    public KundeAssert hasIdentity() {
        assertThat(actual.getIdentity())
            .overridingErrorMessage("Zum Kundenobjekt muss es ein Identity-Objekt geben, es ist aber null")
            .isNotNull();
        return this;
    }
    
    public KundeAssert hasLoginname(String loginname) {
        assertThat(actual.getLoginname())
            .overridingErrorMessage("Zum Kundenobjekt muss es den Loginnamen %s geben, es ist aber %s", loginname, actual.getLoginname())
            .isEqualTo(loginname);
        return this;
    }
    
    public KundeAssert hasLoginname() {
        assertThat(actual.getLoginname())
            .overridingErrorMessage("Zum Kundenobjekt muss es einen Loginnamen geben")
            .isNotEmpty();
        return this;
    }
    
    public KundeAssert hasNoLoginname() {
        assertThat(actual.getLoginname())
            .overridingErrorMessage("Zum Kundenobjekt darf es keinen Loginnamen geben")
            .isNull();
        return this;
    }
    
    public KundeAssert hasLoginnameInIdentity(String loginname) {
        final String actualLoginname = actual.getIdentity().getLoginname();
        assertThat(actualLoginname)
            .overridingErrorMessage("Zum Kundenobjekt muss es den Loginnamen %s geben, es ist aber %s", loginname, actualLoginname)
            .isEqualTo(loginname);
        return this;
    }
    
    public KundeAssert hasLoginnameInIdentity() {
        final String actualLoginname = actual.getIdentity().getLoginname();
        assertThat(actualLoginname)
            .overridingErrorMessage("Zum Kundenobjekt muss es einen Loginnamen im Identity-Objekt geben")
            .isNotNull();
        return this;
    }
    
    public KundeAssert hasNachname(String nachname) {
        final String actualNachname = actual.getIdentity().getNachname();
        assertThat(actualNachname)
            .overridingErrorMessage("Zum Kundenobjekt muss es ein Identity-Objekt mit dem Nachnamen %s geben, es ist aber %s", nachname, actualNachname)
            .isEqualTo(nachname);
        return this;
    }
    
    public KundeAssert hasAdresse() {
        assertThat(actual.getIdentity().getAdresse())
            .overridingErrorMessage("Zum Kundenobjekt muss es ein Identity-Objekt mit einem Adresse-Objekt geben, aber das Adresse-Objekt null")
            .isNotNull();
        return this;
    }

    public KundeAssert hasAgbAkzeptiert() {
        assertThat(actual.isAgbAkzeptiert())
            .overridingErrorMessage("Beim Kundenobjekt muss agbAKzeptiert true sein")
            .isTrue();
        return this;
    }
    
    public KundeAssert hasBemerkungen(String bemerkungen) {
        assertThat(actual.getBemerkungen())
            .overridingErrorMessage("Die Bemerkungen muessen <%s> sein, sind aber <%s>", bemerkungen, actual.getBemerkungen())
            .isEqualTo(bemerkungen);
        return this;
    }
    
    public KundeAssert hasBestellungenUri() {
        assertThat(actual.getBestellungen())
            .overridingErrorMessage("Die URI fuer die Bestellungen ist null")
            .isNotNull();
        return this;
    }

    public KundeAssert hasMindestBestellmenge(int min) {
        int bestellmenge = 0;
        final Collection<Bestellung> bestellungen = actual.getBestellungen();
        for (Bestellung b : bestellungen) {
            bestellmenge += b.getBestellpositionen()
                             .stream()
                             .map(Bestellposition::getAnzahl)
                             .reduce(0, Integer::sum);
        }

        assertThat(bestellmenge)
            .overridingErrorMessage("Die Mindestbestellmenge des Kunden mit der ID %d muss %d sein, ist aber %d",
                                    actual.getId(), min, bestellmenge)
            .isGreaterThanOrEqualTo(min);
        return this;
    }

    public KundeAssert hasFamilienstand(FamilienstandType familienstand) {
        final Privatkunde privatkunde = (Privatkunde) actual;
        assertThat(privatkunde.getFamilienstand())
            .overridingErrorMessage("Der Familienstand muss %s sein, ist aber %s", familienstand, privatkunde.getFamilienstand())
            .isEqualTo(familienstand);
        return this;
    }
    
    public KundeAssert hasHobbys(Set<HobbyType> hobbys) {
        final Privatkunde privatkunde = (Privatkunde) actual;
        final Set<HobbyType> actualHobbys = privatkunde.getHobbys();
        
        assertThat(actualHobbys.size())
            .overridingErrorMessage("Die Anzahl der Hobbys muss %d sein, ist aber %d", hobbys.size(), actualHobbys.size())
            .isEqualTo(hobbys.size());
        
        final Optional<HobbyType> falschesHobby = hobbys.stream()
                                                        .filter(h -> !actualHobbys.contains(h))
                                                        .findAny();
        if (falschesHobby.isPresent()) {
            fail("Unzulaessiger Wert fuer HobbyType: " + falschesHobby.get());
        }
        return this;
    }
}
