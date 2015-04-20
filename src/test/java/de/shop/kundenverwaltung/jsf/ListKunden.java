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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import static de.shop.util.Strings.isNullOrEmpty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.guardNoRequest;
import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.tagName;
import static org.openqa.selenium.By.xpath;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ListKunden extends AbstractPage {
	static final String TABELLE_ID = "kundenTabelle";
    static final int ANZAHL_SPALTEN = 4;
	

	//=========================================================================
	// Suchformular
	//=========================================================================
	
	/**
	 * <pre>
	 *  PrimeFaces:
	 *    <form jsf:id="form" jsf:prependId="false">
	 *       <p:autocomplete id="nachname" ...>
	 * HTML:
	 *    <span id="nachname" ...
	 *       <input id="nachname_input" ...
	 * </pre>
	 */
	@FindBy(id = "nachname_input")
	private WebElement nachnameInput;
	
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      ...
	 *       <p:commandButton id="sucheButton" ...>
	 * HTML:
	 *   <button id="sucheButton" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement sucheButton;
	

	//=========================================================================
	// Tabelle mit gefundenen Kunden
	//=========================================================================

	@FindBy
	private WebElement kundenForm;
    
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="kundenForm" jsf:prependId="false">
	 *      <r:dataTable id="kundenTabelle" ...>
	 * HTML:
	 *   <div id="kundenTabelle" ...>
	 * </pre>
	 */
	@FindBy
	private WebElement kundenTabelle;
	
	/**
	 * <pre>
     * PrimeFaces:
	 *    <form jsf:id="kundenForm" jsf:prependId="false">
	 *       <p:dataTable id="kundenTabelle" ...>
	 * HTML:
	 * <div id="kundenTabelle" ...>
     *   <table
	 *     <tbody
	 *       <tr class="ui-datatable-empty-message"
	 *         <td colspan="3">...
	 * <pre>
	 */
	@FindBys({
		@FindBy(id = TABELLE_ID),
		@FindBy(className = "ui-datatable-empty-message"),
		@FindBy(tagName = "td")
	})
	private WebElement fehlermeldung;
	
	
	
	public WebElement getNachnameFeld() {
		return nachnameInput;
	}

	public ListKunden suchen(String prefix, String nachnameStr) {
		nachnameInput.clear();    // evtl. Vorbelegung loeschen

		if (isNullOrEmpty(prefix)) {
            guardAjax(nachnameInput).sendKeys(nachnameStr);
        }
        else {
            guardAjax(nachnameInput).sendKeys(prefix);
            
            // PrimeFaces:
            // <form jsf:id="..." jsf:prependId="false">
            //    <r:autocomplete id="nachname" ...>
            // HTML:
            //    <div id="nachname_panel"
            //       <ul class="ui-autocomplete-items ...
            //          <li class="ui-autocomplete-item ...  data-item-label="Alpha"
            guardNoRequest(body.findElement(id("nachname_panel"))
                               .findElement(className("ui-autocomplete-items"))
                               .findElement(xpath("li[@data-item-label='" + nachnameStr + "']")))
            .click();
		}

		guardAjax(sucheButton).click();
		
		return this;
	}
	
	public WebElement getKundenTabelle() {
		return kundenTabelle;
	}
    
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="kundenForm" jsf:prependId="false">
	 *      <p:dataTable id="kundenTabelle" ...>
	 *         <p:column id = "vornameSpalte" ...>
	 * HTML:
     *   <div id="kundenTabelle" ...>
	 *      <table ...>
	 *         <tbody id="kundenTabelle_data" ...>
	 *            <tr ...>
	 *               <td ...>
     *                  <span id="kundenTabelle:1:vornameSpalte">
	 * </pre>
	 * @param spalteId Id der Spalte, z.B. vornameSpalte
	 * @return Liste der Eintraege in der Spalte
	 */
	public List<WebElement> getSpalteFilter(String spalteId) {
        final WebElement tbody = kundenTabelle.findElement(id(TABELLE_ID + "_data"));
        
        // Anzahl Zeilen ermitteln
        final int anzahlZeilen = tbody.findElements(tagName("td"))
                                      .size() / ANZAHL_SPALTEN;

		final List<WebElement> spalte = new ArrayList<>(anzahlZeilen);
		for (int zeilennr = 0; spalte.size() < anzahlZeilen; zeilennr++) {
			final String zelleId = TABELLE_ID + ":" + zeilennr + ":" + spalteId;
            WebElement zelle;
			
			try {
				zelle = tbody.findElement(id(zelleId));
			}
			catch (NoSuchElementException e) {
				continue;
			}
			spalte.add(zelle);
		}
		
		return spalte;
	}
	
	public ViewKundePopup clickDetails(long kundeId, WebDriver browser) {
		// Zaehlung der Zeilennr ab 0
        final int zeilennr = getZeilennr(kundeId);
		
		// Richtige Zeile gefunden: Button fuer Popup anklicken
        // PrimeFaces:
		//    <form id="kundenForm" jsf:prependId="false">
		//       <p:dataTable id="kundenTabelle" ...>
		//          <p:column id="buttons">
		//             <p:commandButton id="detailsButton" icon="..."
		// HTML:
		//    <button id="kundenTabelle:0:detailsButton" ...>
        //       <span class="ui-button-icon-left ... fa ...
		final String idDetailsButton = TABELLE_ID + ":" + zeilennr + ":detailsButton";
		final WebElement detailsButton = kundenTabelle.findElement(id(idDetailsButton)).findElement(className("ui-button-icon-left"));
        guardAjax(detailsButton).click();
        
        // Default-Timeout: 1 Sek.
        waitGui().withTimeout(5, SECONDS).until().element(kundenForm.findElement(id("tabView"))
                                                                    .findElement(xpath("ul/li/a[@href='#tabView:stammdatenTab']")))
                                                 .is().visible();
		
		final ViewKundePopup kundePopup = PageFactory.initElements(browser, ViewKundePopup.class);
		kundePopup.init(browser, kundenForm);
		
		return kundePopup;
	}
	
	private int getZeilennr(long kundeId) {
		int zeilennr = 0;
		for (;;) {
			// Zeile mit der passenden Kunde-ID ermitteln: Zaehlung ab 0
            // PrimeFaces:
			//    <form jsf:id="kundenForm" jsf:prependId="false">
			//       <p:dataTable id="kundenTabelle" ...>
			//          <p:column id="idSpalte" ...>
            //             <h:outputText id="idTxt" .../>
			// HTML:
			//    <td ...>
            //       <span id="kundenTabelle:0:idTxt">301
			final String idZelle = TABELLE_ID + ":" + zeilennr + ":idTxt";
			WebElement zelle;
			try {
				zelle = kundenTabelle.findElement(id(idZelle));
			}
			catch (NoSuchElementException e) {
				if (zeilennr == 0) {
					zeilennr = -1;
				}
				break;   // Es gibt keine weiteren Zeilen mehr
			}
			
			final String kundeIdStr = zelle.getText();
			final long tmpKundeId = Long.parseLong(kundeIdStr);
			if (tmpKundeId == kundeId) {
				break;
			}
			
			zeilennr++;
		}
		
		return zeilennr;
	}
	
	public void clickUpdateButton(long kundeId) {
		final int zeilennr = getZeilennr(kundeId);

		// Richtige Zeile gefunden: Edit-Button anklicken
        // PrimeFaces:
		//    <form jsf:id="kundenForm" jsf:prependId="false">
		//       <p:dataTable id="kundenTabelle" ...>
		//          <p:column id="buttons">
		//             <p:buttonButton id="editButton" icon="fa ..."
		// HTML:
		//    <button id="kundenTabelle:...:editButton" ...>
		final String idButton = TABELLE_ID + ":" + zeilennr + ":editButton";
		final WebElement updateButton = kundenTabelle.findElement(id(idButton));
        guardHttp(updateButton).click();
		sleep(1);
	}
    
    public ListKunden filterVorname(String vornamePrefix) {
        // PrimeFaces:
        //    <form jsf:id="kundenForm"
        //       <p:dataTable id="kundenTabelle"
        //          <p:column id="vornameSpalte" ... filterBy="..."
        // HTML:
        //    <input id="kundenTabelle:vornameSpalte:filter"
        kundenTabelle.findElement(id(TABELLE_ID + ":vornameSpalte:filter")).sendKeys(vornamePrefix);
        sleep(1);
        return this;
    }
    
    public ListKunden sortiereVornamen() {
        // PrimeFaces:
		//    <form jsf:id="kundenForm" jsf:prependId="false">
		//       <p:dataTable id="kundenTabelle" ...>
		//          <p:column id="vornameSpalte" sortBy="..."
        // HTML:
        //    <div id="kundenTabelle"
        //       <table
        //          <thead id="kundenTabelle_head">
        //             <tr
        //                <th id="kundenTabelle:vornameSpalte"
        //                   <span class="ui-column-title">Vorname
        //                   <span class="ui-sortable-column-icon
        final WebElement sortArrows = kundenTabelle.findElement(id(TABELLE_ID + ":vornameSpalte"))
                                                   .findElement(className("ui-sortable-column-icon"));
        guardNoRequest(sortArrows).click();
        return this;
    }

    public ListKunden assertGleicherNachname(String nachname) {
		final List<WebElement> spalte = getSpalte("nachnameTxt");
		assertThat(spalte).isNotEmpty();
		// parallelStream() funktioniert nicht mit Graphene
        assertThat(spalte.stream()
		                 .map(WebElement::getText)
                         .allMatch(text -> text.equals(nachname))).isTrue();        
        return this;
    }
    
	/**
	 * <pre>
	 * PrimeFaces:
	 *   <form jsf:id="kundenForm" jsf:prependId="false">
	 *      <p:dataTable id="kundenTabelle" ...>
	 *         <p:column id = "nachnameSpalte" ...>
     *            <h:outputText id="nachnameTxt" ...>
	 * HTML:
	 *   <table id="kundenTabelle" ...>
	 *      <tbody id="kundenTabelle_data" ...>
	 *         <tr ...>
	 *            <td id="kundenTabelle:1:nachnameSpalte" ...>
     *               <span id="kundenTabelle:0:nachnameTxt">Alpha
	 * </pre>
	 * @param spalteId  Id der Spalte, z.B. nachnameSpalte
	 * @return Liste der Eintraege in der Spalte
	 */
	private List<WebElement> getSpalte(String spalteId) {
        final WebElement tbody = kundenTabelle.findElement(id(TABELLE_ID + "_data"));
        
		final List<WebElement> spalte = new ArrayList<>();
		for (int zeilennr = 0; ; zeilennr++) {
			final String zelleId = TABELLE_ID + ":" + zeilennr + ":" + spalteId;
            WebElement zelle;
			
			try {
				zelle = tbody.findElement(id(zelleId));
			}
			catch (NoSuchElementException e) {
				break;
			}
			spalte.add(zelle);
		}
		
		return spalte;
	}

    public ListKunden assertVornamenAbsteigend() {
        final List<String> vornamen = getSpalte("vornameTxt")
                                      .stream()
                                      .map(elem -> elem.getText())
                                      .collect(toList());
		assertThat(vornamen).isNotEmpty();
        if (vornamen.size() == 1) {
            return this;
        }
        
        IntStream.range(1, vornamen.size())
                 .forEach(i -> {
            final String vorname = vornamen.get(i);
            final String vorgaenger = vornamen.get(i - 1);
            assertThat(vorname.compareTo(vorgaenger)).isPositive();
        });
        return this;
    }
    
    public ListKunden assertVornamenFilter(String filterStr) {
        final List<WebElement> spalte = getSpalteFilter("vornameTxt");
		assertThat(spalte).isNotEmpty();
        assertThat(spalte.stream()
                         .map(elem -> elem.getText())
                         .allMatch(vorname -> vorname.startsWith(filterStr))).isTrue();
        return this;
    }
    
    public ListKunden assertFehlermeldung() {
        assertThat(fehlermeldung.getText()).isEqualTo("Keine Kunden gefunden.");
        return this;
    }
}
