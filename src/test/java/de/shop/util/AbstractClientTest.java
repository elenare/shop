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

import javax.ws.rs.core.Response;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.shrinkwrap.api.Archive;

import static de.shop.util.ResponseAssert.assertThatResponse;
import static de.shop.util.TestConstants.HOST;
import static de.shop.util.TestConstants.TEST_WAR;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public abstract class AbstractClientTest {
    private static final int MANAGEMENT_PORT = 9990;
    private static final String MANAGEMENT_REALM = "ManagementRealm";
    private static final String DIGEST_AUTH_SCHEME = "Digest";
    
	private static ResteasyClientBuilder resteasyClientBuilder;
	
	
	@Deployment(name = TEST_WAR, testable = false) // Tests laufen nicht im Container
	// https://docs.jboss.org/author/display/ARQ/Servlet+3.0
    @OverProtocol(value = "Servlet 3.0")
	protected static Archive<?> deployment() {
		return ArchiveBuilder.getInstance().getArchive();
	}
    
    // auch verwendet in AbstractResourceTest
    protected static void initResteasyClientBuilder() {
        if (resteasyClientBuilder == null) {
            resteasyClientBuilder = new ResteasyClientBuilder();
        }
	}
    
    protected void assertThatWildFlyIsUpAndRunning() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        final Credentials credentials = new UsernamePasswordCredentials("zimmermann", "pass123.");
        credentialsProvider.setCredentials(new AuthScope(HOST, MANAGEMENT_PORT, MANAGEMENT_REALM, DIGEST_AUTH_SCHEME), credentials);
        final HttpClient httpclient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
        final ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(httpclient, true);

        initResteasyClientBuilder();
        
        // Test, ob WildFly laeuft
        Response response = resteasyClientBuilder
                            .httpEngine(engine)
                            .build()
                            .target("http://" + HOST + ":" + MANAGEMENT_PORT + "/management")
                            .queryParam("operation", "attribute")
                            .queryParam("name", "server-state")
                            .request(APPLICATION_JSON)
                            .get();
        assertThatResponse(response).hasStatusOk();
        assertThat(response.readEntity(String.class)).isEqualTo("\"running\"");
        
        // Sicherstellen, dass es das Deployment shop.war gibt
        response = resteasyClientBuilder
                   .httpEngine(engine)
                   .build()
                   .target("http://" + HOST + ":" + MANAGEMENT_PORT + "/management/deployment-info")
                   .request(APPLICATION_JSON)
                   .get();
        
        assertThatResponse(response).hasStatusOk();
        assertThat(response.readEntity(String.class)).contains(TEST_WAR);
    }
    
	static ResteasyClientBuilder getResteasyClientBuilder() {
		return resteasyClientBuilder;
	}
}
