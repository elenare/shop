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

package de.shop.kundenverwaltung.rest;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.GeschlechtType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.util.AbstractResourceTest;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ViolationReport;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.bestellverwaltung.util.BestellungenAssert.assertThatBestellungen;
import static de.shop.kundenverwaltung.rest.KundenResource.GESCHLECHT_QUERY_PARAM;
import static de.shop.kundenverwaltung.rest.KundenResource.ID_PATH_PARAM;
import static de.shop.kundenverwaltung.rest.KundenResource.NACHNAME_QUERY_PARAM;
import static de.shop.kundenverwaltung.util.KundeAssert.assertThatKunde;
import static de.shop.kundenverwaltung.util.KundenAssert.assertThatKunden;
import static de.shop.kundenverwaltung.util.ViolationAssert.assertThatViolations;
import static de.shop.util.ByteArrrayAssert.assertThatByteArrray;
import static de.shop.util.Constants.FIRST_LINK;
import static de.shop.util.Constants.LAST_LINK;
import static de.shop.util.ResponseAssert.assertThatResponse;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.KUNDEN_BESTELLUNGEN_ID_URI;
import static de.shop.util.TestConstants.KUNDEN_ID_URI;
import static de.shop.util.TestConstants.KUNDEN_IMAGE_ID_URI;
import static de.shop.util.TestConstants.KUNDEN_PRIVAT_URI;
import static de.shop.util.TestConstants.KUNDEN_URI;
import static de.shop.util.TestConstants.LOGINNAME;
import static de.shop.util.TestConstants.LOGINNAME_ADMIN;
import static de.shop.util.TestConstants.LOGINNAME_KUNDE;
import static de.shop.util.TestConstants.NOT_EXISTING_URI;
import static de.shop.util.TestConstants.PASSWORD;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static de.shop.util.TestConstants.PASSWORD_FALSCH;
import static de.shop.util.TestConstants.PASSWORD_KUNDE;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class KundenResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final long ID_MIT_BESTELLUNGEN = 301;
	private static final long ID_NICHT_VORHANDEN = 10_000L;
	private static final long ID_UPDATE = 320;
	private static final long ID_UPDATE_FALSCHES_PASSWORD = 325;
    private static final long ID_DELETE = 322;
	private static final long ID_DELETE_MIT_BESTELLUNGEN = 301;
	private static final long ID_DELETE_FORBIDDEN = 301;
	private static final String NACHNAME = "Alpha";
	private static final String NACHNAME_NICHT_VORHANDEN = "Falschername";
	private static final String NACHNAME_INVALID = "Test9";
    
    private static final int TIMEOUT = 3;
    
    private static final long BESTELLUNG_ID = 601;
	private static final long BESTELLUNG_ID_FORBIDDEN = 601;
    
	private static final String NEUER_NACHNAME = "Nachnameneu";
    private static final String NEUE_BEMERKUNGEN = "Neue Bemerkungen";
	private static final String NEU_SEIT_STR = "2014-02-01";
    
    private static final String NEUER_NACHNAME_UPDATE = NEUER_NACHNAME + "update";
	
	private static final String IMAGE_FILENAME = "image.png";
	private static final String IMAGE_PATH_UPLOAD = "src/test/resources/rest/" + IMAGE_FILENAME;
	private static final String IMAGE_MIMETYPE = "image/png";
	private static final String IMAGE_PATH_DOWNLOAD = "target/" + IMAGE_FILENAME;
	private static final long ID_UPLOAD = 302;

	private static final String IMAGE_INVALID = "image.bmp";
	private static final String IMAGE_INVALID_PATH = "src/test/resources/rest/" + IMAGE_INVALID;
	private static final String IMAGE_INVALID_MIMETYPE = "image/bmp";
	
	
    @Test
	@InSequence(1)
	public void validate() {
		LOGGER.finer(BEGINN);
		
		assertThat(true).isTrue();
		
		LOGGER.finer(ENDE);
	}
	
	@Ignore("Beispiel fuer Ignore")
	@Test
	@InSequence(2)
	public void beispielIgnore() {
		assertThat(true).isFalse();
	}
	
	@Ignore("Beispiel fuer Fail")
	@Test
	@InSequence(3)
	public void beispielFailMitIgnore() {
		fail("Beispiel fuer fail()");
	}
	
    // Story: Als ein Administrator moechte ich einen Kunden anhand der ID suchen und
    //        die Bestellungen mitladen
    @Test
	@InSequence(10)
	public void findByIdFetchBestellungen() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				            .target(KUNDEN_ID_URI)
                            .resolveTemplate(ID_PATH_PARAM, ID_MIT_BESTELLUNGEN)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
	
		// Then
        assertThatResponse(response)
            .hasStatusOk()
            .hasLinks()
            .hasSelfLinkMitId(ID_MIT_BESTELLUNGEN);
        
		final AbstractKunde kunde = response.readEntity(AbstractKunde.class);
        assertThatKunde(kunde)
            .hasId(ID_MIT_BESTELLUNGEN)
            .hasNoLoginname()
            .hasIdentity()
            .hasLoginnameInIdentity()
            .hasAdresse()
            .hasBestellungenUri();
		
		final URI bestellungenUri = kunde.getBestellungenUri();
		response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				   .target(bestellungenUri)
				   .request()
				   .accept(APPLICATION_JSON)
				   .get();
        assertThatResponse(response).hasStatusOk();
		
		// Verweist der Link-Header der ermittelten Bestellungen auf den Kunden?
		final List<Bestellung> bestellungen = response.readEntity(new GenericType<List<Bestellung>>() { });
		
        assertThatBestellungen(bestellungen)
            .isNotEmpty()
            .doesNotContainNull()
            .haveKundeUriEndingWith(ID_MIT_BESTELLUNGEN);
		LOGGER.finer(ENDE);
	}
	
    // Story: Als ein Administrator erhalte ich einen 404 Fehler, wenn ich einen Kunden zu einer nicht-vorhandenen ID suche
	@Test
	@InSequence(11)
	public void findByIdNichtVorhanden() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		final Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				                  .target(KUNDEN_ID_URI)
                                  .resolveTemplate(ID_PATH_PARAM, ID_NICHT_VORHANDEN)
                                  .request()
                                  .acceptLanguage(GERMAN)
                                  .get();

    	// Then
        assertThatResponse(response).hasStatusNotFound();
    	response.close();
		
		LOGGER.finer(ENDE);
	}

    // Story: Als ein Gast erhalte ich einen 404 Fehler, wenn ich eine nicht-existierende URI aufrufe
	@Test
	@InSequence(12)
	public void notExistingUri() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		Response response = getHttpsClient().target(NOT_EXISTING_URI)
                                            .request()
                                            .get();
	
		// Then
        assertThatResponse(response).hasStatusNotFound();
        response.close();
		
		LOGGER.finer(ENDE);
	}
	
    // Story: Als ein Administrator moechte ich einen Kunden anhand der ID asynchron suchen
	@Test
	@InSequence(13)
	public void findByIdAsyncClient() throws InterruptedException, ExecutionException, TimeoutException {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When *ASYNCHRONER* Aufruf
        final Future<Response> future = CompletableFuture.supplyAsync(() ->
                                            getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
                                            .target(KUNDEN_ID_URI)
                                            .resolveTemplate(ID_PATH_PARAM, ID_MIT_BESTELLUNGEN)
                                            .request()
                                            .accept(APPLICATION_JSON)
                                            .get());
        
		// Then Abfrage mit begrenztem Timeout
        final Response response = future.get(TIMEOUT, SECONDS);
		final AbstractKunde kunde = response.readEntity(AbstractKunde.class);
        assertThatKunde(kunde)
            .hasId(ID_MIT_BESTELLUNGEN)
            .hasNoLoginname()
            .hasAdresse()
            .hasAgbAkzeptiert();
		
		LOGGER.finer(ENDE);
	}
     // Story: Als ein Administrator moechte ich einen Kunden anhand der ID asynchron suchen
     //        und abschliessend einen Callback aufrufen   
	@Test
	@InSequence(14)
	public void findByIdAsyncClientMitCallback() throws InterruptedException, ExecutionException, TimeoutException {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When *ASYNCHRONER* Aufruf mit einer Callback-Funktion
		getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
		.target(KUNDEN_ID_URI)
        .resolveTemplate(ID_PATH_PARAM, ID_MIT_BESTELLUNGEN)
        .request()
        .accept(APPLICATION_JSON)
		.async()
        .get(new InvocationCallback<Response>() {
            @Override
            public void completed(Response response) {
                // Then
                final AbstractKunde kunde = response.readEntity(AbstractKunde.class);
                assertThatKunde(kunde)
                    .hasId(ID_MIT_BESTELLUNGEN)
                    .hasLoginname()
                    .hasAdresse()
                    .hasAgbAkzeptiert();
            }
            @Override
            public void failed(Throwable throwable) {
                fail("Fehler beim asynchronen Aufruf des RESTful Web Service");
            }
        });
		
		LOGGER.finer(ENDE);
	}
	
    // Story: Als ein Administrator moechte ich einen Kunden anhand der ID suchen, wobei im
    //        Server der Request asynchron abgearbeitet wird
	@Test
	@InSequence(15)
    @Ignore("PicketLink 2.6.x funktioniert nicht mit asynchronen Requests")
	public void findByIdAsyncServer() throws InterruptedException, ExecutionException, TimeoutException {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When *SYNCHRONER* Aufruf der *ASYNCHRONEN* REST-Schnittstelle
		Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
		                    .target(KUNDEN_ID_URI + "/async")
                            .resolveTemplate(ID_PATH_PARAM, ID_MIT_BESTELLUNGEN)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
	
		// Then
        assertThatResponse(response).hasStatusOk();
		AbstractKunde kunde = response.readEntity(AbstractKunde.class);
        assertThatKunde(kunde)
            .hasId(ID_MIT_BESTELLUNGEN)
            .hasLoginname()
            .hasAdresse()
            .hasAgbAkzeptiert();
		
		// When *ASYNCHRONEN* Aufruf
        final Future<AbstractKunde> future = CompletableFuture.supplyAsync(() ->
                                                getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
                                                .target(KUNDEN_ID_URI + "/async")
                                                .resolveTemplate(ID_PATH_PARAM, ID_MIT_BESTELLUNGEN)
                                                .request()
                                                .accept(APPLICATION_JSON)
                                                .get(AbstractKunde.class));
        
		// Then Abfrage in separatem Thread
		kunde = future.get(TIMEOUT, SECONDS);
        assertThatKunde(kunde)
            .hasId(ID_MIT_BESTELLUNGEN)
            .hasLoginname()
            .hasAdresse()
            .hasAgbAkzeptiert();
		
		LOGGER.finer(ENDE);
	}
	
    // Story: Als ein Administrator moechte ich Kunden anhand des Nachnamens suchen
	@Test
	@InSequence(20)
	public void findByNachname() {
		LOGGER.finer(BEGINN);
		
		// Given

		// When
		Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				            .target(KUNDEN_URI)
                            .queryParam(NACHNAME_QUERY_PARAM, NACHNAME)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();

		// Then
        assertThatResponse(response).hasStatusOk();
		
		final List<AbstractKunde> kunden = response.readEntity(new GenericType<List<AbstractKunde>>() { });
        assertThatKunden(kunden)
            .isNotEmpty()
            .doNotContainNull();
		
        assertThatResponse(response)
            .hasLinks()
            .hasLink(FIRST_LINK)
            .hasLink(LAST_LINK);
        
		kunden.forEach(k -> {  // NICHT parallelStream wegen Response.close()
            assertThatKunde(k)
                .hasNachname(NACHNAME)
                .hasBestellungenUri();
			
			final URI bestellungenUri = k.getBestellungenUri();
			final Response r = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
					           .target(bestellungenUri)
					           .request()
					           .accept(APPLICATION_JSON)
					           .get();
            // Entweder gibt es Bestellungen oder nicht
            assertThatResponse(r).hasStatusOkOrNotFound();
			// readEntity() wurde nicht aufgerufen
			r.close();
		});
		
		LOGGER.finer(ENDE);
	}
    
	// Story: Als ein Administrator moechte ich einen 404 Fehler erhalten, wenn ich Kunden anhand
    //        eines nicht-vorhandenen Nachnamens suche
	@Test
	@InSequence(21)
	public void findByNachnameNichtVorhanden() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		final Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				                  .target(KUNDEN_URI)
                                  .queryParam(NACHNAME_QUERY_PARAM, NACHNAME_NICHT_VORHANDEN)
                                  .request()
                                  .acceptLanguage(GERMAN)
                                  .get();
		
		// Then
        assertThatResponse(response).hasStatusNotFound();
		response.close();

		LOGGER.finer(ENDE);
	}
	
    // Story: Als ein Administrator moechte ich einen 400 Fehler erhalten, wenn ich Kunden anhand
    //        eines ungueltigen Nachnamens suche
	@Test
	@InSequence(22)
	public void findByNachnameInvalid() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		final Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				                  .target(KUNDEN_URI)
                                  .queryParam(NACHNAME_QUERY_PARAM, NACHNAME_INVALID)
                                  .request()
                                  .accept(APPLICATION_JSON)
                                  .acceptLanguage(ENGLISH)
                                  .get();
		
		// Then
        assertThatResponse(response)
            .hasStatusBadRequest()
            .hasValidationException();
        
		final Collection<ResteasyConstraintViolation> violations = response.readEntity(ViolationReport.class)
                                                                           .getParameterViolations();
        assertThatViolations(violations)
            .haveAtLeastOneViolation()
            .haveNachnameViolation(NACHNAME_INVALID);

		LOGGER.finer(ENDE);
	}
	
    // Story: Als ein Administrator moechte ich Kunden anhand des Geschlechts suchen
	@Test
	@InSequence(30)
	public void findByGeschlecht() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		for (GeschlechtType geschlecht : GeschlechtType.values()) {
			// When
			final Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
					                  .target(KUNDEN_URI)
                                      .queryParam(GESCHLECHT_QUERY_PARAM, geschlecht)
                                      .request()
                                      .accept(APPLICATION_JSON)
                                      .get();
			final List<Privatkunde> kunden = response.readEntity(new GenericType<List<Privatkunde>>() { });
			
			// Then
            assertThatKunden(kunden)
                .isNotEmpty()
                .doNotContainNull()
                .containOnlyGeschlecht(geschlecht);
		}
		
		LOGGER.finer(ENDE);
	}
    
    // Story: Als Administrator moechte ich einen Kunden zu einer Bestellung-ID auslesen
	@Test
	@InSequence(31)
	public void findKundeByBestellungId() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				            .target(KUNDEN_BESTELLUNGEN_ID_URI)
                            .resolveTemplate(KundenResource.BESTELLUNGEN_ID_PATH_PARAM, BESTELLUNG_ID)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
			
		// Then
        assertThatResponse(response).hasStatusOk();
		final AbstractKunde kunde = response.readEntity(AbstractKunde.class);
        assertThatKunde(kunde).isNotNull();
		
		response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				   .target(KUNDEN_ID_URI)
                   .resolveTemplate(KundenResource.ID_PATH_PARAM, kunde.getId())
                   .request()
                   .accept(APPLICATION_JSON)
                   .get();
        assertThatResponse(response)
            .hasStatusOk()
            .hasLinks();
		response.close();    // response.readEntity() wurde nicht aufgerufen

		LOGGER.finer(ENDE);
	}

    // Story: Als Kunde erhalte ich einen 403 Fehler, wenn ich einen anderen Kunden zu seiner Bestellung-ID
    //        auszulesen versuche
    @Test
	@InSequence(32)
	public void findKundeByBestellungForbidden() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		final Response response = getHttpsClient(LOGINNAME_KUNDE, PASSWORD_KUNDE)
				                  .target(KUNDEN_BESTELLUNGEN_ID_URI)
                                  .resolveTemplate(KundenResource.BESTELLUNGEN_ID_PATH_PARAM, BESTELLUNG_ID_FORBIDDEN)
                                  .request()
                                  .accept(APPLICATION_JSON)
                                  .get();
			
		// Then
        assertThatResponse(response).hasStatusForbidden();
		// response.readEntity() wurde nicht aufgerufen
		response.close();

		LOGGER.finer(ENDE);
	}

    // Story: Als ein Administrator moechte ich Kundendaten aendern
	@Test
	@InSequence(40)
	public void update() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				            .target(KUNDEN_ID_URI)
                            .resolveTemplate(ID_PATH_PARAM, ID_UPDATE)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
		AbstractKunde kunde = response.readEntity(AbstractKunde.class);
        assertThatKunde(kunde).hasId(ID_UPDATE);
		final int origVersion = kunde.getVersion();
    	
    	// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuen Bemerkungen bauen
		kunde.setBemerkungen(NEUE_BEMERKUNGEN);
        kunde.getIdentity().setNachname(NEUER_NACHNAME_UPDATE);
    	
		response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				   .target(KUNDEN_URI)
                   .request()
                   .accept(APPLICATION_JSON)
                   .put(json(kunde));
		// Then
        assertThatResponse(response).hasStatusOk();
		kunde = response.readEntity(AbstractKunde.class);
        assertThatKunde(kunde)
            .hasVersionGreaterThant(origVersion)
            .hasNachname(NEUER_NACHNAME_UPDATE)
            .hasBemerkungen(NEUE_BEMERKUNGEN);
		
		// Erneutes Update funktioniert, da die Versionsnr. beim ausgelesenen (readEntity s.o.) Kunden aktualisiert ist
        kunde.setBemerkungen(NEUE_BEMERKUNGEN + "_2");
		response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				   .target(KUNDEN_URI)
                   .request()
                   .put(json(kunde));
        assertThatResponse(response).hasStatusOk();
		response.close();
		
		// Erneutes Update funktioniert NICHT, da das Kundenobjekt nicht ausgelesen wurde
        // d.h. das vorherige Kundenobjekt wird verwendet und die Versionsnr ist deshalb NICHT aktualisiert
        kunde.setBemerkungen(NEUE_BEMERKUNGEN + "_3");
		response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				   .target(KUNDEN_URI)
                   .request()
                   .put(json(kunde));
        assertThatResponse(response).hasStatusConflict();
		response.close();
		
		LOGGER.finer(ENDE);
   	}
	
    // Story: Als ein Administrator moechte ich Kundendaten via Formularparameter aendern
	@Test
	@InSequence(41)
	public void updatePrivatkundeForm() {
		LOGGER.finer(BEGINN);
		
		// Given
		Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				            .target(KUNDEN_ID_URI)
                            .resolveTemplate(ID_PATH_PARAM, ID_UPDATE)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
		Privatkunde kunde = response.readEntity(Privatkunde.class);
        assertThatKunde(kunde).hasId(ID_UPDATE);
		final int origVersion = kunde.getVersion();
        
    	// Aus den gelesenen Werten ein neues Formular-Objekt mit neuem Nachnamen bauen
		final Form form = new Form();
		form.param("id", String.valueOf(kunde.getId()))
		    .param("version", String.valueOf(kunde.getVersion()))
            .param("loginname", kunde.getIdentity().getLoginname())
		    .param("nachname", NEUER_NACHNAME)
		    .param("email", kunde.getIdentity().getEmail())
		    .param("kategorie", String.valueOf(kunde.getKategorie()))
		    .param("umsatz", String.valueOf(kunde.getUmsatz()))
		    .param("seit", NEU_SEIT_STR)
		    .param("agbAkzeptiert", String.valueOf(kunde.isAgbAkzeptiert()))
		    .param("familienstand", kunde.getFamilienstand().name())
		    .param("geschlecht", kunde.getGeschlecht().name())
		    .param("plz", kunde.getIdentity().getAdresse().getPlz())
		    .param("ort", kunde.getIdentity().getAdresse().getOrt())
		    .param("strasse", kunde.getIdentity().getAdresse().getStrasse());
		
		if (kunde.getIdentity().getVorname() != null) {
		    form.param("vorname", String.valueOf(kunde.getIdentity().getVorname()));
		}
		if (kunde.getRabatt() != null) {
		    form.param("rabatt", String.valueOf(kunde.getRabatt()));
		}
		if (kunde.getIdentity().getAdresse().getHausnr() != null) {
		    form.param("hausnr", String.valueOf(kunde.getIdentity().getAdresse().getHausnr()));
		}
    	
		// When
		response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				   .target(KUNDEN_PRIVAT_URI)
                   .request()
                   .accept(APPLICATION_JSON)
                   .put(form(form));        // POST fuer Update in einer Web-Anwendung
		// Then
        assertThatResponse(response).hasStatusOk();
		kunde = response.readEntity(Privatkunde.class);
        assertThatKunde(kunde).hasVersionGreaterThant(origVersion);
		
		// Erneutes Update funktioniert NICHT, da die Versionsnr. NICHT aktualisiert ist
		response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				   .target(KUNDEN_PRIVAT_URI)
                   .request()
                   .put(form(form));
        assertThatResponse(response).hasStatusConflict();
		response.close();
		
		LOGGER.finer(ENDE);
   	}
	
	
    // Story: Als ein Administrator erhalte ich einen 401 Fehler, wenn ich beim Aendern
    //        eines vorhandenen Kunden ein falsches Passwort verwende
	@Test
	@InSequence(42)
	public void updateFalschesPassword() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				            .target(KUNDEN_ID_URI)
                            .resolveTemplate(ID_PATH_PARAM, ID_UPDATE_FALSCHES_PASSWORD)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
        assertThatResponse(response).hasStatusOk();
		final AbstractKunde kunde = response.readEntity(AbstractKunde.class);
    	
		response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_FALSCH)
				   .target(KUNDEN_URI)
                   .request()
                   .accept(APPLICATION_JSON)
                   .put(json(kunde));

		// Then
        assertThatResponse(response).hasStatusUnauthorized();
		response.close();
		
		LOGGER.finer(ENDE);
	}

    // Story: Als ein Administrator moechte ich einen Kunden ohne Bestellungen loeschen
	@Test
	@InSequence(50)
	public void delete() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				            .target(KUNDEN_ID_URI)
                            .resolveTemplate(ID_PATH_PARAM, ID_DELETE)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
        assertThatResponse(response).hasStatusOk();
		response.close();
		
		response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				   .target(KUNDEN_ID_URI)
                   .resolveTemplate(ID_PATH_PARAM, ID_DELETE)
                   .request()
                   .delete();
		
		// Then
        assertThatResponse(response).hasStatusNoContent();
		response.close();
		
		response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				   .target(KUNDEN_ID_URI)
                   .resolveTemplate(ID_PATH_PARAM, ID_DELETE)
                   .request()
                   .accept(APPLICATION_JSON)
                   .get();
        assertThatResponse(response).hasStatusNotFound();
		response.close();
        
		LOGGER.finer(ENDE);
	}
	
    // Story: Als ein Administrator erhalte ich einen 400 Fehler, wenn ich einen
    //        Kunden mit Bestellungen zu loeschen versuche
	@Test
	@InSequence(51)
	public void deleteMitBestellung() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		final Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
							      .target(KUNDEN_ID_URI)
                                  .resolveTemplate(ID_PATH_PARAM, ID_DELETE_MIT_BESTELLUNGEN)
                                  .request()
                                  .acceptLanguage(GERMAN)
                                  .delete();
		
		// Then
        assertThatResponse(response)
            .hasStatusBadRequest()
            .hasErrorMessageStartingEnding("Der Kunde mit ID", "Bestellung(en).");
		
		LOGGER.finer(ENDE);
	}
	
	
    // Story: Als ein Kunde erhalte ich einen 401 Fehler, wenn ich einen anderen Kunden zu loeschen versuche
	@Test
	@InSequence(52)
	public void deleteFehlendeBerechtigung() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		final Response response = getHttpsClient(LOGINNAME, PASSWORD)
				                  .target(KUNDEN_ID_URI)
                                  .resolveTemplate(ID_PATH_PARAM, ID_DELETE_FORBIDDEN)
                                  .request()
                                  .delete();
		
		// Then
        assertThatResponse(response).hasStatusForbiddenOrNotFound();
		response.close();
		
		LOGGER.finer(ENDE);
	}
	
    // Story: Als ein Administrator moechte ich zu einem Kunden ein neues Bild hochladen
    //        und auch wieder herunterladen
	@Test
	@InSequence(60)
	public void uploadDownload() throws IOException {
		LOGGER.finer(BEGINN);
		
		// Given
		// Datei einlesen
		final byte[] uploadBytes = Files.readAllBytes(Paths.get(IMAGE_PATH_UPLOAD));
		
		// When
		Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				            .target(KUNDEN_IMAGE_ID_URI)
                            .resolveTemplate(ID_PATH_PARAM, ID_UPLOAD)
                            .request()
                            .post(entity(uploadBytes, IMAGE_MIMETYPE));

		// Then
        assertThatResponse(response)
            .hasStatusCreated()
            .hasId(ID_UPLOAD);
		response.close();
		
		// When (2)
		// Download der zuvor hochgeladenen Datei
		byte[] downloadBytes;
		
		response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				   .target(KUNDEN_IMAGE_ID_URI)
                   .resolveTemplate(ID_PATH_PARAM, ID_UPLOAD)
                   .request()
                   .accept(IMAGE_MIMETYPE)
                   .get();
		downloadBytes = response.readEntity(new GenericType<byte[]>() {});
		
		// Then (2)
        assertThatByteArrray(downloadBytes)
            .hasEqualLength(uploadBytes)
            .isEqualTo(uploadBytes);
		
		// Abspeichern des heruntergeladenen byte[] als Datei im Unterverz. target zur manuellen Inspektion
		Files.write(Paths.get(IMAGE_PATH_DOWNLOAD), downloadBytes);
		LOGGER.info("Heruntergeladene Datei abgespeichert: " + IMAGE_PATH_DOWNLOAD);
		
		LOGGER.finer(ENDE);
	}
	
    // Story: Als ein Administrator erhalte ich einen 415 Fehler, wenn ich ein
    //        Bild mit ungueltigem MIME-Type hochzuladen versuche
	@Test
	@InSequence(61)
	public void uploadInvalidMimeType() throws IOException {
		LOGGER.finer(BEGINN);
		
		// Given
		// Datei einlesen
		final byte[] uploadBytes = Files.readAllBytes(Paths.get(IMAGE_INVALID_PATH));
		
		// When
		final Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				                  .target(KUNDEN_IMAGE_ID_URI)
                                  .resolveTemplate(ID_PATH_PARAM, ID_UPLOAD)
                                  .request()
                                  .post(entity(uploadBytes, IMAGE_INVALID_MIMETYPE));
		
        assertThatResponse(response).hasStatusUnsupportedType();
		response.close();
	}
}
