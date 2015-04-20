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
import java.util.stream.IntStream;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class UpdateKunde extends AbstractPage {
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="updateKundeForm" jsf:prependId="false">
	 *      <p:panelGrid columns="3" ...>
	 *	       <p:inputText id="nachname" ...>
	 * HTML:
	 *   <input id="nachname" type="text" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement nachname;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="updateKundeForm" jsf:prependId="false">
	 *      <h:panelGrid columns="3" ...>
	 *	       <p:inputText id="vorname" ...>
	 * HTML:
	 *   <input id="vorname" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement vorname;

	/**
     * <pre>
     * PrimeFaces:
	 * <form jsf:id="updateKundeForm" jsf:prependId="false">
	 *    <h:panelGrid ...>
	 *       <p:spinner id="kategorie" ...>
	 * HTML:
	 * <span id="kategorie"
	 *    <a class="..."
	 *       <span class="...">
	 *          <span class="ui-icon ui-icon-triangle-1-n ui-c">
	 */
	@FindBys({
		@FindBy(id = "kategorie"),
		@FindBy(className = "ui-icon-triangle-1-n")
	})
	private WebElement kategorieArrowIncr;

	/**
     * <pre>
     * PrimeFaces:
	 * <form jsf:id="updateKundeForm" jsf:prependId="false">
	 *    <h:panelGrid ...>
	 *       <p:spinner id="kategorie" ...>
	 * HTML:
	 * <span id="kategorie" ...>
	 *    <a class="..."
	 *       <span class="...">
	 *          <span class="ui-icon ui-icon-triangle-1-s ui-c">
	 */
	@FindBys({
		@FindBy(id = "kategorie"),
		@FindBy(className = "ui-icon-triangle-1-s")
	})
	private WebElement kategorieArrowDecr;

	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="updateKundeForm" jsf:prependId="false">
	 *      <p:commandButton id="updateButton" ...>
	 * HTML:
	 *   <button id="updateButton" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement updateButton;
	
	/**
	 * <pre>
	 * PrimeFaces
	 *   <form jsf:id="updateKundeForm" jsf:prependId="false">
	 *      <p:messages id="fehlermeldungenNachname" ...>
	 * HTML
	 *   <span id="fehlermeldungenNachname
	 *      <span class="rf-msgs-sum">Ein Nachname...
	 */
	@FindBys({
		@FindBy(id = "fehlermeldungenNachname"),
		@FindBy(className = "rf-msgs-sum")
	})
	private WebElement fehlermeldungNachname;

	public WebElement getNachname() {
		return nachname;
	}
	
	public WebElement getVorname() {
		return vorname;
	}

	public UpdateKunde inputNachname(String nachnameStr) {
		nachname.clear();
		nachname.sendKeys(nachnameStr);
		return this;
	}
	
	public UpdateKunde inputVorname(String vornameStr) {
		vorname.clear();
		vorname.sendKeys(vornameStr);
		return this;
	}
	
	public UpdateKunde incrKategorie(int anzahl) {
        IntStream.range(0, anzahl)
                 .forEach(i -> kategorieArrowIncr.click());
		return this;
	}

	public UpdateKunde decrKategorie(int anzahl) {
		IntStream.range(0, anzahl)
                 .forEach(i -> kategorieArrowDecr.click());
		return this;
	}

	public void clickUpdateButton() {
		guardHttp(updateButton).click();
	}

    public UpdateKunde assertFehlermeldungNachname() {
        assertThat(fehlermeldungNachname.getText()).contains("Nachname");
        return this;
    }
}
