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

import de.shop.util.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static de.shop.util.Strings.isNullOrEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.guardNoRequest;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.xpath;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ViewKunde extends AbstractPage {
	//=========================================================================
	// Suchformular
	//=========================================================================

	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      <p:panelGrid id="suchePanelGrid" ...>
	 *         <p:autocomplete id="kundeIdSuche" ...>
	 * HTML:
	 *   ...
	 *      <table id="suchePanelGrid">
	 *         <tbody>
	 *            <tr ...
	 *               <td ...
	 *                  <span id="kundeIdSuche">
	 *                     <input id="kundeIdSuche_input" type="text" ...>
	 * </pre>
	 */
	@FindBy(id = "kundeIdSuche_input")
	private WebElement kundeIdSucheInput;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      ...
	 *      <p:commandButton id="findButton" ...>
	 * HTML:
	 *   <button id="findButton" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement findButton;
	

	//=========================================================================
	// Gefundener Kunde
	//=========================================================================

	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      <p:tabView id="tabView" ...>
	 *         <r:tab id="stammdatenTab" ...>
	 *            <p:panelGrid id="stammdatenGrid"
	 *               <h:outputText id="kundeIdValue"
	 * HTML:
	 *   ...
	 *   <div id="tabView"
	 *      <div class="ui-tabs-panel ...
     *         <div id="tabView:stammdatenTab" ...
     *            <table id="tabView:stammdatenGrid" ...
	 *               <tbody>
     *                  <tr ...
     *                     <td ...
     *                        <span id="tabView:kundeIdValue"
	 * </pre>
	 */
	@FindBy(id = "tabView:kundeIdValue")
	private WebElement kundeId;
	
	@FindBy(id = "tabView:vorname")
	private WebElement vorname;

	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="kundeForm" jsf:prependId="false">
	 *      <p:tabView id="tabView" ...>
	 *         <r:tab id="bestellungenTab" ...>
	 *            <f:facet name="title">
	 *               <img jsf:id="bestellungenIcon"
	 * HTML:
	 *   ...
	 *   <div id="tabView"
	 *      <ul class="ui-tabs-nav ...
     *         <li class="ui-state-default ...
     *            <a href="#tabView:bestellungenTab"
	 *               <img id="tabView:bestellungenIcon"
	 * </pre>
	 */
	@FindBy(id = "tabView:bestellungenIcon")
	private WebElement bestellungenTab;
    
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="kundeForm" jsf:prependId="false">
	 *      <p:panelGrid id="buttons"
     *         <p:commandButton id="updateButton"
	 * HTML:
	 *   <button id="updateButton"
	 * </pre>
	 */
    @FindBy
    private WebElement updateButton;
	
	
	public ViewKunde suchen(String prefix, long kundeId) {
		// evtl. Vorbelegung loeschen
        kundeIdSucheInput.clear();
		
        final String kundeIdStr = String.valueOf(kundeId);
		if (isNullOrEmpty(prefix)) {
            guardAjax(kundeIdSucheInput).sendKeys(kundeIdStr);
        }
        else {
            guardAjax(kundeIdSucheInput).sendKeys(prefix);
            
            // PrimeFaces:
            // <form jsf:id="..." jsf:prependId="false">
            //    <p:autocomplete id="kundeIdSuche" ...>
            // HTML:
            // <div id="kundeIdSuche_panel"
            //    <ul class="ui-autocomplete-items ...
            //       <li class="ui-autocomplete-item ..." data-item-label="302"
            guardNoRequest(body.findElement(id("kundeIdSuche_panel"))
                               .findElement(className("ui-autocomplete-items"))
                               .findElement(xpath("li[@data-item-label='" + kundeIdStr + "']")))
            .click();
		}
		
		guardAjax(findButton).click();
		return this;
	}
	
	public ViewKunde clickBestellungenTab() {
		guardNoRequest(bestellungenTab).click();
		return this;
	}
    
    public ViewKunde assertKundeId(long kundeIdVal) {
   		assertThat(kundeId.getText()).isEqualTo(String.valueOf(kundeIdVal));
        return this;
    }
    
    public ViewKunde assertVorname(String vornameStr) {
        assertThat(vorname.getText()).isEqualTo(vornameStr);
        return this;
    }

    public void clickUpdateButton() {
        guardHttp(updateButton).click();
    }
}
