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

package de.shop.util;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static java.util.logging.Level.WARNING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.guardNoRequest;
import static org.jboss.arquillian.graphene.Graphene.waitAjax;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;


/**
* @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
*/
public abstract class AbstractPage {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final int MAX_LOGOUT_VERSUCHE = 5;

	//=========================================================================
	// Navigationsleiste
	//=========================================================================
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <p:menuitem id="artikelverwaltungListArtikelItem" value="Suche Artikel" ...>
	 * HTML:
	 *   <a id="artikelverwaltungListArtikelItem" ...> Suche Artikel
	 * </pre>
	 */
	@FindBy(id = "artikelverwaltungListArtikelItem")
	private WebElement linkSucheArtikel;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <p:menuitem id="artikelverwaltungListArtikelItem" value="Artikel Auswahl" ...>
	 * HTML:
	 *   <a id="artikelverwaltungListArtikelItem" ...> Artikel Auswahl
	 * </pre>
	 */
	@FindBy(id = "artikelverwaltungSelectArtikelItem")
	private WebElement linkArtikelAuswahl;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *    <p:menuitem id="kundenverwaltungViewKundeItem" ...>
	 * HTML:
	 *   <a id="kundenverwaltungViewKundeItem" ...> Suche mit Kundennr. </a>
	 * </pre>
	 */
	@FindBy(id = "kundenverwaltungViewKundeItem")
	private WebElement linkSucheMitKundennr;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <p:menuitem id="kundenverwaltungListKundenItem" ...>Suche mit Nachname
	 * HTML:
	 *   <a id="kundenverwaltungListKundenItem" ...> Suche mit Nachname </a>
	 * </pre>
	 */
	@FindBy(id = "kundenverwaltungListKundenItem")
	private WebElement linkSucheMitNachname;
	

	//=========================================================================
	// Kopfleiste
	//=========================================================================

	/**
	 * <pre>
	 * PrimeFaces:
	 *   <p:toolbar id="headerToolbar"
     *      <p:toolbarGroup id="homeGroup"
     *         <a jsf:id="homeLink"
     *            <img jsf:id="homeLogo" 
	 * HTML:
	 *   <img id="hsLogo" ...
	 * </pre>
	 */
	@FindBy(id = "homeLogo")
	private WebElement home;
	
	/**
	 * <pre>
	 * PrimeFaces;
     *   <p:toolbarGroup id="loginKontoGroup"
     *      <form jsf:id="loginFormHeader"
     *         <p:inputText id="loginnameHeader"
	 * HTML:
	 *   <form id="loginFormHeader" ...>
	 *      <input id="loginnameHeader" ...>
	 * </pre>
	 */
	@FindBy(id = "loginFormHeader")
	private WebElement loginForm;
	
	@FindBy(id = "loginnameHeader")
	private WebElement loginnameFeld;
	
	@FindBy(id = "passwordHeader")
	private WebElement passwordFeld;
	
	@FindBy(id = "loginButtonHeader")
	private WebElement loginButton;
	
	/**
	 * <pre>
	 * JSF:
	 *   <a jsf:id="registrieren" ...>Registrieren
	 * HTML:
	 *   <a id="registrieren" name="..." href="..."> Registrieren </a>
	 * </pre>
	 */
	@FindBy
	private WebElement registrieren;
    
    /**
     * PrimeFaces:
     *  <p:toolbarGroup id="headerGroup"
     *      <form jsf:id="headerForm" jsf:prependId="false"
     *         <p:menubar id="headerMenubar">
     * HTML:
     *   <div id="headerMenubar"
     */
	@FindBy
	private WebElement headerMenubar;
	
	@FindBy(tagName = "body")
	protected WebElement body;

    @FindBy(id = "artikelverwaltung:cnt")
	protected WebElement navArtikelverwaltung;
    
	public WebElement getNavArtikelverwaltung() {
		return navArtikelverwaltung;
	}

    public void clickLinkSucheArtikel() {
        guardHttp(linkSucheArtikel).click();
	}
    
    public void clickLinkArtikelAuswahl() {
        guardHttp(linkArtikelAuswahl).click();
    }

	public void clickSucheMitKundennr() {
		guardHttp(linkSucheMitKundennr).click();
	}
	
	public void clickSucheMitNachname() {
		guardHttp(linkSucheMitNachname).click();
	}
	
	public void clickRegistrieren() {
		guardHttp(registrieren).click();
	}
	
	public AbstractPage login(String loginname, String password) {
        loginnameFeld.clear();
		loginnameFeld.sendKeys(loginname);
		passwordFeld.clear();
		passwordFeld.sendKeys(password);
        guardAjax(loginButton).click();
        
        return this;
    }
	
	protected boolean isLoggedIn() {
        try {
            // PrimeFaces:
            //   <p:toolbarGroup id="headerGroup"
            //      <form jsf:id="headerForm" jsf:prependId="false"
            //         <p:menubar id="headerMenubar">
            //            <p:submenu id="meinKonto"
            // HTML:
            //   <div id="headerMenubar"
            //      <ul class="ui-menu-list ui-helper-reset">
            //         <li id="meinKonto"
            headerMenubar.findElement(id("meinKonto"));
        }
        catch (NoSuchElementException e) {
			// Login ist fehlgeschlagen, z.B. wegen eines falschen Passworts
			LOGGER.finer("Login ist fehlgeschlagen: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	protected WebElement getFehlermeldungLogin() {
		// PrimeFaces:
        //   <p:toolbarGroup id="loginKontoGroup"
        //      <form jsf:id="loginFormHeader"
        //         <p:message id="fehlermeldungLogin"
		// HTML:
		//   <form id="loginFormHeader" ...>
		//      <div id="fehlermeldungLogin" 
		//         <span class="ui-message-error-detail">Falsche Login-Daten.
		waitAjax().until().element(loginForm.findElement(className("ui-message-error-detail"))).is().visible();
		return loginForm.findElement(className("ui-message-error-detail"));
	}
	
	public void logout(WebDriver browser) {
		if (!isLoggedIn()) {
			return;
		}
		
		// PrimeFaces:
		//   <p:toolbarGroup id="headerGroup"
        //      <form jsf:id="headerForm" jsf:prependId="false"
        //         <p:menubar id="headerMenubar">
        //            <p:submenu id="meinKonto"
		// HTML:
        //   <div id="headerMenubar"
        //      <ul class="ui-menu-list ui-helper-reset">
        //         <li id="meinKonto"
        new Actions(browser).moveToElement(headerMenubar.findElement(id("meinKonto")))
							.build()
							.perform();
        
		// PrimeFaces:
		//   <p:toolbarGroup id="headerGroup"
        //      <form jsf:id="headerForm" jsf:prependId="false"
        //         <p:menubar id="headerMenubar">
        //            <p:submenu id="meinKonto"
		// HTML:
        //   <div id="headerMenubar"
        //      <ul class="ui-menu-list ui-helper-reset">
        //         <li id="meinKonto"
        //            <ul class="ui-widget-content ...
        //               <li class="ui-menuitem 
        //                   <a id="logout"
		WebElement logoutLabel;
		try {
			logoutLabel = headerMenubar.findElement(id("logout"));
		}
		catch (NoSuchElementException e) {
			LOGGER.finest("Kein Logout-Menuepunkt: " + e.getMessage());
			return;
		}
        
        int i = 1;
        for (; i <= MAX_LOGOUT_VERSUCHE; i++) {
            try {
                guardHttp(logoutLabel).click();
                break;
            } catch (ElementNotVisibleException e) {
                // Das Logout-Menue war evtl. durch eine Mausbewegung ueber ein Drop-Down-Menue verdeckt
                LOGGER.warning("Menue-Eintrag fuer Logout war nicht sichtbar: " + i + ". Versuch");
            }
        }
        assertThat(i).isLessThanOrEqualTo(MAX_LOGOUT_VERSUCHE);

		// Vorsichtsmassnahme, damit beim Testen keine Drop-Down-Menues herunterklappen
		// und evtl. andere Menuepunkte verdecken
		home.click();
	}
    
        
    public AbstractPage assertLoggedFailed() {
		assertThat(isLoggedIn()).isFalse();
		assertThat(getFehlermeldungLogin().getText()).isEqualTo("Falsche Login-Daten.");
        return this;
    }
	
	/**
	 * Hilfsmethode: irgendwo hinclicken (z.B. body), um das Blur-Ereignis auszuloesen
	 */
	public void clickBody() {
		guardNoRequest(body).click();
	}

	public static void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		}
		catch (InterruptedException e) {
			LOGGER.log(WARNING, e.getMessage());
		}
	}
}
