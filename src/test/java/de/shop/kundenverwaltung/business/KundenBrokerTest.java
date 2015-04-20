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

import de.shop.iam.domain.IdentityVO;
import de.shop.kundenverwaltung.business.KundenBroker.OrderByType;
import de.shop.kundenverwaltung.domain.AbstractKunde;
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

import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.MIT_BESTELLUNGEN;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.NUR_KUNDE;
import static de.shop.kundenverwaltung.util.KundeAssert.assertThatKunde;
import static de.shop.kundenverwaltung.util.KundenAssert.assertThatKunden;
import static de.shop.kundenverwaltung.util.OptionalKundenAssert.assertThatOptionalKunden;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class KundenBrokerTest extends AbstractBrokerTest {
	private static final String NACHNAME = "Alpha";
	private static final String NACHNAME_NICHT_VORHANDEN = "Beta";
	private static final long PRIVATKUNDE_ID = 301;
	private static final long ID_MIT_BESTELLUNGEN = 302;
	private static final long ID_OHNE_BESTELLUNGEN = 303;
	private static final String LOGINNAME = "adriana.alpha";
    private static final int MIN_MENGE = 2;
    private static final char SUFFIX_NEUER_NACHNAME = 'x';

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private KundenBroker kundenBroker;


    // Story: Als ein ... moechte ich einen Kunden zu gegebenem Loginnamen suchen
	@Test
	@InSequence(1)
	public void findByLoginname() throws RollbackException, HeuristicMixedException,
                                         HeuristicRollbackException, SecurityException,
                                         IllegalStateException, SystemException, NotSupportedException {
		LOGGER.finer("findByLoginname " + BEGINN);
		
		// Given
		
		// When
		trans.begin();
		final AbstractKunde kunde = kundenBroker.findByLoginname(LOGINNAME, NUR_KUNDE).get();
        trans.commit();
		
		// Then
        assertThatKunde(kunde).hasLoginname(LOGINNAME);
		LOGGER.finer("findByLoginname " + ENDE);
	}
	
    // Story: Als ein ... moechte ich Kunden zu einem gegebenen Nachnamen suchen
	@Test
	@InSequence(10)
	public void findByNachname() throws RollbackException, HeuristicMixedException,
                                        HeuristicRollbackException, SecurityException,
                                        IllegalStateException, SystemException, NotSupportedException {
		LOGGER.finer("findByNachname " + BEGINN);
		
		// Given
		
		// When
		trans.begin();
		final Optional<List<AbstractKunde>> kunden = kundenBroker.findByNachname(NACHNAME, NUR_KUNDE);
        trans.commit();
		
		// Then
        assertThatKunden(kunden.get())
            .isNotEmpty()
            .haveSameNachname(NACHNAME);

		LOGGER.finer("findByNachname " + ENDE);
	}
	
    // Story: Als ein ... moechte ich Kunden zu einem gegebenen Nachnamen suchen und dabei
    //        die Bestellungen mitladen
	@Test
	@InSequence(11)
	public void findByNachnameFetch() throws RollbackException, HeuristicMixedException,
                                             HeuristicRollbackException, SecurityException,
                                             IllegalStateException, SystemException, NotSupportedException {
		LOGGER.finer("findByNachnameFetch " + BEGINN);
		
		// Given
		
		// When
		trans.begin();
		final Optional<List<AbstractKunde>> kunden = kundenBroker.findByNachname(NACHNAME, MIT_BESTELLUNGEN);
        trans.commit();
		
		// Then
        assertThatKunden(kunden.get())
            .isNotEmpty()
            .haveSameNachname(NACHNAME)
            .haveBestellungenGeladen();

		LOGGER.finer("findByNachnameFetch " + ENDE);
	}

    // Story: Als ein ... moechte ich, dass ich keine Kunden finde, wenn ich
    //        anhand eines nicht vorhandenen Nachnamens suche
	@Test
	@InSequence(12)
    public void findByNachnameNichtVorhanden() throws RollbackException, HeuristicMixedException,
                                                      HeuristicRollbackException, SecurityException,
                                                      IllegalStateException, SystemException, NotSupportedException {
		LOGGER.finer("findByNachnameNichtVorhanden " + BEGINN);

		// Given
		
		// When
		trans.begin();
        final Optional<List<AbstractKunde>> kunden = kundenBroker.findByNachname(NACHNAME_NICHT_VORHANDEN,
                                                                                 NUR_KUNDE);
        trans.commit();
        
        assertThatOptionalKunden(kunden).isNotPresent();
        
		LOGGER.finer("findByNachnameNichtVorhanden " + ENDE);
	}

	
    // Story: Als ein ... moechte ich Kunden suchen, die Bestellungen mit einer Mindestbestellmenge haben
	@Test
	@InSequence(20)
	public void findByMindestBestellmenge() throws RollbackException, HeuristicMixedException,
                                                   HeuristicRollbackException, SecurityException,
                                                   IllegalStateException, SystemException, NotSupportedException {
		LOGGER.finer("findByMindestBestellmenge " + BEGINN);
		
		// Given
		
		// When
		trans.begin();
		final Collection<? extends AbstractKunde> kunden = kundenBroker.findByMindestBestellmenge(MIN_MENGE).get();
        trans.commit();
		
		// Then
		for (AbstractKunde k : kunden) {
            trans.begin();
			final AbstractKunde kundeMitBestellungen = kundenBroker.findById(k.getId(), MIT_BESTELLUNGEN).get();
            trans.commit();
            
            assertThatKunde(kundeMitBestellungen).hasMindestBestellmenge(MIN_MENGE);
		}
		
		LOGGER.finer("findByMindestBestellmenge " + ENDE);
	}


    // Story: Als ein ... moechte ich den Nachnamen eines vorhandenen Kunden aendern
	@Test
	@InSequence(30)
	public void updateNachname() throws RollbackException, HeuristicMixedException,
	                                    HeuristicRollbackException, SystemException,
	                                    NotSupportedException {
		LOGGER.finer("updateNachname " + BEGINN);

		// Given
        
		trans.begin();
		AbstractKunde kunde = kundenBroker.findById(PRIVATKUNDE_ID, NUR_KUNDE).get();
        trans.commit();
        assertThatKunde(kunde).isNotNull();
		
		// When
        final IdentityVO identity = kunde.getIdentity();
        final String alterNachname = identity.getNachname();
		final String neuerNachname = alterNachname + SUFFIX_NEUER_NACHNAME;
		kunde.getIdentity().setNachname(neuerNachname);
		trans.begin();
		kunde = kundenBroker.update(kunde).get();
		trans.commit();

		// Then
        assertThatKunde(kunde).hasNachname(neuerNachname);
		
        trans.begin();
		kunde = kundenBroker.findById(PRIVATKUNDE_ID, NUR_KUNDE).get();
        trans.commit();
        assertThatKunde(kunde).hasNachname(neuerNachname);

		LOGGER.finer("updateNachname " + ENDE);
	}
	
    // Story: Als ein ... moechte ich einen vorhandenen Kunden loeschen, falls
    //        er keine Bestellungen hat
	@Test
	@InSequence(40)
	public void delete() throws RollbackException, HeuristicMixedException,
	                            HeuristicRollbackException, SystemException,
	                            NotSupportedException {
		LOGGER.finer("delete " + BEGINN);

		// Given
		
        trans.begin();
		final Collection<? extends AbstractKunde> kundenVorher =
                                                  kundenBroker.findAll(NUR_KUNDE, OrderByType.UNORDERED).get();
        trans.commit();
		final AbstractKunde kunde = kundenBroker.findById(ID_OHNE_BESTELLUNGEN, MIT_BESTELLUNGEN).get();
        assertThatKunde(kunde).isNotNull();
		
		// When
		trans.begin();
		kundenBroker.delete(kunde);
		trans.commit();

		// Then
        trans.begin();
		final Optional<List<AbstractKunde>> kundenNachher =
                                            kundenBroker.findAll(NUR_KUNDE, OrderByType.UNORDERED);
        trans.commit();
		assertThatKunden(kundenNachher.get()).haveSize(kundenVorher.size() - 1);

		LOGGER.finer("delete " + ENDE);
	}
	
    // Story: Als ein ... moechte ich, dass ein Kunde nicht geloescht werden darf,
    //        wenn er mindestens eine Bestellung hat
	@Test
	@InSequence(41)
    public void deleteMitBestellungen() throws NotSupportedException, SystemException {
		LOGGER.finer("deleteMitBestellungen " + BEGINN);

		// Given
		
		final AbstractKunde kunde = kundenBroker.findById(ID_MIT_BESTELLUNGEN, MIT_BESTELLUNGEN).get();
		assertThat(kunde).isNotNull();

		// When & Then
		trans.begin();
        try {
            kundenBroker.delete(kunde);
            failBecauseExceptionWasNotThrown(KundeDeleteBestellungException.class);
        } catch (KundeDeleteBestellungException e) {
            final String msg = e.getMessage();
            assertThat(msg).contains("Kunde mit ID=" + ID_MIT_BESTELLUNGEN + " kann nicht geloescht werden: ");
            assertThat(msg).contains("Bestellung(en)");
        }
        
		trans.rollback();
		LOGGER.finer("deleteMitBestellungen " + ENDE);
	}
}
