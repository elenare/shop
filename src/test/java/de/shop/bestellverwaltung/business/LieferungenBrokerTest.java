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

package de.shop.bestellverwaltung.business;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.util.AbstractBrokerTest;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

import static de.shop.bestellverwaltung.util.LieferungAssert.assertThatLieferung;
import static de.shop.bestellverwaltung.util.LieferungenAssert.assertThatLieferungen;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class LieferungenBrokerTest extends AbstractBrokerTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String LIEFERNR_PATTERN = "201402%";
	private static final String LIEFERNR_PATTERN_NICHT_VORHANDEN = "1888%";
	
	@Inject
	private LieferungenBroker lieferungenBroker;
	
    // Story: Als ... moechte ich eine Lieferung anhand der Liefernr. auslesen
    @Test
	@InSequence(1)
	public void findByLiefernr() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	                                    SystemException, NotSupportedException {
		LOGGER.finer("findByLiefernr " + BEGINN);

		// Given
		
		// When
		trans.begin();
		final Optional<List<Lieferung>> lieferungenOpt = lieferungenBroker.findByLiefernr(LIEFERNR_PATTERN);
        trans.commit();
		
		// Then
        assertThatLieferungen(lieferungenOpt)
            .arePresent()
            .areNotEmpty();

        // Bestellungen ueberpruefen: dazu muessen sich nachgeladen werden, weshalb eine Transaktion verwendet wird
        trans.begin();
		final String lieferNrPraefix = LIEFERNR_PATTERN.substring(0, LIEFERNR_PATTERN.length() - 2);  // '%' ausblenden
		lieferungenOpt.get().forEach(l -> {
            assertThatLieferung(l)
               .hasLiefernrStartingWith(lieferNrPraefix)
               .hasBestellungen()
               .hasBestellungenMitKunde();
		});
        trans.commit();

		LOGGER.finer("findByLiefernr " + ENDE);
	}

    // Story: Als ... moechte ich eine Lieferung anhand einer nicht vorhandenen Liefernummer auslesen
    //        und deshalb nicht auffinden
	@Test
	@InSequence(2)
	public void findByLiefernrNichtVorhanden() throws RollbackException, HeuristicMixedException,
                                                      HeuristicRollbackException, SystemException,
                                                      NotSupportedException {
		LOGGER.finer("findByLiefernrNichtVorhanden " + BEGINN);

		// Given
		
		// When
		trans.begin();
		final Optional<List<Lieferung>> lieferungenOpt = lieferungenBroker.findByLiefernr(LIEFERNR_PATTERN_NICHT_VORHANDEN);
        trans.commit();
         assertThatLieferungen(lieferungenOpt).areNotPresent();
        
		LOGGER.finer("findByLiefernrNichtVorhanden " + ENDE);
	}
}
