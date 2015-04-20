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

package de.shop.artikelverwaltung.rest;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.util.ArtikelBuilder;
import de.shop.util.AbstractResourceTest;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.logging.Logger;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.artikelverwaltung.domain.Artikel.MAX_RATING;
import static de.shop.artikelverwaltung.rest.ArtikelResource.ID_PATH_PARAM;
import static de.shop.artikelverwaltung.util.ArtikelAssert.assertThatArtikel;
import static de.shop.util.LocationHelper.extractId;
import static de.shop.util.ResponseAssert.assertThatResponse;
import static de.shop.util.TestConstants.ARTIKEL_URI;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.KATALOG_ARTIKEL_ID_URI;
import static de.shop.util.TestConstants.LOGINNAME_ADMIN;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
//import static org.assertj.core.api.Assertions.assertThat;


/**
 *
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class ArtikelResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String BEZEICHNUNG = "Bezeichnung neuer Artikel";
	private static final BigDecimal PREIS = new BigDecimal("100.11");
	private static final String BEZEICHNUNG_FORM = "Bezeichnung neuer Artikel (Form)";
    private static final int RATING = MAX_RATING - 1;
	
	private static final long ID_UPDATE = 502;
	private static final BigDecimal PREIS_INCR = new BigDecimal("1.11");
	
    // Story: Als Administrator moechte ich einen neuen Artikel anlegen
	@Test
	@InSequence(1)
	public void save() {
		LOGGER.finer(BEGINN);
		
		// Given
		Artikel artikel = new ArtikelBuilder()
                          .bezeichnung(BEZEICHNUNG)
                          .preis(PREIS)
                          .rating(RATING)
                          .build();
		
		// When
		Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				            .target(ARTIKEL_URI)
				            .request()
				            .post(json(artikel));
		
		// Then
        assertThatResponse(response)
            .hasStatusCreated()
            .hasPositiveNewId();
        final long id = extractId(response);
        response.close();
		
		response = getHttpsClient()
				   .target(KATALOG_ARTIKEL_ID_URI)
				   .resolveTemplate(ID_PATH_PARAM, id)
				   .request()
                   .accept(APPLICATION_JSON)
                   .get();
		artikel = response.readEntity(Artikel.class);
        assertThatArtikel(artikel)
            .hasId(id)
            .hasBezeichnung(BEZEICHNUNG)
            .hasPreis(PREIS);
		
		LOGGER.finer(ENDE);
	}
	
    // Story: Als Administrator moechte ich einen neuen Artikel mittels Formulardaten anlegen
	@Test
	@InSequence(2)
	public void saveForm() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		final Form form = new Form();
		form.param("bezeichnung", BEZEICHNUNG_FORM)
			.param("preis", String.valueOf(PREIS))
            .param("rating", String.valueOf(RATING));
		
		// When
		Response response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				            .target(ARTIKEL_URI)
				            .request()
				            .post(form(form));
		
		// Then
		assertThatResponse(response)
            .hasStatusCreated()
            .hasPositiveNewId();
        final long id = extractId(response);
		response.close();
		
		response = getHttpsClient()
				   .target(KATALOG_ARTIKEL_ID_URI)
				   .resolveTemplate(ID_PATH_PARAM, id)
				   .request()
                   .accept(APPLICATION_JSON)
                   .get();
		final Artikel artikel = response.readEntity(Artikel.class);
        assertThatArtikel(artikel)
            .hasId(id)
            .hasBezeichnung(BEZEICHNUNG_FORM)
            .hasPreis(PREIS);
		
		LOGGER.finer(ENDE);
	}
	
    // Story: Als Administrator moechte ich die Daten zu einem vorhandenen Artikel aendern
	@Test
	@InSequence(10)
	public void update() {
		LOGGER.finer(BEGINN);
		
		// Given
		Response response = getHttpsClient()
				            .target(KATALOG_ARTIKEL_ID_URI)
				            .resolveTemplate(ID_PATH_PARAM, ID_UPDATE)
				            .request()
                            .accept(APPLICATION_JSON)
                            .get();
		Artikel artikel = response.readEntity(Artikel.class);
        assertThatArtikel(artikel).hasId(ID_UPDATE);
		
		// When
		final BigDecimal preisAlt = artikel.getPreis();
		final BigDecimal preisNeu = preisAlt.add(PREIS_INCR);
		artikel.setPreis(preisNeu);
		response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				   .target(ARTIKEL_URI)
				   .request()
                   .put(json(artikel));
		
		// Then
		assertThatResponse(response).hasStatusOk();
		artikel = response.readEntity(Artikel.class);
        assertThatArtikel(artikel)
            .hasId(ID_UPDATE)
            .hasPreis(preisNeu);
		
		LOGGER.finer(ENDE);		
	}
	
    // Story: Als Administrator moechte ich die Daten zu einem vorhandenen Artikel mittels Formulardaten aendern
	@Test
	@InSequence(11)
	public void updateForm() {
		LOGGER.finer(BEGINN);
		
		// Given
		Response response = getHttpsClient()
				            .target(KATALOG_ARTIKEL_ID_URI)
				            .resolveTemplate(ID_PATH_PARAM, ID_UPDATE)
				            .request()
                            .accept(APPLICATION_JSON)
                            .get();
		Artikel artikel = response.readEntity(Artikel.class);
        assertThatArtikel(artikel).hasId(ID_UPDATE);

		
		// When
		// Aus den gelesenen Werten ein neues Formular-Objekt mit neuem Nachnamen bauen
		final BigDecimal preisAlt = artikel.getPreis();
		final BigDecimal preisNeu = preisAlt.add(PREIS_INCR);
		final Form form = new Form();
		form.param("id", String.valueOf(artikel.getId()))
		    .param("version", String.valueOf(artikel.getVersion()))
			.param("bezeichnung", artikel.getBezeichnung())
			.param("preis", String.valueOf(preisNeu))
			.param("ausgesondert", String.valueOf(artikel.isAusgesondert()));
		
		response = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				   .target(ARTIKEL_URI)
				   .request()
				   .put(form(form));
		
		// Then
		assertThatResponse(response).hasStatusOk();
		response = getHttpsClient()
				            .target(KATALOG_ARTIKEL_ID_URI)
				            .resolveTemplate(ID_PATH_PARAM, ID_UPDATE)
				            .request()
                            .accept(APPLICATION_JSON)
                            .get();
		assertThatResponse(response).hasStatusOk();
        
		artikel = response.readEntity(Artikel.class);
        assertThatArtikel(artikel)
            .hasId(ID_UPDATE)
            .hasPreis(preisNeu);
	}
}
