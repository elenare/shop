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
import java.util.stream.IntStream;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.guardNoRequest;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class WarenkorbPage extends AbstractPage {
	private static final String WARENKORBTABELLE = "warenkorbTabelle";
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="warenkorbForm" jsf:prependId="false">
	 *      <p:dataTable id="warenkorbTabelle"
	 * HTML:
	 *   <form id="warenkorbForm" ...>
	 *      <table id="warenkorbTabelle" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement warenkorbTabelle;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="warenkorbForm" jsf:prependId="false">
	 *      <p:dataTable id="warenkorbTabelle"
	 *         ...
	 *         <f:facet name="footer">
	 *            <span jsf:id="buttons">
	 *               <p:commandButton id="bestellenButton"
	 * HTML:
	 *   <form id="warenkorbForm" ...>
	 *      <table id="warenkorbTabelle" ...>
	 *         <tfoot id="warenkorbTabelle:tf" ...>
	 *            <span id="warenkorbTabelle:buttons">
	 *               <button id="warenkorbTabelle:zurKasseButton" ...>
	 * </pre>
	 */
	@FindBy(id = "warenkorbTabelle:zurKasseButton")
	private WebElement zurKasseButtonButton;
	
	
    public WarenkorbPage incrAnzahl(int zeile, int anzahlKlicks) {
        IntStream.range(0, anzahlKlicks)
                 .forEach(i -> guardNoRequest(getIncButton(zeile)).click());
        return this;
    }
    
    public WebElement getIncButton(int zeilennr) {
		// PrimeFaces:
		//   <form jsf:id="warenkorbForm" jsf:prependId="false">
		//      <p:dataTable id="warenkorbTabelle"
		//         <p:column id="anzahlSpalte">
		//            <p:spinner id="anzahl" ...>
		// HTML:
		//   <form id="warenkorbForm" ...>
		//      <table id="warenkorbTabelle" ...>
		//         <tbody id="warenkorbTabelle_data" ...>
		//            <tr
		//               <td
		//                  <span id="warenkorbTabelle:0:anzahl" ...>
		//                     <a class="ui-spinner-button ui-spinner-up
		//                        <span class="rf-insp-inc" ...>
		return warenkorbTabelle.findElement(id(WARENKORBTABELLE + ":" + zeilennr + ":anzahl"))
				               .findElement(className("ui-spinner-up"));
	}
	
	public void zurKasse() {
		guardHttp(zurKasseButtonButton).click();
	}
}
