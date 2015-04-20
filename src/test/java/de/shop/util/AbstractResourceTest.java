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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import static de.shop.util.TestConstants.BASIC_AUTH_SCHEME;
import static de.shop.util.TestConstants.HOST;
import static de.shop.util.TestConstants.HTTPS;
import static de.shop.util.TestConstants.HTTPS_PORT;
import static de.shop.util.TestConstants.KEYSTORE_TYPE;
import static de.shop.util.TestConstants.REALM;
import static de.shop.util.TestConstants.TIMEOUT_ESTABISH_CONNECTION;
import static de.shop.util.TestConstants.TIMEOUT_SOCKET;
import static de.shop.util.TestConstants.TLS12;
import static de.shop.util.TestConstants.TRUSTSTORE_PASSWORD;
import static de.shop.util.TestConstants.TRUSTSTORE_PATH;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public abstract class AbstractResourceTest extends AbstractClientTest {
    private static final AuthScope AUTH_SCOPE = new AuthScope(HOST, HTTPS_PORT, REALM, BASIC_AUTH_SCHEME);

    // Injizierte URL nimmt das HTTP-Protokoll und nicht HTTPS
	//@ArquillianResource
    //protected URL url;
    
    // Lambda-Expression als anonyme Methode fuer das Interface SchemePortResolver
    private static final SchemePortResolver SCHEME_PORT_RESOLVER = (host) -> {
        if (HTTPS_PORT == host.getPort()) {
            return HTTPS_PORT;
        }
        throw new ShopRuntimeException("Falscher HttpHost: " + host);
    };

    private static Registry<ConnectionSocketFactory> registry;
    private HttpClientConnectionManager httpClientConnectionManager;

    
	@BeforeClass
	public static void initHttps() {
		initResteasyClientBuilder();
		
        LayeredConnectionSocketFactory socketFactory;
        try {
			final KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);
			try (final InputStream stream = Files.newInputStream(TRUSTSTORE_PATH)) {
				trustStore.load(stream, TRUSTSTORE_PASSWORD);
			}
			
			final SSLContext sslcontext = SSLContexts.custom()
													 .useProtocol(TLS12)
													 .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
													 .build();
			socketFactory = new SSLConnectionSocketFactory(sslcontext, new DefaultHostnameVerifier());
		}
		catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException 
		       | KeyManagementException e) {
			throw new ShopRuntimeException(e);
		}
        
		registry = RegistryBuilder.<ConnectionSocketFactory>create()
								  .register(HTTPS, socketFactory)
								  .build();
	}
    
    @Before
    @Override
    public void assertThatWildFlyIsUpAndRunning() {
        super.assertThatWildFlyIsUpAndRunning();
    }
	
	@After
	public void shutdownHttpClientConnectionManager() {
		if (httpClientConnectionManager != null) {
			httpClientConnectionManager.shutdown();
		}
	}
	
	protected Client getHttpsClient() {
		return getHttpsClient(null, null);
	}
	
	protected Client getHttpsClient(String loginname, String password) {
		shutdownHttpClientConnectionManager();  // falls noch eine offene HTTP-Verbindung existiert, diese zuerst schliessen
		
		// Nur fuer genau 1 HTTP-Verbindung geeignet (und nicht fuer mehrere)
		httpClientConnectionManager = new BasicHttpClientConnectionManager(registry);
		final HttpClientBuilder clientBuilder = HttpClients.custom()
														   .setConnectionManager(httpClientConnectionManager)
														   .setSchemePortResolver(SCHEME_PORT_RESOLVER);

		if (loginname != null) {
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			final Credentials credentials = new UsernamePasswordCredentials(loginname, password);
            credentialsProvider.setCredentials(AUTH_SCOPE, credentials);
			clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
		}

		final HttpClient httpClient = clientBuilder.build();
		final ClientHttpEngine engine = new ApacheHttpClient4Engine(httpClient, true);
		return getResteasyClientBuilder().establishConnectionTimeout(TIMEOUT_ESTABISH_CONNECTION, SECONDS)
                                         .socketTimeout(TIMEOUT_SOCKET, SECONDS)
                                         .httpEngine(engine).build();
	}
}
