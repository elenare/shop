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

package de.shop.registrierung.jsf;

import de.shop.kundenverwaltung.domain.FamilienstandType;
import de.shop.kundenverwaltung.domain.GeschlechtType;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.util.AbstractWebTest;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.kundenverwaltung.domain.FamilienstandType.LEDIG;
import static de.shop.kundenverwaltung.domain.GeschlechtType.MAENNLICH;
import static de.shop.kundenverwaltung.domain.HobbyType.LESEN;
import static de.shop.kundenverwaltung.domain.HobbyType.SPORT;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;


/**
* @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
*/
@RunWith(Arquillian.class)
public class RegistrierenWebTest extends AbstractWebTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String NACHNAME = "Test";
	private static final String VORNAME = "Theo";
    private static final String LOGINNAME = VORNAME + '.' + NACHNAME;
    // Schieberegler fuer Kategorie: nach rechts verschieben [Pixel]
	private static final int KATEGORIE_OFFSET_X = 20;
	private static final FamilienstandType FAMILIENSTAND = LEDIG;
	private static final GeschlechtType GESCHLECHT = MAENNLICH;
	private static final boolean NEWSLETTER = true;
	private static final Collection<HobbyType> HOBBYS = new HashSet<HobbyType>() {
        // anonyme innere Klasse
        private static final long serialVersionUID = 3109256773218160485L;
        {
            add(SPORT);
            add(LESEN);
        }
    };
	private static final String EMAIL = "theo@test.de";
	private static final String PASSWORD = "p";
	private static final String PLZ = "12345";
	private static final String ORT = "Testort";
	private static final String STRASSE = "Testweg";
	private static final String HAUSNR = "1";
	private static final boolean AGB = true;
	
	private static final String NACHNAME_INVALID = "!?$%&";

	
	@Page
	private Registrieren registrieren;
    
    @Page
	private OkRegistrieren ok;

	@Test
	@InSequence(1)
	public void registrieren() {
		LOGGER.finer(BEGINN);

		// GIVEN : Link anklicken
		indexPage.clickRegistrieren();
		
		// WHEN : Forumular ausfuellen
		registrieren.inputLoginname(LOGINNAME)
                    .inputNachname(NACHNAME)
		            .inputVorname(VORNAME)
				    .schiebeKategorie(browser, KATEGORIE_OFFSET_X)
		            .clickFamilienstand(FAMILIENSTAND)
		            .clickGeschlecht(GESCHLECHT)
		            .clickNewsletter(NEWSLETTER)
		            .clickHobbys(HOBBYS)
		            .inputEmail(EMAIL)
		            .inputPassword(PASSWORD)
		            .inputPasswordWdh(PASSWORD)
		            .inputPlz(PLZ)
		            .inputOrt(ORT)
		            .inputStrasse(STRASSE)
		            .inputHausnr(HAUSNR)
		            .clickAgb(AGB)
		            .clickAnlegenButton();
		
		// THEN : Die Bestaetigungsseite wird nach erfolgreichem Registrieren angezeigt
		ok.assertOK(LOGINNAME);
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(2)
    @Ignore("Client-side Validation (CSV) funktioniert nicht mit internationalisierten Fehlermeldungen bei Bean Validation")
	public void registrierenInvalid() {
		LOGGER.finer(BEGINN);
		
		// GIVEN : Link anklicken
		indexPage.clickRegistrieren();
		
		// WHEN : Forumular mit ungueltigem Nachnamen ausfuellen und in das Vorname-Feld wechseln
		registrieren.inputNachname(NACHNAME_INVALID)
		            .inputVorname(VORNAME);
        
		// THEN : Fehlermeldung "Bei Nachnamen ist nach einem " wegen des ungueltigen Nachnamens
        registrieren.assertError("fehlermeldungenNachname", "Bei Nachnamen ist nach einem ");
		
		LOGGER.finer(ENDE);
	}
}
