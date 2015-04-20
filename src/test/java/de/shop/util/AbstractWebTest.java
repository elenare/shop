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

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.junit.Before;
import org.openqa.selenium.WebDriver;

import static de.shop.util.TestConstants.INDEX_PAGE;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public abstract class AbstractWebTest extends AbstractClientTest {
	@Drone
	protected WebDriver browser;           // Treiber fuer Webbrowser: Firefox, Chrome, ...
	
	@Page
	protected IndexPage indexPage;
	
	@Before
	public void before() {
        super.assertThatWildFlyIsUpAndRunning();
        
		browser.manage().deleteAllCookies();  // alle Cookies loeschen
        // FIXME https://issues.jboss.org/browse/ARQGRA-454
		//goTo(IndexPage.class);
        browser.navigate().to(INDEX_PAGE);
	}
}
