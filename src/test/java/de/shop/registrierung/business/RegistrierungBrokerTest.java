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

package de.shop.registrierung.business;

import de.shop.iam.domain.Adresse;
import de.shop.iam.domain.IdentityVO;
import de.shop.kundenverwaltung.business.EmailExistsException;
import de.shop.kundenverwaltung.business.KundenBroker;
import de.shop.kundenverwaltung.business.KundenBroker.OrderByType;
import de.shop.kundenverwaltung.business.RegistrierungBroker;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.FamilienstandType;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.kundenverwaltung.util.AdresseBuilder;
import de.shop.kundenverwaltung.util.IdentityBuilder;
import de.shop.kundenverwaltung.util.PrivatkundeBuilder;
import de.shop.util.AbstractBrokerTest;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.NUR_KUNDE;
import static de.shop.kundenverwaltung.domain.FamilienstandType.VERHEIRATET;
import static de.shop.kundenverwaltung.domain.HobbyType.LESEN;
import static de.shop.kundenverwaltung.domain.HobbyType.REISEN;
import static de.shop.kundenverwaltung.util.KundeAssert.assertThatKunde;
import static de.shop.kundenverwaltung.util.KundenAssert.assertThatKunden;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static java.util.Calendar.FEBRUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class RegistrierungBrokerTest extends AbstractBrokerTest {
	private static final String NACHNAME = "Alphaneu";
    private static final String VORNAME = "Vornameneu";
    private static final String LOGINNAME = NACHNAME + '.' + NACHNAME;
	private static final String EMAIL = NACHNAME + "@" + NACHNAME + ".de";
	private static final int KATEGORIE = 1;
	private static final BigDecimal RABATT = new BigDecimal("0.15");
	private static final BigDecimal UMSATZ = new BigDecimal("10000000");
	private static final int TAG = 1;
	private static final int MONAT = FEBRUARY;
	private static final int JAHR = 2014;
	private static final Date SEIT = new GregorianCalendar(JAHR, MONAT, TAG).getTime();
	private static final boolean NEWSLETTER = true;
	private static final FamilienstandType FAMILIENSTAND = VERHEIRATET;
	private static final Set<HobbyType> HOBBYS = new HashSet<HobbyType>() {
        // anonyme innere Klasse
        private static final long serialVersionUID = 3109256773218160485L;
        {
            add(LESEN);
            add(REISEN);
        }
    };
	private static final boolean AGB_AKZEPTIERT = true;
	private static final String PASSWORD = "p";
	private static final String PLZ = "76133";
	private static final String ORT = "Karlsruhe";
	private static final String STRASSE = "Moltkestra\u00DFe";
	private static final String HAUSNR = "40";
    
	private static final long PRIVATKUNDE_ID = 301;
    private static final String LOGINNAME_OHNE_ADR = LOGINNAME + ".ohneAdr";
    private static final String EMAIL_OHNE_ADR = "ohneAdr." + EMAIL;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private KundenBroker kundenBroker;
    
    @Inject
	private RegistrierungBroker registrierungBroker;


    // Story: Als ein Gast moechte ich mich registrieren
	@Test
	@InSequence(1)
	public void save() throws RollbackException, HeuristicMixedException,
                              HeuristicRollbackException, SecurityException,
                              IllegalStateException, SystemException, NotSupportedException {
		LOGGER.finer("save " + BEGINN);

		// Given
		trans.begin();
		final List<? extends AbstractKunde> kundenVorher =
                                            kundenBroker.findAll(NUR_KUNDE, OrderByType.UNORDERED).get();
        trans.commit();
		
		// When
        final Adresse adresse = new AdresseBuilder()
                                .plz(PLZ)
                                .ort(ORT)
                                .strasse(STRASSE)
                                .hausnr(HAUSNR)
                                .build();
        final IdentityVO identity = new IdentityBuilder()
                                    .loginname(LOGINNAME)
                                    .enabled(true)
                                    .expirationDate(null)
                                    .password(PASSWORD)
                                    .passwordWdh(PASSWORD)
                                    .nachname(NACHNAME)
                                    .vorname(VORNAME)
                                    .email(EMAIL)
                                    .adresse(adresse)
                                    .build();
        final Privatkunde kunde = new PrivatkundeBuilder()
                                  .identity(identity)
                                  .kategorie(KATEGORIE)
                                  .rabatt(RABATT)
                                  .umsatz(UMSATZ)
                                  .seit(SEIT)
                                  .newsletter(NEWSLETTER)
                                  .familienstand(FAMILIENSTAND)
                                  .hobbys(HOBBYS)
                                  .agbAkzeptiert(AGB_AKZEPTIERT)
                                  .build();

		final Date datumVorher = new Date();
		
        trans.begin();
		final AbstractKunde result = registrierungBroker.save(kunde);
        trans.commit();
		
		// Then
        assertThatKunde(result)
            .hasPositiveId()
            .hasGreaterIdThan(kundenVorher)
            .hasNewerTimestampThan(datumVorher)
            .isExactlyInstanceOf(Privatkunde.class)
            .hasFamilienstand(FAMILIENSTAND)
            .hasHobbys(HOBBYS);

		trans.begin();
		final Optional<List<AbstractKunde>> kundenNachher =
                                            kundenBroker.findAll(NUR_KUNDE, OrderByType.UNORDERED);
        trans.commit();
        assertThatKunden(kundenNachher.get()).haveSize(kundenVorher.size() + 1);

		LOGGER.finer("save " + ENDE);
	}
	
    // Story: Als ein Gast darf ich mich nicht registrieren, falls es die Email-Adresse bereits gibt
	@Test
	@InSequence(2)
	public void saveEmailExists() throws RollbackException, HeuristicMixedException,
                                         HeuristicRollbackException, SecurityException,
                                         IllegalStateException, SystemException, NotSupportedException {
		LOGGER.finer("saveEmailExists " + BEGINN);

		// Given
        final String loginnameSuffix = ".neu";
		
		trans.begin();
		final AbstractKunde k = kundenBroker.findById(PRIVATKUNDE_ID, NUR_KUNDE).get();
        trans.commit();
		
		// When
        final IdentityVO identity = k.getIdentity();
        final IdentityVO neueIdentity = new IdentityBuilder()
                                        .loginname(identity.getLoginname() + loginnameSuffix)
                                        .password(identity.getPassword())
                                        .passwordWdh(identity.getPasswordWdh())
                                        .nachname(identity.getNachname())
                                        .vorname(identity.getVorname())
                                        .email(identity.getEmail())
                                        .adresse(identity.getAdresse())
                                        .build();
        final AbstractKunde neuerKunde = new PrivatkundeBuilder()
                                         .identity(neueIdentity)
                                         .seit(SEIT)
                                         .agbAkzeptiert(AGB_AKZEPTIERT)
                                         .build();
		
		// Then
		// Transaktion starten, damit EmailExistsException gefangen werden kann. Sonst: RollbackException durch EJB
        trans.begin();
        try {
            registrierungBroker.save(neuerKunde);
            failBecauseExceptionWasNotThrown(EmailExistsException.class);
        } catch (EmailExistsException e) {
            assertThat(e.getMessage()).isEqualTo("Die Email-Adresse " + identity.getEmail() + " existiert bereits");
        }
        
		trans.rollback();
        LOGGER.finer("saveEmailExists " + ENDE);
	}
	
    // Story: Als ein Gast darf ich mich nicht registrieren, falls es die Adresse fehlt
	@Test
	@InSequence(3)
	public void saveOhneAdresse() throws NotSupportedException, SystemException, HeuristicMixedException,
	                                     HeuristicRollbackException {
		LOGGER.finer("saveOhneAdresse " + BEGINN);

		// Given
		
		// When
        final IdentityVO identity = new IdentityBuilder()
                                    .loginname(LOGINNAME_OHNE_ADR)
                                    .enabled(true)
                                    .expirationDate(null)
                                    .password(PASSWORD)
                                    .passwordWdh(PASSWORD)
                                    .nachname(NACHNAME)
                                    .vorname(VORNAME)
                                    .email(EMAIL_OHNE_ADR)
                                    .build();
        final Privatkunde neuerKunde = new PrivatkundeBuilder()
                                       .identity(identity)
                                       .seit(SEIT)
                                       .agbAkzeptiert(AGB_AKZEPTIERT)
                                       .build();
		
		trans.begin();
        registrierungBroker.save(neuerKunde);

		try {
			trans.commit();
            failBecauseExceptionWasNotThrown(RollbackException.class);
		} catch (RollbackException e) {
			@SuppressWarnings("ThrowableResultIgnored")
			final PersistenceException persistenceException = PersistenceException.class.cast(e.getCause());
			@SuppressWarnings("ThrowableResultIgnored")
			final ConstraintViolationException constraintViolationException =
					                           ConstraintViolationException.class
					                                                       .cast(persistenceException.getCause());
			
			final Optional<ConstraintViolation<?>> violation =
					                               constraintViolationException.getConstraintViolations()
					                                                           .stream()
					                                                           .filter(v -> "Eine Benutzerkennung muss eine Adresse haben.".equals(v.getMessage()))
					                                                           .findAny();
            assertThat(violation.isPresent()).isTrue();
		}

        LOGGER.finer("saveOhneAdresse " + ENDE);
	}
}
