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

package de.shop.artikelverwaltung.business;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.util.ArtikelBuilder;
import de.shop.util.AbstractBrokerTest;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.artikelverwaltung.domain.Artikel.MAX_RATING;
import static de.shop.artikelverwaltung.util.ArtikelAssert.assertThatArtikel;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class ArtikelBrokerTest extends AbstractBrokerTest {
	private static final String BEZEICHNUNG = "Bezeichnung neuer Artikel";
	private static final BigDecimal PREIS = new BigDecimal("100.11");
    private static final int RATING = MAX_RATING - 1;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private ArtikelBroker artikelBroker;
	
	@Inject
	private KatalogBroker katalogBroker;

    // Story: Als ... moechte ich einen neuen Artikel abspeichern
	@Test
	@InSequence(1)
	public void save() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	                          SystemException, NotSupportedException {
		LOGGER.finer("save " + BEGINN);
		
		// Given
        Artikel artikel = new ArtikelBuilder()
                          .bezeichnung(BEZEICHNUNG)
                          .preis(PREIS)
                          .rating(RATING)
                          .build();
		
		// When
		
		// Test: An der Web-Oberflaeche wird ein Artikel in mehrerem Benutzerinteraktionen
		//       und Transaktionen komponiert
		
		trans.begin();
		artikel = artikelBroker.save(artikel);
		trans.commit();
		
		// Then
        assertThatArtikel(artikel)
            .hasPositiveId()
            .hasBezeichnung(BEZEICHNUNG)
            .hasPreis(PREIS)
            .hasRating(RATING)
            .isNichtAusgesondert();

		trans.begin();
		final Artikel artikelFind = katalogBroker.findById(artikel.getId()).get();
		trans.commit();
		assertThatArtikel(artikelFind)
            .isEqualTo(artikel);

		LOGGER.finer("save " + ENDE);
	}
}
