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

import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.AbstractResourceTest;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.kundenverwaltung.rest.KundenResource.ID_PATH_PARAM;
import static de.shop.util.ResponseAssert.assertThatResponse;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.KUNDEN_ID_URI;
import static de.shop.util.TestConstants.KUNDEN_URI;
import static de.shop.util.TestConstants.LOGINNAME_ADMIN;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class KundenResourceConcurrencyTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final long TIMEOUT = 10;

	private static final long ID_UPDATE = 320;
	private static final String NEUE_BEMERKUNGEN = "Neue Bemerkungen";
	private static final String NEUE_BEMERKUNGEN_2 = "Neue Bemerkungen 2";
	private static final long ID_DELETE1 = 322;
	private static final long ID_DELETE2 = 324;

    // Story: Als ein Admin moechte ich einen 409 Fehler erhalten, wenn ich ein konkurrierendes Update
    //        zu einem vorangegangenen Update versuche
    @Test
	@InSequence(1)
	public void updateUpdate() throws InterruptedException, ExecutionException, TimeoutException {
		LOGGER.finer(BEGINN);
		
		// Given
		
		Response response1 = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				             .target(KUNDEN_ID_URI)
                             .resolveTemplate(ID_PATH_PARAM, ID_UPDATE)
                             .request()
                             .accept(APPLICATION_JSON)
                             .get();

    	final AbstractKunde kunde = response1.readEntity(AbstractKunde.class);

    	// Konkurrierendes Update
		// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuem Nachnamen bauen
		kunde.setBemerkungen(NEUE_BEMERKUNGEN_2);
		
        final Future<Void> future = CompletableFuture.supplyAsync(() -> {
			final Response response2 = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
					                   .target(KUNDEN_URI)
					                   .request()
					                   .accept(APPLICATION_JSON)
					                   .put(json(kunde));
            assertThatResponse(response2).hasStatusOkOrNoContent();
			response2.close();
            return null;
        });

    	future.get(TIMEOUT, SECONDS);   // Warten bis der "parallele" Thread fertig ist
		
		// When
    	// Fehlschlagendes Update
		// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuem Nachnamen bauen
		kunde.setBemerkungen(NEUE_BEMERKUNGEN);
		response1 = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				    .target(KUNDEN_URI)
                    .request()
                    .accept(APPLICATION_JSON)
                    .put(json(kunde));
	    	
		// Then
        assertThatResponse(response1).hasStatusConflict();
		response1.close();
		
		LOGGER.finer(ENDE);
	}
	
    // Story: Als ein Admin moechte ich einen 404 Fehler erhalten, wenn ich ein konkurrierendes Update
    //        zu einem vorangegangenen Delete versuche
	@Test
	@InSequence(2)
	public void updateDelete() throws InterruptedException, ExecutionException, TimeoutException {
		LOGGER.finer(BEGINN);
		
		// Given
		Response response1 = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				             .target(KUNDEN_ID_URI)
                             .resolveTemplate(ID_PATH_PARAM, ID_DELETE1)
                             .request()
                             .accept(APPLICATION_JSON)
                             .get();

		final AbstractKunde kunde = response1.readEntity(AbstractKunde.class);

		// Konkurrierendes Delete
        final Future<Void> future = CompletableFuture.supplyAsync(() -> {
			final Response response2 = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
					                   .target(KUNDEN_ID_URI)
					                   .resolveTemplate(ID_PATH_PARAM, ID_DELETE1)
					                   .request()
					                   .delete();
            assertThatResponse(response2).hasStatusOkOrNoContent();
			response2.close();
			return null;
		});
        
    	future.get(TIMEOUT, SECONDS);   // Warten bis der "parallele" Thread fertig ist
		
		// When
    	// Fehlschlagendes Update
		kunde.setBemerkungen(NEUE_BEMERKUNGEN);
		response1 = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				    .target(KUNDEN_URI)
                    .request()
                    .accept(APPLICATION_JSON)
                    .put(json(kunde));
			
		// Then
        assertThatResponse(response1).hasStatusNotFound();
    	response1.close();
		
		LOGGER.finer(ENDE);
	}
	
    // Story: Als ein Admin moechte ich ein konkurrierendes Delete trotz vorangegangenen Update durchfuehren
	@Test
	@InSequence(3)
	public void deleteUpdate() throws InterruptedException, ExecutionException, TimeoutException {
		LOGGER.finer(BEGINN);
		
		// Given
		Response response1 = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				             .target(KUNDEN_ID_URI)
                             .resolveTemplate(ID_PATH_PARAM, ID_DELETE2)
                             .request()
                             .accept(APPLICATION_JSON)
                             .get();

		final AbstractKunde kunde = response1.readEntity(AbstractKunde.class);

		// Konkurrierendes Update
		kunde.setBemerkungen(NEUE_BEMERKUNGEN);
        final Future<Void> future = CompletableFuture.supplyAsync(() -> {
			final Response response2 = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
					                   .target(KUNDEN_URI)
					                   .request()
					                   .accept(APPLICATION_JSON)
					                   .put(json(kunde));
            assertThatResponse(response2).hasStatusOkOrNoContent();
			response2.close();
			return null;
		});
        
        // Warten bis der "parallele" Thread fertig ist
    	future.get(TIMEOUT, SECONDS);
		
		// When
    	// Erfolgreiches Delete trotz konkurrierendem Update
		response1 = getHttpsClient(LOGINNAME_ADMIN, PASSWORD_ADMIN)
				    .target(KUNDEN_ID_URI)
                    .resolveTemplate(ID_PATH_PARAM, ID_DELETE2)
                    .request()
                    .delete();
			
		// Then
        assertThatResponse(response1).hasStatusNoContent();
    	response1.close();
		
		LOGGER.finer(ENDE);
	}
}
