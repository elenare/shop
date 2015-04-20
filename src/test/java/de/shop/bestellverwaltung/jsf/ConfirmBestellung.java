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

import de.shop.util.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.xpath;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ConfirmBestellung extends AbstractPage {
	private static final String POSITIONENTABELLE = "positionenTabelle";
	/**
	 * <pre>
	 * PrimeFaces
	 *   <form jsf:id="bestellungForm" jsf:prependId="false"
	 *      <p:dataTable id="positionenTabelle"
	 * HTML:
	 *   <form id="bestellungForm" ...>
	 *      <div id="positionenTabelle" ...>
     *         ...
     *            <tbody id="positionenTabelle_data"
	 * </pre>
	 */
	@FindBy(id = "positionenTabelle_data")
	private WebElement positionenTabelleBody;

	/**
	 * <pre>
	 * PrimeFaces
	 *   <form jsf:id="bestellungForm" jsf:prependId="false"
	 *      <p:dataTable id="positionenTabelle"
	 *         ...
	 *         <f:facet name="footer">
	 *            <span id="buttons">
	 *               <p:commandButton id="bestellenButton"
	 * HTML:
	 *   <form id="bestellungForm" ...>
	 *      <div id="positionenTabelle" ...>
	 *         ...
	 *         <div class="ui-datatable-footer
	 *            <button id="positionenTabelle:bestellenButton"
	 * </pre>
	 */
	@FindBy(id = "positionenTabelle:bestellenButton")
	private WebElement bestellenButton;
	
	public void bestellen() {
		guardHttp(bestellenButton).click();
	}
    
    public ConfirmBestellung assertAnzahlInZeile(int zeilennr, int soll) {
        assertThat(getAnzahl(zeilennr)).isEqualTo(soll);
        return this;
    }
	
	private int getAnzahl(int zeile) {
        // position() von XPath beginnt ab 1 zu zaehlen
        final int zeileAb1 = zeile + 1;

        // PrimeFaces:
		//   <form jsf:id="bestellungForm" jsf:prependId="false"
		//      <p:dataTable id="positionenTabelle"
		//         <p:column id="anzahlSpalte">
		// HTML:
		//   <form id="bestellungForm" ...>
		//      <div id="positionenTabelle" ...>
        //         ...
		//            <tbody id="positionenTabelle_data"
        //               <tr
		//                  <td
		final String anzahlStr = positionenTabelleBody.findElement(xpath("tr[" + zeileAb1 + "]/td[3]"))
				                                      .getText();
		return Integer.parseInt(anzahlStr);
	}
}
