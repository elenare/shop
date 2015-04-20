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

package de.shop.bestellverwaltung.jsf;

import de.shop.katalog.jsf.ListArtikel;
import de.shop.katalog.jsf.SelectArtikel;
import de.shop.util.AbstractWebTest;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.LOGINNAME;
import static de.shop.util.TestConstants.PASSWORD;

/**
* @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
*/
@RunWith(Arquillian.class)
public class BestellungWebTest extends AbstractWebTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Page
	private ListArtikel listArtikel;
	
	@Page
	private SelectArtikel selectArtikel;
	
	@Page
	private WarenkorbPage warenkorb;
	
	@Page
	private ConfirmBestellung confirmBestellung;
	
	@Page
	private ViewBestellung viewBestellung;

	@Test
	@InSequence(1)
	public void bestellen() {
		LOGGER.finer(BEGINN);
		
		// GIVEN : als Mitarbeiter einloggen
        indexPage.login(LOGINNAME, PASSWORD);
	
		// WHEN
		
		// Link >>Suche Artikel<< wird angeklickt, alle Artikel suchen und den obersten Artikel in den Warenkorb
        indexPage.clickLinkSucheArtikel();
        listArtikel.suchen()
                   .inDenWarenkorb(0);
		
		// Link >>Suche Artikel<< wird angeklickt, alle Artikel suchen und den naechsten Artikel in den Warenkorb
        indexPage.clickLinkSucheArtikel();
        listArtikel.suchen()
                   .inDenWarenkorb(1);
		
		// Im Warenkorb beim obersten Artikel die Anzahl erhoehen und dann zur Kasse gehen
		final int zeile = 0;
		final int anzahlKlicks = 2;
        warenkorb.incrAnzahl(zeile, anzahlKlicks)
                 .zurKasse();
        
        confirmBestellung.assertAnzahlInZeile(zeile, 1 + anzahlKlicks)
		                 .bestellen();
		
		// THEN
		viewBestellung.assertHeader("Bestellung")
                      .logout(browser);
		
		LOGGER.finer(ENDE);
	}
	
	@Test
    @InSequence(2)
	public void bestellenSelect() {
		LOGGER.finer(BEGINN);
		
		// GIVEN : als Mitarbeiter einloggen
        indexPage.login(LOGINNAME, PASSWORD);

		// WHEN
		
		// Link >>Artikel Auswahl<< wird angeklickt, alle Artikel suchen und den obersten Artikel in den Warenkorb
        indexPage.clickLinkArtikelAuswahl();
        indexPage.clickLinkArtikelAuswahl();
		selectArtikel.auswaehlen(0);
		
		// Link >>Artikel Auswahl<< wird angeklickt, alle Artikel suchen und den naechsten Artikel in den Warenkorb
		indexPage.clickLinkArtikelAuswahl();
		selectArtikel.auswaehlen(1);
		
		// bestellen
		final int zeile = 0;
		final int anzahlKlicks = 1;
        warenkorb.incrAnzahl(zeile, anzahlKlicks)
                 .zurKasse();
        
        confirmBestellung.assertAnzahlInZeile(zeile, 1 + anzahlKlicks)
                         .bestellen();
		
		// THEN
        viewBestellung.assertHeader("Bestellung")
                      .logout(browser);
		
		LOGGER.finer(ENDE);
	}
}
