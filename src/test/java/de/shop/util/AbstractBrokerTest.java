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

package de.shop.util;

import de.shop.artikelverwaltung.business.ArtikelBrokerTest;
import de.shop.artikelverwaltung.util.ArtikelAssert;
import de.shop.artikelverwaltung.util.ArtikelBuilder;
import de.shop.bestellverwaltung.business.BestellungenBrokerTest;
import de.shop.bestellverwaltung.business.LieferungenBrokerTest;
import de.shop.bestellverwaltung.util.BestellungAssert;
import de.shop.bestellverwaltung.util.BestellungBuilder;
import de.shop.bestellverwaltung.util.LieferungAssert;
import de.shop.bestellverwaltung.util.LieferungenAssert;
import de.shop.kundenverwaltung.business.KundenBrokerTest;
import de.shop.kundenverwaltung.business.ReklamationenBrokerTest;
import de.shop.kundenverwaltung.util.AdresseBuilder;
import de.shop.kundenverwaltung.util.IdentityBuilder;
import de.shop.kundenverwaltung.util.KundeAssert;
import de.shop.kundenverwaltung.util.KundenAssert;
import de.shop.kundenverwaltung.util.OptionalKundenAssert;
import de.shop.kundenverwaltung.util.PrivatkundeBuilder;
import de.shop.kundenverwaltung.util.ReklamationenAssert;
import de.shop.registrierung.business.RegistrierungBrokerTest;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;

import static javax.transaction.Status.STATUS_ACTIVE;
import static javax.transaction.Status.STATUS_MARKED_ROLLBACK;
import static javax.transaction.Status.STATUS_NO_TRANSACTION;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public abstract class AbstractBrokerTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	// Testklassen fuer Business-Tests
	private static final Class<?>[] TEST_CLASSES = { AbstractBrokerTest.class,
                                                     ArtikelBrokerTest.class,
                                                     ArtikelBuilder.class,
                                                     ArtikelAssert.class,
                                                     KundenBrokerTest.class,
                                                     PrivatkundeBuilder.class,
                                                     IdentityBuilder.class,
                                                     AdresseBuilder.class,
                                                     KundeAssert.class,
                                                     KundenAssert.class,
                                                     OptionalKundenAssert.class,
                                                     RegistrierungBrokerTest.class,
                                                     ReklamationenBrokerTest.class,
                                                     ReklamationenAssert.class,
                                                     BestellungenBrokerTest.class,
                                                     BestellungBuilder.class,
                                                     BestellungAssert.class,
                                                     LieferungenBrokerTest.class,
                                                     LieferungAssert.class,
                                                     LieferungenAssert.class};
	
	@Inject
	protected UserTransaction trans;
	
	@Deployment
    // https://docs.jboss.org/author/display/ARQ/Servlet+3.0
	@OverProtocol(value = "Servlet 3.0")
	protected static Archive<?> deployment() {
		return ArchiveBuilder.getInstance().getArchive(TEST_CLASSES);
	}
	
	@Before
	public void before() throws Exception {
		int status;
		try {
			status = trans.getStatus();
		}
		catch (IllegalStateException e) {
			assertThat(e.getMessage(), is("UserTransaction is not available within the scope of a bean or method annotated with @Transactional and a Transactional.TxType other than NOT_SUPPORTED or NEVER"));
			return;
		}
		assertThat(status, is(STATUS_NO_TRANSACTION));
	}
	
	@After
	public void after() throws SystemException {
		int status;
		try {
			status = trans.getStatus();
		}
		catch (IllegalStateException e) {
			assertThat(e.getMessage(), is("UserTransaction is not available within the scope of a bean or method annotated with @Transactional and a Transactional.TxType other than NOT_SUPPORTED or NEVER"));
			return;
		}
		
		switch (status) {
			case STATUS_ACTIVE:
				// In einer Testmethode wurde eine Exception geworfen und dadurch vorzeitig beendet
				LOGGER.finer("after(): UserTransaction.getStatus() == STATUS_ACTIVE");
				trans.rollback();
				break;
				
			case STATUS_MARKED_ROLLBACK:
				// In einer Testmethode wurde eine Exception mit @ApplicationException fuer EJBs geworfen
				// und dadurch vorzeitig beendet
				LOGGER.finer("after(): UserTransaction.getStatus() == STATUS_MARKED_ROLLBACK");
				trans.rollback();
				break;
			
			default:
				break;
		}

		assertThat(trans.getStatus(), is(STATUS_NO_TRANSACTION));
	}
}
