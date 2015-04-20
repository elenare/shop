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

package de.shop.kundenverwaltung.business;

import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Reklamation;
import de.shop.util.AbstractBrokerTest;
import java.lang.invoke.MethodHandles;
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

import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.NUR_KUNDE;
import static de.shop.kundenverwaltung.util.KundeAssert.assertThatKunde;
import static de.shop.kundenverwaltung.util.ReklamationenAssert.assertThatReklamationen;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class ReklamationenBrokerTest extends AbstractBrokerTest {
	private static final long KUNDE_ID_MIT_REKLAMATIONEN = 301;
	private static final long KUNDE_ID_OHNE_REKLAMATION = 304;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private ReklamationenBroker reklamationenBroker;

	@Inject
	private KundenBroker kundenBroker;

	// Story: Als ein ... moechte ich die Reklamationen zu einem Kunden mit
    //        gegebener Kunde-ID suchen
	@Test
	@InSequence(1)
	public void findByKundeId() throws RollbackException, HeuristicMixedException,
                                       HeuristicRollbackException, SecurityException,
                                       IllegalStateException, SystemException, NotSupportedException {
		LOGGER.finer("findByKundeId " + BEGINN);

		// Given
		
		// When
		trans.begin();
		final Optional<List<Reklamation>> reklamationen =
                                          reklamationenBroker.findByKundeId(KUNDE_ID_MIT_REKLAMATIONEN);
        trans.commit();
		
		// Then
        assertThatReklamationen(reklamationen).isNotEmpty();
		
        trans.begin();
		final AbstractKunde kunde = kundenBroker.findById(KUNDE_ID_MIT_REKLAMATIONEN, NUR_KUNDE).get();
        trans.commit();
        assertThatKunde(kunde).isNotNull();
		assertThatReklamationen(reklamationen).eachHasKunde(kunde);
		
		LOGGER.finer("findByKundeId " + ENDE);
	}
	
	// Story: Als ein ... kann ich keine Reklamationen zu einem Kunden mit
    //        ungueltiger Kunde-ID finden
	@Test
	@InSequence(2)
    public void findByKundeIdNichtVorhanden() throws RollbackException, HeuristicMixedException,
                                                     HeuristicRollbackException, SecurityException,
                                                     IllegalStateException, SystemException, NotSupportedException {
		LOGGER.finer("findByKundeIdNichtVorhanden " + BEGINN);

		// Given
		
		// When
        trans.begin();
		final Optional<List<Reklamation>> reklamationenOpt =
                                          reklamationenBroker.findByKundeId(KUNDE_ID_OHNE_REKLAMATION);
        trans.commit();
        assertThatReklamationen(reklamationenOpt).notExisting();
        
		LOGGER.finer("findByKundeIdNichtVorhanden " + ENDE);
	}
}
