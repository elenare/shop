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

import de.shop.artikelverwaltung.business.KatalogBroker;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.util.BestellungBuilder;
import de.shop.kundenverwaltung.business.KundenBroker;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.AbstractBrokerTest;
import java.lang.invoke.MethodHandles;
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

import static de.shop.bestellverwaltung.util.BestellungAssert.assertThatBestellung;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.MIT_BESTELLUNGEN;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.LOGINNAME;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class BestellungenBrokerTest extends AbstractBrokerTest {
	private static final long ARTIKEL_1_ID = 501;
	private static final int ARTIKEL_1_ANZAHL = 1;
	private static final long ARTIKEL_2_ID = 502;
	private static final int ARTIKEL_2_ANZAHL = 2;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private BestellungenBroker bestellungenBroker;
	
	@Inject
	private KundenBroker kundenBroker;
	
	@Inject
	private KatalogBroker katalogBroker;
	
    // Story: Als ... moechte ich eine neue Bestellung abspeichern
	@Test
	@InSequence(1)
	public void save() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	                          SystemException, NotSupportedException {
		LOGGER.finer("save " + BEGINN);
		
		// Given
		// Test: An der Web-Oberflaeche wird eine Bestellung in mehrerem Benutzerinteraktionen
		//       und Transaktionen komponiert
		
		trans.begin();
		final Artikel artikel1 = katalogBroker.findById(ARTIKEL_1_ID).get();
		trans.commit();
		
		trans.begin();
		final Artikel artikel2 = katalogBroker.findById(ARTIKEL_2_ID).get();
		trans.commit();
		
		Bestellung bestellung = new BestellungBuilder()
                                .addBestellposition(artikel1, ARTIKEL_1_ANZAHL)
                                .addBestellposition(artikel2, ARTIKEL_2_ANZAHL)
                                .build();
		
		trans.begin();
		final AbstractKunde kunde = kundenBroker.findByLoginname(LOGINNAME, MIT_BESTELLUNGEN).get();
		trans.commit();
		
		// When
		trans.begin();
		bestellung = bestellungenBroker.save(bestellung, kunde);
		trans.commit();
		
		// Then
        assertThatBestellung(bestellung)
            .isNotNull()
            .hasKunde(kunde);

		LOGGER.finer("save " + ENDE);
	}
}
