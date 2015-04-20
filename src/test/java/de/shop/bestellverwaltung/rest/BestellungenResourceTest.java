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

package de.shop.bestellverwaltung.rest;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.util.BestellungBuilder;
import de.shop.util.AbstractResourceTest;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.bestellverwaltung.util.BestellungAssert.assertThatBestellung;
import static de.shop.util.LocationHelper.extractId;
import static de.shop.util.ResponseAssert.assertThatResponse;
import static de.shop.util.TestConstants.ARTIKEL_URI;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.BESTELLUNGEN_ID_URI;
import static de.shop.util.TestConstants.BESTELLUNGEN_URI;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.LOGINNAME;
import static de.shop.util.TestConstants.LOGINNAME_ADMIN;
import static de.shop.util.TestConstants.LOGINNAME_KUNDE;
import static de.shop.util.TestConstants.PASSWORD;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static de.shop.util.TestConstants.PASSWORD_KUNDE;
import static de.shop.util.TestConstants.WARENKORBPOSITION_URI;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class BestellungenResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final long ID = 601;
	private static final long ID_FORBIDDEN = 601;
	private static final long ARTIKEL_ID_1 = 501;
	private static final long ARTIKEL_ID_2 = 502;
    private static final int ANZAHL = 1;
	private static final String ARTIKEL_ID_INVALID = "INVALID";
	
    // Story: Als Administrator moechte ich eine Bestellung anhand der ID auslesen
	@Test
	@InSequence(1)
	public void findById() {
		LOGGER.finer(BEGINN);
        
		// Given
		
		// When
		final Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				                  .target(BESTELLUNGEN_ID_URI)
                                  .resolveTemplate(BestellungenResource.ID_PATH_PARAM, ID)
                                  .request()
                                  .accept(APPLICATION_JSON)
                                  .get();
		
		// Then
        assertThatResponse(response).hasStatusOk();
		final Bestellung bestellung = response.readEntity(Bestellung.class);
		
        assertThatBestellung(bestellung)
            .hasId(ID)
            .hasBestellpositionen();

		LOGGER.finer(ENDE);
	}

    // Story: Als Kunde moechte ich, dass ich eine Bestellung eines anderen Kunden
    //         nicht anhand der ID auslesen auslesen darf
	@Test
	@InSequence(2)
	public void findByIdForbidden() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		final Response response = getHttpsClient(LOGINNAME_KUNDE, PASSWORD_KUNDE)
				                  .target(BESTELLUNGEN_ID_URI)
                                  .resolveTemplate(BestellungenResource.ID_PATH_PARAM, ID_FORBIDDEN)
                                  .request()
                                  .accept(APPLICATION_JSON)
                                  .get();
		
		// Then
        assertThatResponse(response).hasStatusForbidden();
		response.close();

		LOGGER.finer(ENDE);
	}

    // Story: Als Kunde moechte ich eine neue Bestellung anlegen
	@Test
	@InSequence(10)
	public void save() throws URISyntaxException {
		LOGGER.finer(BEGINN);
		
		// Given
		// Neues, client-seitiges Bestellungsobjekt als JSON-Datensatz
		final Bestellung bestellung = new BestellungBuilder()
                                      .addBestellposition(ARTIKEL_URI + "/" + ARTIKEL_ID_1, ANZAHL)
                                      .addBestellposition(ARTIKEL_URI + "/" + ARTIKEL_ID_2, ANZAHL)
                                      .build();
		
		// When
		Response response = getHttpsClient(LOGINNAME, PASSWORD)
				            .target(BESTELLUNGEN_URI)
                            .request()
                            .post(json(bestellung));
			
		// Then
        assertThatResponse(response)
            .hasStatusCreated()
            .hasPositiveNewId();
        final long id = extractId(response);
		response.close();
			
		// Gibt es die neue Bestellung?
		response = getHttpsClient(LOGINNAME, PASSWORD)
				   .target(BESTELLUNGEN_ID_URI)
                   .resolveTemplate(BestellungenResource.ID_PATH_PARAM, id)
                   .request()
                   .accept(APPLICATION_JSON)
                   .get();
		assertThatResponse(response).hasStatusOk();
		response.close();
		
		LOGGER.finer(ENDE);
	}
	
    // Story: Als Kunde moechte ich, dass keine neue Bestellung angelegt wird,
    //        falls eine Artikel-ID nicht gueltig ist
	@Test
	@InSequence(11)
	public void saveArtikelIdInvalid() throws URISyntaxException {
		LOGGER.finer(BEGINN);
		
		// Given
		// Neues, client-seitiges Bestellungsobjekt als JSON-Datensatz
		final Bestellung bestellung = new BestellungBuilder()
                                      .addBestellposition(ARTIKEL_URI + "/" + ARTIKEL_ID_INVALID, ANZAHL)
                                      .build();

		// When
		final Response response = getHttpsClient(LOGINNAME, PASSWORD)
				                  .target(BESTELLUNGEN_URI)
                                  .request()
                                  .post(json(bestellung));
			
		// Then
        assertThatResponse(response)
            .hasStatusBadRequest()
            .hasErrorMessageStartingWith("Keine ");
		
		LOGGER.finer(ENDE);
	}
	
    // Story: Als Kunde moechte ich, dass eine neue Bestellung mit dem Inhalt des
    //        persistenten Warenkorbs angelegt wird.
	@Test
	@InSequence(20)
	public void saveWarenkorb() throws URISyntaxException {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// Neues, client-seitiges Objekt fuer Warenkorbposition als Formular-Datensatz
		final Form form1 = new Form();
		form1.param("artikelUri", ARTIKEL_URI + "/" + ARTIKEL_ID_1)
		     .param("anzahl", "1");
		final Form form2 = new Form();
		form2.param("artikelUri", ARTIKEL_URI + "/" + ARTIKEL_ID_2)
		     .param("anzahl", "2");
		
		// When
		Response response = getHttpsClient(LOGINNAME, PASSWORD)
				            .target(WARENKORBPOSITION_URI)
                            .request()
                            .post(form(form1));
		assertThatResponse(response).hasStatusCreated();
		response.close();
        
		response = getHttpsClient(LOGINNAME, PASSWORD)
				   .target(WARENKORBPOSITION_URI)
                   .request()
                   .post(form(form2));
		assertThatResponse(response).hasStatusCreated();
		response.close();
        
		response = getHttpsClient(LOGINNAME, PASSWORD)
				   .target(BESTELLUNGEN_URI)
                   .request()
                   .post(null);

		// Then
		assertThatResponse(response)
            .hasStatusCreated()
            .hasPositiveNewId();
        final long id = extractId(response);
		response.close();
		
		// Gibt es die neue Bestellung?
		response = getHttpsClient(LOGINNAME, PASSWORD)
				   .target(BESTELLUNGEN_ID_URI)
                   .resolveTemplate(BestellungenResource.ID_PATH_PARAM, id)
                   .request()
                   .accept(APPLICATION_JSON)
                   .get();
		assertThatResponse(response).hasStatusOk();
		response.close();
		
		LOGGER.finer(ENDE);
	}
	
    // Story: Als Kunde moechte ich, dass keine neue Bestellung mit dem Inhalt des
    //        persistenten Warenkorbs angelegt wird, falls eine Artikel-ID nicht
    //        gueltig ist.
	@Test
	@InSequence(21)
	public void saveWarenkorbInvalidArtikel() throws URISyntaxException {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// Neues, client-seitiges Objekt fuer Warenkorbposition als Formular-Datensatz
		final Form form = new Form();
		form.param("artikelUri", ARTIKEL_URI + "/" + ARTIKEL_ID_INVALID)
		    .param("anzahl", "1");
		
		// When
		final Response response = getHttpsClient(LOGINNAME, PASSWORD)
				                  .target(WARENKORBPOSITION_URI)
                                  .request()
                                  .post(form(form));
		
		// Then
        assertThatResponse(response).hasStatusBadRequest();
		response.close();
	}

    // Story: Als Kunde moechte ich, dass keine neue Bestellung angelegt wird, falls
    //        der persistente Warenkorb leer ist
	@Test
	@InSequence(22)
	public void saveWarenkorbLeer() throws URISyntaxException {
		LOGGER.finer(BEGINN);
		
		// Given

		// When
		final Response response = getHttpsClient(LOGINNAME, PASSWORD)
				                  .target(BESTELLUNGEN_URI)
                                  .request()
                                  .post(null);
		
		// Then
		assertThatResponse(response).hasStatusBadRequest();
		response.close();
	}
}
