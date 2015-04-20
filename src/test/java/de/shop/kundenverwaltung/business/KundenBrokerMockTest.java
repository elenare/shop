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

import de.shop.iam.business.IdentityAccessManagement;
import de.shop.iam.domain.Adresse;
import de.shop.iam.domain.IdentityVO;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.util.AdresseBuilder;
import de.shop.kundenverwaltung.util.IdentityBuilder;
import de.shop.kundenverwaltung.util.PrivatkundeBuilder;
import de.shop.util.AbstractBrokerMockTest;
import de.shop.util.persistence.FileHelper;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.logging.Logger;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.Instance;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.NUR_KUNDE;
import static de.shop.kundenverwaltung.domain.AbstractKunde.BY_LOGINNAME;
import static de.shop.kundenverwaltung.util.KundeAssert.assertThatKunde;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class KundenBrokerMockTest extends AbstractBrokerMockTest {
    private static final String PASSWORD = "p";
    private static final String NACHNAME = "Test";
    private static final String VORNAME = "Theo";
    private static final String LOGINNAME = VORNAME + '.' + NACHNAME;
    private static final String EMAIL = "theo@test.de";
    private static final Date SEIT = new Date();
    private static final String PLZ = "12345";
    private static final String ORT = "Testort";
    private static final String STRASSE = "Testweg";
    private static final String HAUSNR = "1";

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    @Mock
    private EntityManager mockedEm;
    
    @Mock
    private TypedQuery<AbstractKunde> mockedQuery;
    
    @Mock
    private Instance<IdentityAccessManagement> iamInstance;
    
    @Mock
    private Instance<FileHelper> fileHelperInstance;
    
    @Mock
    private Instance<ManagedExecutorService> managedExecutorServiceInstance;
	
    // Story: Als ein ... moechte ich einen Kunden zu einem gegebenen Loginnamen suchen
	@Test
	@InSequence(1)
	public void findByLoginname() {
		LOGGER.finer("findByLoginname " + BEGINN);
		
		// Given
        
        // new statt @Inject: injizierte Attribute (z.B. EntityManager) sind null
	    final KundenBroker kundenBroker = new KundenBroker(mockedEm, iamInstance, fileHelperInstance, managedExecutorServiceInstance);
        final AbstractKunde mockedKunde = getMockedKundeByLoginname(LOGINNAME);
        mockSingletonQuery(BY_LOGINNAME, mockedKunde);
        
		// When
		final AbstractKunde kunde = kundenBroker.findByLoginname(LOGINNAME, NUR_KUNDE).get();
		
		// Then
        assertThatKunde(kunde).hasLoginname(LOGINNAME);
		LOGGER.finer("findByLoginname " + ENDE);
	}
    
    private AbstractKunde getMockedKundeByLoginname(String loginname) {
        final AbstractKunde kunde = getMockedKunde();
        kunde.setLoginname(loginname);
        return kunde;
    }
	
    private AbstractKunde getMockedKunde() {
        final Adresse adresse = new AdresseBuilder()
                                .plz(PLZ)
                                .ort(ORT)
                                .strasse(STRASSE)
                                .hausnr(HAUSNR)
                                .build();
        final IdentityVO identity = new IdentityBuilder()
                                    .loginname(LOGINNAME)
                                    .password(PASSWORD)
                                    .passwordWdh(PASSWORD)
                                    .nachname(NACHNAME)
                                    .vorname(VORNAME)
                                    .email(EMAIL)
                                    .adresse(adresse)
                                    .build();
        final AbstractKunde kunde = new PrivatkundeBuilder()
                                    .identity(identity)
                                    .seit(SEIT)
                                    .build();
        return kunde;
    }
    
    private <T extends AbstractKunde> void mockSingletonQuery(String name, T result) {
        given(mockedEm.createNamedQuery(name, AbstractKunde.class)).willReturn(mockedQuery);
        given(mockedQuery.setParameter(anyString(), anyObject())).willReturn(mockedQuery);
        given(mockedQuery.getSingleResult()).willReturn(result);
    }
    
//    private void mockQuery(String name, List<AbstractKunde> result) {
//        given(mockedEm.createNamedQuery(name, AbstractKunde.class)).willReturn(mockedQuery);
//        given(mockedQuery.setParameter(anyString(), anyObject())).willReturn(mockedQuery);
//        given(mockedQuery.getResultList()).willReturn(result);
//    }
}
