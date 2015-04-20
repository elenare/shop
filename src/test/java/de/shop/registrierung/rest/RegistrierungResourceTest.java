/*
 * Copyright (C) 2013 Juergen Zimmermann, Hochschule Karlsruhe
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

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.util.BestellungBuilder;
import de.shop.iam.domain.Adresse;
import de.shop.iam.domain.IdentityVO;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.kundenverwaltung.util.AdresseBuilder;
import de.shop.kundenverwaltung.util.IdentityBuilder;
import de.shop.kundenverwaltung.util.PrivatkundeBuilder;
import de.shop.util.AbstractResourceTest;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ViolationReport;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.kundenverwaltung.domain.FamilienstandType.LEDIG;
import static de.shop.kundenverwaltung.domain.GeschlechtType.MAENNLICH;
import static de.shop.kundenverwaltung.rest.KundenResource.ID_PATH_PARAM;
import static de.shop.kundenverwaltung.util.KundeAssert.assertThatKunde;
import static de.shop.registrierung.rest.ViolationAssert.assertThatViolations;
import static de.shop.util.ResponseAssert.assertThatResponse;
import static de.shop.util.TestConstants.ARTIKEL_URI;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.BESTELLUNGEN_URI;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.KUNDEN_ID_URI;
import static de.shop.util.TestConstants.LOGINNAME_ADMIN;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static de.shop.util.TestConstants.REGISTRIERUNG_PRIVAT_URI;
import static de.shop.util.TestConstants.REGISTRIERUNG_URI;
import static java.util.Calendar.FEBRUARY;
import static java.util.Locale.ENGLISH;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class RegistrierungResourceTest extends AbstractResourceTest {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private static final String NACHNAME = "Nachnameneu";
    private static final String VORNAME = "Vorname";
    private static final String LOGINNAME = VORNAME + '.' + NACHNAME;
    private static final String EMAIL = NACHNAME + "@test.de";
    private static final int KATEGORIE = 1;
    private static final BigDecimal RABATT = new BigDecimal("0.15");
    private static final BigDecimal UMSATZ = new BigDecimal("10000000");
    private static final int TAG = 1;
    private static final int MONAT = FEBRUARY;
    private static final int JAHR = 2014;
    private static final Date SEIT = new GregorianCalendar(JAHR, MONAT, TAG).getTime();
    private static final String SEIT_STR = "2014-02-01";
    private static final String FAMILIENSTAND = LEDIG.name();
    private static final String GESCHLECHT = MAENNLICH.name();
    private static final String PLZ = "76133";
    private static final String PLZ_INVALID = "1234";
    private static final String ORT = "Karlsruhe";
    private static final String STRASSE = "Testweg";
    private static final String HAUSNR = "1";
    private static final String PASSWORD = "p";
    private static final boolean AGB_AKZEPTIERT = true;
    private static final boolean AGB_NICHT_AKZEPTIERT = false;
    private static final long ARTIKEL_ID = 501;
    
    private static final String NACHNAME_FORM = NACHNAME + "f";
    private static final String VORNAME_FORM = VORNAME + "f";
    private static final String LOGINNAME_FORM = VORNAME_FORM + '.' + NACHNAME_FORM;
    private static final String EMAIL_FORM_STRING =  "f" + EMAIL;
    private static final String ORT_FORM = ORT + "f";
    private static final String STRASSE_FORM = STRASSE + "f";
    private static final String HAUSNR_FORM = HAUSNR + "f";
    private static final String PASSWORD_FORM = PASSWORD + "f";
    
    private static final String LOGINNAME_INVALID = VORNAME + '.' + NACHNAME + ".invalid";
    private static final String PASSWORD_INVALID = PASSWORD + "x";
    
    private static final long ID_MIT_BESTELLUNGEN = 301;
    private static final String LOGINNAME_EMAIL_EXISTS = VORNAME + '.' + NACHNAME + ".emailEx";
    
    // Story: Als ein Gast moechte ich mich registrieren
    @Test
    @InSequence(1)
    public void save() throws URISyntaxException {
        LOGGER.finer(BEGINN);
        
        // Given
        final Adresse adresse = new AdresseBuilder()
                                .plz(PLZ)
                                .ort(ORT)
                                .strasse(STRASSE)
                                .hausnr(HAUSNR)
                                .build();
        final IdentityVO identity = new IdentityBuilder()
                                    .loginname(LOGINNAME)
                                    .enabled(true)
                                    .expirationDate(null)
                                    .password(PASSWORD)
                                    .passwordWdh(PASSWORD)
                                    .nachname(NACHNAME)
                                    .vorname(VORNAME)
                                    .email(EMAIL)
                                    .adresse(adresse)
                                    .build();
        final Privatkunde kunde = new PrivatkundeBuilder()
                                  .identity(identity)
                                  .kategorie(KATEGORIE)
                                  .rabatt(RABATT)
                                  .umsatz(UMSATZ)
                                  .seit(SEIT)
                                  .agbAkzeptiert(AGB_AKZEPTIERT)
                                  .build();
        
        // When
        Response response = getHttpsClient().target(REGISTRIERUNG_URI)
                                            .request()
                                            .post(json(kunde));
            
        // Then
        assertThatResponse(response)
            .hasStatusCreated()
            .hasPositiveNewId();
        response.close();
        
        // Einloggen als neuer Kunde und Bestellung aufgeben

        // Given (2)
        final Bestellung bestellung = new BestellungBuilder()
                                      .addBestellposition(ARTIKEL_URI + "/" + ARTIKEL_ID, 1)
                                      .build();

        // When (2)
        response = getHttpsClient(LOGINNAME, PASSWORD)
                   .target(BESTELLUNGEN_URI)
                   .request()
                   .post(json(bestellung));

        // Then (2)
        assertThatResponse(response)
            .hasStatusCreated()
            .hasPositiveNewId();
        response.close();

        LOGGER.finer(ENDE);
    }
    
    // Story: Als ein Gast moechte ich mich registrieren und dabei Formalarparameter verwenden
    @Test
    @InSequence(2)
    public void savePrivatkundeForm() throws URISyntaxException {
        LOGGER.finer(BEGINN);
        
        // Given
        final Form form = new Form();
        form.param("loginname", LOGINNAME_FORM)
            .param("nachname", NACHNAME_FORM)
            .param("vorname", VORNAME_FORM)
            .param("email", EMAIL_FORM_STRING)
            .param("kategorie", String.valueOf(KATEGORIE))
            .param("rabatt", String.valueOf(RABATT))
            .param("umsatz", String.valueOf(UMSATZ))
            .param("seit", SEIT_STR)
            .param("familienstand", FAMILIENSTAND)
            .param("geschlecht", GESCHLECHT)
            .param("agbAkzeptiert", String.valueOf(AGB_AKZEPTIERT))
            .param("plz", PLZ)
            .param("ort", ORT_FORM)
            .param("strasse", STRASSE_FORM)
            .param("hausnr", HAUSNR_FORM)
            .param("password", PASSWORD_FORM)
            .param("passwordWdh", PASSWORD_FORM);
        
        Response response = getHttpsClient().target(REGISTRIERUNG_PRIVAT_URI)
                                            .request()
                                            .post(form(form));
            
        // Then
        assertThatResponse(response)
            .hasStatusCreated()
            .hasPositiveNewId();
        response.close();
        
        // Einloggen als neuer Kunde und Bestellung aufgeben

        // Given (2)
        final Bestellung bestellung = new BestellungBuilder()
                                      .addBestellposition(ARTIKEL_URI + "/" + ARTIKEL_ID, 1)
                                      .build();

        // When (2)
        response = getHttpsClient(LOGINNAME_FORM, PASSWORD_FORM)
                   .target(BESTELLUNGEN_URI)
                   .request()
                   .post(json(bestellung));

        // Then (2)
        assertThatResponse(response)
            .hasStatusCreated()
            .hasPositiveNewId();
        response.close();

        LOGGER.finer(ENDE);
    }


    // Story: Als ein Gast erhalte ich einen 400 Fehler, wenn ich mich mit ungueltigen
    //        Daten zu registrieren versuche
    @Test
    @InSequence(3)
    public void saveInvalid() {
        LOGGER.finer(BEGINN);
        
        // Given
        final Adresse adresse = new AdresseBuilder()
                                .plz(PLZ_INVALID)
                                .ort(ORT)
                                .strasse(STRASSE)
                                .hausnr(HAUSNR)
                                .build();
        final IdentityVO identity = new IdentityBuilder()
                                    .loginname(LOGINNAME_INVALID)
                                    .enabled(true)
                                    .expirationDate(null)
                                    .password(PASSWORD)
                                    .passwordWdh(PASSWORD_INVALID)
                                    .nachname(NACHNAME)
                                    .vorname(VORNAME)
                                    .email(EMAIL)
                                    .adresse(adresse)
                                    .build();
        final Privatkunde kunde = new PrivatkundeBuilder()
                                  .identity(identity)
                                  .seit(SEIT)
                                  .agbAkzeptiert(AGB_NICHT_AKZEPTIERT)
                                  .build();
        
        // When
        final Response response = getHttpsClient().target(REGISTRIERUNG_URI)
                                                  .request()
                                                  .accept(APPLICATION_JSON)
                                                  // engl. Fehlermeldungen ohne Umlaute ;-)
                                                  .acceptLanguage(ENGLISH)
                                                  .post(json(kunde));

        // Then
        assertThatResponse(response)
            .hasStatusBadRequest()
            .hasValidationException();
        
        final Collection<ResteasyConstraintViolation> violations = response.readEntity(ViolationReport.class)
                                                                           .getParameterViolations();
        response.close();

        assertThatViolations(violations)
            .haveAtLeastOneViolation()
            .havePasswordViolation()
            .haveAgbViolation()
            .havePlzViolation(PLZ_INVALID);
        
        LOGGER.finer(ENDE);
    }

    // Story: Als ein Gast erhalte ich einen 409 Fehler, wenn ich mich mit einer
    //        bereits existierenden Email-Adresse zu registrieren versuche
    @Test
    @InSequence(4)
    public void saveEmailExists() {
        LOGGER.finer(BEGINN);
        
        // Given
        Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
                            .target(KUNDEN_ID_URI)
                            .resolveTemplate(ID_PATH_PARAM, ID_MIT_BESTELLUNGEN)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
        assertThatResponse(response).hasStatusOk();
        
        final AbstractKunde kunde = response.readEntity(AbstractKunde.class);
        assertThatKunde(kunde)
            .hasIdentity()
            .hasAdresse();
        
        kunde.getIdentity().setLoginname(LOGINNAME_EMAIL_EXISTS);
        
        // When
        response = getHttpsClient().target(REGISTRIERUNG_URI)
                                   .request()
                                   .accept(APPLICATION_JSON)
                                   // engl. Fehlermeldungen ohne Umlaute ;-)
                                   .acceptLanguage(ENGLISH)
                                   .post(json(kunde));
        
        // Then
        assertThatResponse(response)
            .hasStatusBadRequest()
            .hasErrorMessage("The email address \"" + kunde.getIdentity().getEmail() + "\" already exists.");
        response.close();
        
        LOGGER.finer(ENDE);
    }
}
