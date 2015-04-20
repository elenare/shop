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

package de.shop.kundenverwaltung.jsf;

import de.shop.util.AbstractWebTest;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.util.AbstractPage.sleep;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.LOGINNAME;
import static de.shop.util.TestConstants.LOGINNAME_ADMIN;
import static de.shop.util.TestConstants.PASSWORD;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;


/**
* @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
*/
@RunWith(Arquillian.class)
public class KundeWebTest extends AbstractWebTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String PASSWORD_FALSCH = "falschesPassword";

	private static final long ID = 301;
	private static final String ID_PREFIX = "30";
	private static final String NACHNAME = "Alpha";
	private static final String NACHNAME_PREFIX = "a";
	private static final String NACHNAME_NICHT_VORHANDEN = "Nichtvorhanden";
	
    private static final String PREFIX_VORNAME = "Al";
    
	private static final long KUNDE_ID_UPDATE = 302;
	private static final String KUNDE_ID_PREFIX_UPDATE = "30";
	private static final String VORNAME_UPDATE = "Vornameneu";
	private static final int ANZAHL_INCR_KATEGORIE = 3;
	private static final int ANZAHL_DECR_KATEGORIE = 1;
	private static final String NACHNAME_UPDATE_INVALID = "!";
	
	@Page
	private ViewKunde viewKunde;

	@Page
	private ListKunden listKunden;
	
	@Page
	private UpdateKunde updateKunde;

	@Test
	@InSequence(10)
	public void falschesPasswort() {
		LOGGER.finer(BEGINN);
		
		// GIVEN
		
		// WHEN : Mit falschem Passwort einloggen
		indexPage.login(LOGINNAME_ADMIN, PASSWORD_FALSCH)
				
		// THEN : Fehlermeldung wird angezeigt
		         .assertLoggedFailed();
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(11)
	public void sucheMitId() {
		LOGGER.finer(BEGINN);
		
		// GIVEN
		
		indexPage.login(LOGINNAME_ADMIN, PASSWORD_ADMIN)
		         .clickSucheMitKundennr();
				
		// WHEN : kundeId wird als Suchkriterium eingegeben und abgeschickt
		viewKunde.suchen(ID_PREFIX, ID)
				
		// THEN : Ein Kunde mit kundeId wird angezeigt
		         .assertKundeId(ID);
		sleep(1);
		viewKunde.clickBestellungenTab()
                 .logout(browser);
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(12)
	public void sucheMitNachname() {
		LOGGER.finer(BEGINN);

		// GIVEN : als Administrator einloggen und Link >>Suche mit Nachname<< wird angeklickt
		indexPage.login(LOGINNAME_ADMIN, PASSWORD_ADMIN)
		         .clickSucheMitNachname();
		
		// WHEN : nachname wird als Suchkriterium eingegeben und abgeschickt
		listKunden.suchen(NACHNAME_PREFIX, NACHNAME)
		
		// THEN : In der Spalte der Nachnamen steht immer der eingegebene Nachname und es gibt mindestens 1 Eintrag
		          .assertGleicherNachname(NACHNAME);
		
        // FIXME Popup funktioniert nicht in der Tabelle bei FF 36
//		// WHEN (2) : Der Details-Button zum Kunden mit der Nr. kundeId wird angeklickt
//		final ViewKundePopup kundePopup = listKunden.clickDetails(ID, browser);
//		
//		// THEN (2) : Im Popup-Fenster zum Kunden Nr. kundeId ist der nachname enthalten und
//		//            eine Bestellung hat mindestens eine Position
//		kundePopup.assertNachname(NACHNAME);
//		kundePopup.clickBestellungenTab()
//		          .clickAlleBestellungen();
//		sleep(1);
//		kundePopup.close();
        
        listKunden.logout(browser);
		
		LOGGER.finer(ENDE);
	}
    
	@Test
	@InSequence(13)
    @Ignore("Funktioniert nicht mit FF 36")
	public void sucheMitNachnameFilterVorname() {
		LOGGER.finer(BEGINN);

		// GIVEN : als Administrator einloggen und Link >>Suche mit Nachname<< wird angeklickt
		indexPage.login(LOGINNAME_ADMIN, PASSWORD_ADMIN)
		         .clickSucheMitNachname();
		listKunden.suchen(NACHNAME_PREFIX, NACHNAME)
		
		// WHEN : Filter fuer Vorname wird benutzt
                  .filterVorname(PREFIX_VORNAME)

        // THEN : In der Spalte der Vornamen stehen nur gefilterte Eintraege und es gibt mindestens 1 Eintrag
		          .assertVornamenFilter(PREFIX_VORNAME)
                  .logout(browser);
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(14)
    @Ignore("Funktioniert nicht mit FF 36")
	public void sucheMitNachnameSortiereVornamen() {
		LOGGER.finer(BEGINN);

		// GIVEN : als Administrator einloggen und Link >>Suche mit Nachname<< wird angeklickt
		indexPage.login(LOGINNAME_ADMIN, PASSWORD_ADMIN)
		         .clickSucheMitNachname();
		listKunden.suchen(NACHNAME_PREFIX, NACHNAME)
		
		// WHEN : Sortieren nach Vornamen
                  // 1. Klick: aufsteigende Sortierung (war durch die Testdaten bereits gegeben)
                  // 2. Klick: absteigende Sortierung
                  .sortiereVornamen()
                  .sortiereVornamen()

        // THEN : Die Vornamen sind alphabetisch absteigend sortiert
		          .assertVornamenAbsteigend()
                  .logout(browser);
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(15)
	public void sucheMitNachnameNichtVorhanden() {
		LOGGER.finer(BEGINN);
		
		// GIVEN : als Administrator einloggen und Link >>Suche mit Nachname<< wird angeklickt
		indexPage.login(LOGINNAME_ADMIN, PASSWORD_ADMIN)
		         .clickSucheMitNachname();
		
		// WHEN : nachname wird als Suchkriterium eingegeben und abgeschickt
		listKunden.suchen(null, NACHNAME_NICHT_VORHANDEN)
		
		// THEN : Die Meldung >>Keine Datensa"tze gefunden.<< erscheint in der Tabelle
                  .assertFehlermeldung()
                  .logout(browser);
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(20)
	public void updateKundeById() {
		LOGGER.finer(BEGINN);

		// GIVEN : als Mitarbeiter einloggen und Link >>Suche mit Nachname<< wird angeklickt
		indexPage.login(LOGINNAME, PASSWORD)
		         .clickSucheMitKundennr();
		viewKunde.suchen(ID_PREFIX, KUNDE_ID_UPDATE)
                 .clickUpdateButton();
        sleep(30);
				
		// WHEN
		
		// Der angezeigte Kundendatensatz wird aktualisiert
		updateKunde.inputVorname(VORNAME_UPDATE)
		           .incrKategorie(ANZAHL_INCR_KATEGORIE)
		           .decrKategorie(ANZAHL_DECR_KATEGORIE)
		           .clickUpdateButton();
		
		// THEN : Die Aenderungen wurden durchgefuehrt
		indexPage.clickSucheMitKundennr();
		viewKunde.suchen(KUNDE_ID_PREFIX_UPDATE, KUNDE_ID_UPDATE)
		         .assertVorname(VORNAME_UPDATE)
                 .logout(browser);
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(30)
    @Ignore("Funktioniert nicht mit FF 36")
	public void updateKundeByNachname() {
		LOGGER.finer(BEGINN);

		// GIVEN : als Mitarbeiter einloggen und Link >>Suche mit Nachname<< wird angeklickt
		indexPage.login(LOGINNAME, PASSWORD)
		         .clickSucheMitNachname();
		
		// nachname wird als Suchkriterium eingegeben und abgeschickt
		listKunden.suchen(NACHNAME_PREFIX, NACHNAME)
		
		// WHEN : Der Update-Button zum Kunden mit Nr. kundeId wird angeklickt
		          .clickUpdateButton(KUNDE_ID_UPDATE);
		
		// Der angezeigte Kundendatensatz wird aktualisiert
		updateKunde.inputVorname(VORNAME_UPDATE)
		           .incrKategorie(ANZAHL_INCR_KATEGORIE)
		           .decrKategorie(ANZAHL_DECR_KATEGORIE)
		           .clickUpdateButton();
		
		// THEN : Die Aenderungen wurden durchgefuehrt
		indexPage.clickSucheMitKundennr();
		viewKunde.suchen(KUNDE_ID_PREFIX_UPDATE, KUNDE_ID_UPDATE)
		         .assertVorname(VORNAME_UPDATE)
                 .logout(browser);
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(31)
    @Ignore("Funktioniert nicht mit FF 36")
	public void updateKundeInvalidByNachname() {
		LOGGER.finer(BEGINN);

		// GIVEN : als Mitarbeiter einloggen und Link >>Suche mit Nachname<< wird angeklickt
		indexPage.login(LOGINNAME, PASSWORD)
		         .clickSucheMitNachname();
		
		// nachname wird als Suchkriterium eingegeben und abgeschickt
		listKunden.suchen(NACHNAME_PREFIX, NACHNAME)
		
		// WHEN : Der Update-Button zum Kunden mit Nr. kundeId wird angeklickt
		          .clickUpdateButton(KUNDE_ID_UPDATE);
		
		// Der angezeigte Kundendatensatz wird aktualisiert
		updateKunde.inputNachname(NACHNAME_UPDATE_INVALID)
		           .clickBody();
		
		// THEN : Die Fehlermeldung erscheint
		updateKunde.assertFehlermeldungNachname()
                   .logout(browser);
		
		LOGGER.finer(ENDE);
	}
}
