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

package de.shop.katalog.rest;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.AbstractResourceTest;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.artikelverwaltung.rest.ArtikelResource.ID_PATH_PARAM;
import static de.shop.artikelverwaltung.util.ArtikelAssert.assertThatArtikel;
import static de.shop.util.ResponseAssert.assertThatResponse;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.KATALOG_ARTIKEL_ID_URI;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
/**
 *
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class KatalogResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final long ID = 501;
	

    // Story: Als Gast moechte ich einen Artikel anhand der ID auslesen
	@Test
	@InSequence(1)
	public void findById() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		final Response response = getHttpsClient()
				                  .target(KATALOG_ARTIKEL_ID_URI)
				                  .resolveTemplate(ID_PATH_PARAM, ID)
				                  .request()
                                  .accept(APPLICATION_JSON)
                                  .get();
		
		// Then
        assertThatResponse(response).hasStatusOk();
		final Artikel artikel = response.readEntity(Artikel.class);
		assertThatArtikel(artikel).hasId(ID);
		
		LOGGER.finer(ENDE);
	}
}
