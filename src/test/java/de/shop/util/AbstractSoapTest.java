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

import javax.xml.ws.BindingProvider;
import org.junit.Before;
import org.junit.BeforeClass;

import static de.shop.util.TestConstants.KEYSTORE_TYPE_SOAP;
import static de.shop.util.TestConstants.TRUSTSTORE_PASSWORD;
import static de.shop.util.TestConstants.TRUSTSTORE_PATH_SOAP;
import static javax.xml.ws.BindingProvider.PASSWORD_PROPERTY;
import static javax.xml.ws.BindingProvider.USERNAME_PROPERTY;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public abstract class AbstractSoapTest extends AbstractClientTest {
	@BeforeClass
	public static void init() {
		System.setProperty("javax.net.ssl.trustStore", TRUSTSTORE_PATH_SOAP.toString());
		System.setProperty("javax.net.ssl.trustStorePassword", String.valueOf(TRUSTSTORE_PASSWORD));
		System.setProperty("javax.net.ssl.trustStoreType", KEYSTORE_TYPE_SOAP);
	}
    
    @Before
    @Override
    public void assertThatWildFlyIsUpAndRunning() {
        super.assertThatWildFlyIsUpAndRunning();
    }
    
    protected void login(String loginname, String password, Object proxy) {
        final BindingProvider bp = (BindingProvider) proxy;
        bp.getRequestContext().put(USERNAME_PROPERTY, loginname);
        bp.getRequestContext().put(PASSWORD_PROPERTY, password);
    }
}
