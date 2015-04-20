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

package de.shop.katalog.jsf;

import de.shop.util.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static de.shop.util.Strings.isNullOrEmpty;
import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ListArtikel extends AbstractPage {
	private static final String ARTIKEL_TABELLE = "artikelTabelle";
	
	//=========================================================================
	// Suchformular
	//=========================================================================

	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      <p:panelGrid id="sucheGrid" ...>
	 *         <p:inputText id="bezeichnung"  ...>
	 * HTML:
	 *   ...
	 *      <table id="sucheGrid">
	 *         <tbody>
	 *            <tr>
	 *               <td>
	 *                  <input id="bezeichnung" type="text" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement bezeichnung;
	
	/**
	 * <pre>
	 * PrimeFaces
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      ...
	 *       <p:commandButton id="findButton" ...>
	 * HTML:
	 *   <button id="findButton" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement findButton;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      ...
	 *       <p:dataTable id="artikelTabelle" ...>
	 * HTML:
	 *   <form id="form"
	 *      <div id="artikelTabelle"
     *         <tbody id="artikelTabelle_data"
	 * </pre>
	 */
	@FindBy(id = "artikelTabelle_data")
	private WebElement artikelTabelleBody;

    
    public ListArtikel suchen() {
        // evtl. Vorbelegung loeschen
        bezeichnung.clear();
        guardAjax(findButton).click();
        return this;
    }
    
    public ListArtikel suchen(String bezeichnungStr) {
        // evtl. Vorbelegung loeschen
		bezeichnung.clear();
		if (!isNullOrEmpty(bezeichnungStr)) {
			bezeichnung.sendKeys(bezeichnungStr);
		}
		guardAjax(findButton).click();
        return this;
	}
    
    public void inDenWarenkorb(int zeilennr) {
        // PrimeFaces:
		// <p:dataTable id="artikelTabelle" ...>
		//    <p:column id="buttonSpalte" ...>
		//       <p:column id="buttonSpalte"
        //          <p:commandButton id="warenkorbButton"
        // HTML:
		// <div id="artikelTabelle"
        //    <tbody id="artikelTabelle_data"
		//       <tr
		//          <td
		//             <button id="artikelTabelle:0:warenkorbButton"
		//                <span class="ui-button-icon-left
		final WebElement warenkorbIcon = artikelTabelleBody.findElement(id(ARTIKEL_TABELLE + ":" + zeilennr + ":warenkorbButton"))
                                                           .findElement(className("ui-button-icon-left"));
        guardHttp(warenkorbIcon).click();
    }
}
