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
import org.openqa.selenium.support.FindBys;

import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.guardNoRequest;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.xpath;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class SelectArtikel extends AbstractPage {
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="selectForm" jsf:prependId="false">
     *      <p:panelGrid id="selectGrid" ...>
	 *         <p:panelGrid id="selectGrid"
	 *            <p:selectOneMenu id="artikelBezeichnung" value="#{warenkorb.artikel}" var="a">
	 *               <f:selectItems id="artikelItems" value="#{katalogModel.verfuegbareArtikel}" var="artikel" ...>
	 * HTML:
	 *   <form id="selectForm" ...>
     *      <table id="selectGrid" ...>
     *         <tbody
     *            <tr
     *               <td
     *                  <div id="artikelBezeichnung
     *                     <div class="ui-selectonemenu-trigger
     *                         <span class="ui-icon ..."></span>
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "artikelBezeichnung"),
		@FindBy(className = "ui-icon")
	})
	private WebElement selectArrow;

	/**
	 * <pre>
	 * PrimeFaces
	 *   <form jsf:id="selectForm" jsf:prependId="false">
	 *      ...
	 *       <p:commandButton id="selectButton" ...>
	 * HTML:
	 *   <form id="selectForm" ...>
	 *      <button id="selectButton" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement selectButton;
    
    public void auswaehlen(int zeilennr) {
		guardNoRequest(selectArrow).click();
		guardNoRequest(getEintrag(zeilennr)).click();
        guardHttp(selectButton).click();
    }
	
	private WebElement getEintrag(int zeilennr) {
        // position() von XPath beginnt ab 1 zu zaehlen
        // ausserdem eine zusaetzliche (Anfangs-) Zeile fuer die Anzeige in der Combobox
        final int zeilennrAb2 = zeilennr + 2;

        // PrimeFaces:
		//   <form jsf:id="selectForm" jsf:prependId="false">
		//      <p:panelGrid id="selectGrid"
		//         <p:selectOneMenu id="artikelBezeichnung" value="#{warenkorb.artikel}" var="a">
		//            <f:selectItems id="artikelItems" value="#{katalogModel.verfuegbareArtikel}" var="artikel"
		//               <p:column>#{a.bezeichnung}
		// HTML:
		//  <div id="artikelBezeichnung_panel" ...>
        //     <div class="ui-selectonemenu-items-wrapper" ...>
		//        <table class="ui-selectonemenu-items ui-selectonemenu-table ui-widget-content ui-widget ui-corner-all ui-helper-reset">
        //           <tbody>
		//              <tr class="ui-selectonemenu-item ui-selectonemenu-row ui-widget-content" data-label="Tisch 'Oval'">
        //                 <td>Tisch 'Oval'</td>
		return body.findElement(id("artikelBezeichnung_panel"))
                   .findElement(xpath("div/table/tbody/tr[" + zeilennrAb2 + ']'));
	}
}
