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
import de.shop.util.AbstractPage;
import java.util.Collection;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.guardNoRequest;
import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class Registrieren extends AbstractPage {
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:panelGrid columns="3" ...>
	 *	       <input jsf:id="loginname" type="text" ...>
	 * HTML:
	 *   <input id="loginname" type="text" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement loginname;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:panelGrid columns="3" ...>
	 *	       <input jsf:id="nachname" type="text" ...>
	 * HTML:
	 *   <input id="nachname" type="text" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement nachname;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:panelGrid columns="3" ...>
	 *	       <input jsf:id="vorname" type="text" ...>
	 * HTML:
	 *   <input id="vorname" type="text" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement vorname;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *     <p:slider id="kategorieSlider"
	 * HTML:
	 *   ...
	 *   <div id="kategorieSlider"
     * </pre>
	 */
	@FindBy(id = "kategorieSlider")
	private WebElement kategorieSchieberegler;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:selectOneMenu id="familienstand"
	 * HTML:
	 *   <div id="familienstand"
	 *      ...
	 *      <span class="ui-icon ui-icon-triangle-1-s
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "familienstand"),
		@FindBy(className = "ui-icon")
	})
	private WebElement familienstandArrow;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:selectOneMenu id="familienstand"
	 *         <f:selectItem id="ledig" itemLabel="ledig" ...>
	 * HTML:
	 *   <div id="familienstand_panel"
	 *      <div
     *         <ul
     *            <li ... data-label="ledig">ledig
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "familienstand_panel"),
		@FindBy(xpath = "div/ul/li[@data-label = 'ledig']")
	})
	private WebElement ledigOption;

	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:selectOneMenu id="familienstand"
	 *         <f:selectItem id="verheiratet" itemLabel="verheiratet" ...>
	 * HTML:
	 *   <div id="familienstand_panel"
	 *      <div
     *         <ul
     *            <li ... data-label="verheiratet">verheiratet
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "familienstand_panel"),
		@FindBy(xpath = "div/ul/li[@data-label = 'verheiratet']")
	})
	private WebElement verheiratetOption;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:selectOneMenu id="familienstand"
	 *         <f:selectItem id="geschieden" itemLabel="geschieden" ...>
	 * HTML:
	 *   <div id="familienstand_panel"
	 *      <div
     *         <ul
     *            <li ... data-label="geschieden">geschieden
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "familienstand_panel"),
		@FindBy(xpath = "div/ul/li[@data-label = 'geschieden']")
	})
	private WebElement geschiedenOption;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:selectOneMenu id="familienstand"
	 *         <f:selectItem id="verwitwet" itemLabel="verwitwet" ...>
	 * HTML:
	 *   <div id="familienstand_panel"
	 *      <div
     *         <ul
     *            <li ... data-label="verwitwet">verwitwet
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "familienstand_panel"),
		@FindBy(xpath = "div/ul/li[@data-label = 'verwitwet']")
	})
	private WebElement verwitwetOption;
    
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:selectOneRadio id="geschlecht" ...>
	 *         <f:selectItem id="weiblich" itemLabel="weiblich" ...>
	 * HTML:
	 *   <table id="geschlecht" ...>
	 *      <tbody>
	 *         <tr>
	 *            <td>
	 *               <label for="geschlecht:0">weiblich</label>
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "geschlecht"),
		@FindBy(xpath = "tbody/tr/td/label[@for = 'geschlecht:0']")
	})
	private WebElement weiblichRadio;
	
	@FindBys({
		@FindBy(id = "geschlecht"),
		@FindBy(xpath = "tbody/tr/td/label[@for = 'geschlecht:1']")
	})
	private WebElement maennlichRadio;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:selectBooleanCheckbox id="newsletter"
	 * HTML:
     *   <div id="newsletter"
	 *      <span class="ui-chkbox-icon
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "newsletter"),
		@FindBy(className = "ui-chkbox-icon")
	})
	private WebElement newsletter;

	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:selectManyCheckbox id="hobbys"
	 *         <f:selectItem itemValue="SPORT"
	 * HTML:
	 *   <table id="hobbys"
	 *      <tbody>
	 *         <tr>
	 *            <td>
     *               <label for="hobbys:0">Sport</label>
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "hobbys"),
		@FindBy(xpath = "tbody/tr/td/label[@for = 'hobbys:0']")
	})
	private WebElement sportCheckbox;
	
	@FindBys({
		@FindBy(id = "hobbys"),
		@FindBy(xpath = "tbody/tr/td/label[@for = 'hobbys:1']")
	})
	private WebElement lesenCheckbox;

    @FindBys({
		@FindBy(id = "hobbys"),
		@FindBy(xpath = "tbody/tr/td/label[@for = 'hobbys:2']")
	})
	private WebElement reisenCheckbox;

	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <h:inputTest id="email" pt:type="email"
	 * HTML:
	 *   <input id="email" type="email" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement email;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:password id="password" ...>
	 * HTML:
	 *   <input id="password" type="password" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement password;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:password id="passwordWdh" ...>
	 * HTML:
	 *   <input id="passwordWdh" type="password" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement passwordWdh;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:inputText id="plz" ...>
	 * HTML:
	 *   <input id="plz" type="text" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement plz;

	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:inputText id="ort" ...>
	 * HTML:
	 *   <input id="ort" type="text" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement ort;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:inputText id="strasse" ...>
	 * HTML:
	 *   <input id="strasse" type="text" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement strasse;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:inputText id="hausnr" ...>
	 * HTML:
	 *   <input id="hausnr" type="text" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement hausnr;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p:selectBooleanCheckbox id="agb" ...>
	 * HTML:
     *   <div id="agb"
	 *      <span class="ui-chkbox-icon
	 * </pre>
	 */
    @FindBys({
		@FindBy(id = "agb"),
		@FindBy(className = "ui-chkbox-icon")
	})
	private WebElement agbCheckbox;
	
	public WebElement getNachnameFeld() {
		return nachname;
	}

	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *   <p>
	 *      <p:commandButton id="createButton" ...>
	 * HTML:
	 *   <button id="createButton" ...>
	 * </pre>
	 */
	@FindBy(id = "createButton")
	private WebElement anlegenButton;

	public Registrieren inputLoginname(String loginnameStr) {
		loginname.sendKeys(loginnameStr);
		return this;
	}
    
    public Registrieren inputNachname(String nachnameStr) {
		nachname.sendKeys(nachnameStr);
		return this;
	}
	
	public Registrieren inputVorname(String vornameStr) {
		vorname.sendKeys(vornameStr);
		return this;
	}
	
	public Registrieren schiebeKategorie(WebDriver browser, int offsetX) {
		new Actions(browser).clickAndHold(kategorieSchieberegler)
				            .moveByOffset(offsetX, 0)
				            .release()
							.build()
							.perform();
		return this;
	}
	
	public Registrieren clickFamilienstand(FamilienstandType familienstand) {
		guardNoRequest(familienstandArrow).click();
        waitGui().until().element(ledigOption).is().visible();
		switch (familienstand) {
			case LEDIG:
				ledigOption.click();
				break;
			case VERHEIRATET:
				verheiratetOption.click();
				break;
			case GESCHIEDEN:
				geschiedenOption.click();
				break;
			case VERWITWET:
				verwitwetOption.click();
				break;
			default:
				break;
		}
		return this;
	}
	
	public Registrieren clickGeschlecht(GeschlechtType geschlecht) {
		switch (geschlecht) {
			case WEIBLICH:
				weiblichRadio.click();
				break;
			case MAENNLICH:
				maennlichRadio.click();
				break;
			default:
				break;
		}
		return this;
	}
	

	public Registrieren clickNewsletter(boolean newsletterVal) {
		if (newsletterVal) {
			newsletter.click();
		}
		return this;
	}
	
	public Registrieren clickHobbys(Collection<HobbyType> hobbys) {
		hobbys.forEach(h -> {
			switch (h) {
				case SPORT:
					sportCheckbox.click();
					break;
				case LESEN:
					lesenCheckbox.click();
					break;
				case REISEN:
					reisenCheckbox.click();
					break;
				default:
					break;
			}
        });
		return this;
	}

	public Registrieren inputEmail(String emailStr) {
		email.sendKeys(emailStr);
		return this;
	}

	public Registrieren inputPassword(String passwordStr) {
		password.sendKeys(passwordStr);
		return this;
	}

	public Registrieren inputPasswordWdh(String passwordWdhStr) {
		passwordWdh.sendKeys(passwordWdhStr);
		return this;
	}

	public Registrieren inputPlz(String plzStr) {
		plz.sendKeys(plzStr);
		return this;
	}
	
	public Registrieren inputOrt(String ortStr) {
		ort.sendKeys(ortStr);
		return this;
	}
	
	public Registrieren inputStrasse(String strasseStr) {
		strasse.sendKeys(strasseStr);
		return this;
	}
	
	public Registrieren inputHausnr(String hausnrStr) {
		hausnr.sendKeys(hausnrStr);
		return this;
	}
	
	public Registrieren clickAgb(boolean agb) {
		if (agb) {
			agbCheckbox.click();
		}
		return this;
	}

	public void clickAnlegenButton() {
		guardHttp(anlegenButton).click();
	}
    
    public void assertError(String idMsg, String startsWith) {
        waitGui().until().element(body.findElement(id(idMsg))).is().visible();
        final boolean fehlermeldungExists = body.findElement(id(idMsg))
                                                .findElements(className("rf-msgs-sum"))
                                                .stream()
				                                .map(WebElement::getText)
				                                .filter(s -> s.startsWith(startsWith))
				                                .findAny()
                                                .isPresent();
        assertThat(fehlermeldungExists).isTrue();
    }
}
