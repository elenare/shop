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

import de.shop.kundenverwaltung.business.KundenBrokerMockTest;
import de.shop.kundenverwaltung.util.AdresseBuilder;
import de.shop.kundenverwaltung.util.IdentityBuilder;
import de.shop.kundenverwaltung.util.KundeAssert;
import de.shop.kundenverwaltung.util.PrivatkundeBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;

import static org.mockito.MockitoAnnotations.initMocks;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public abstract class AbstractBrokerMockTest {
	// Testklassen fuer Business-Tests
	private static final Class<?>[] TEST_CLASSES = { AbstractBrokerMockTest.class,
                                                     KundenBrokerMockTest.class,
                                                     AdresseBuilder.class,
                                                     IdentityBuilder.class,
                                                     PrivatkundeBuilder.class,
                                                     KundeAssert.class
                                                    };
	
	@Deployment
    // https://docs.jboss.org/author/display/ARQ/Servlet+3.0
	@OverProtocol(value = "Servlet 3.0")
	protected static Archive<?> deployment() {
		return ArchiveBuilder.getInstance().getArchive(TEST_CLASSES);
	}
	
	@Before
	public void before() {
		initMocks(this);
	}
}
