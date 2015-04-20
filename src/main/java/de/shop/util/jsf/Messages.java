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

package de.shop.util.jsf;

import de.shop.util.interceptor.Log;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import static java.util.logging.Level.FINEST;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@ApplicationScoped
@Log
public class Messages implements Serializable {
    private static final long serialVersionUID = -2209093106110666329L;
    
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final int LOCALE_LENGTH = 2;

    private transient ResourceBundle defaultBundle;
    private Locale defaultLocale;
    private transient Map<Locale, ResourceBundle> bundles;
    // z.B. "en" als Schluessel auch fuer en_US
    private transient Map<String, ResourceBundle> bundlesLanguageStr;
    
    private transient FacesContext ctx;
    
    /**
     * Package-private Default-Konstruktor f&uuml;r JSF
     */
    Messages() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param ctx zu injizierendes Objekt f&uuml;r FacesContext
     */
    @Inject
    Messages(FacesContext ctx) {
        super();
        this.ctx = ctx;
    }
    
    @PostConstruct
    private void postConstruct() {
        final Application application = ctx.getApplication();
        
        final String messageBundle = application.getMessageBundle();
        defaultLocale = application.getDefaultLocale();
        LOGGER.info("Default Locale: " + defaultLocale);
        
        defaultBundle = ResourceBundle.getBundle(messageBundle, defaultLocale);
        
        bundles = new HashMap<>();
        bundles.put(defaultLocale, defaultBundle);
        
        bundlesLanguageStr = new HashMap<>();
        String localeStr = defaultLocale.toString();
        if (localeStr.length() > LOCALE_LENGTH) {
            localeStr = localeStr.substring(0, LOCALE_LENGTH);
        }
        bundlesLanguageStr.put(localeStr, defaultBundle);
        
        final Iterator<Locale> locales = application.getSupportedLocales();
        final Set<String> languages = new HashSet<>();
        while (locales.hasNext()) {
            final Locale lc = locales.next();
            final ResourceBundle bundle = ResourceBundle.getBundle(messageBundle, lc);
            bundles.put(lc, bundle);
            
            localeStr = lc.toString();
            if (localeStr.length() > LOCALE_LENGTH) {
                localeStr = localeStr.substring(0, LOCALE_LENGTH);
            }
            if (!languages.contains(localeStr)) {
                bundlesLanguageStr.put(localeStr, bundle);
                languages.add(localeStr);
            }
        }
        LOGGER.info("Locales: " + bundles.keySet());
    }
    
    /**
     * Fuer Fehlermeldungen an der Web-Oberflaeche, die durch Exceptions verursacht werden
     * @param msgKey Schluessel in ApplicationMessages
     * @param locale Locale im Webbrowser
     * @param idUiKomponente ID, bei der in der JSF-Seite die Meldung platziert werden soll
     * @param args Werte fuer die Platzhalter in der lokalisierten Meldung aus ApplicationMessages
     */
    public void error(String msgKey, Locale locale, String idUiKomponente, Object... args) {
        createMsg(msgKey, locale, idUiKomponente, SEVERITY_ERROR, args);
    }
    
    public void warn(String msgKey, Locale locale, String idUiKomponente, Object... args) {
        createMsg(msgKey, locale, idUiKomponente, SEVERITY_WARN, args);
    }
    
    public void info(String msgKey, Locale locale, String idUiKomponente, Object... args) {
        createMsg(msgKey, locale, idUiKomponente, SEVERITY_INFO, args);
    }
    
    private void createMsg(String msgKey,
                           Locale locale,
                           String idUiKomponente,
                           Severity severity,
                           Object... args) {
        ResourceBundle bundle = bundles.get(locale);
        if (bundle == null) {
            // Sprache (z.B. "en") statt Locale (z.B. "en_US") verwenden, da die Sprache allgemeiner ist
            String localeStr = locale.toString();
            if (localeStr.length() > LOCALE_LENGTH) {
                localeStr = localeStr.substring(0, LOCALE_LENGTH);
                bundle = bundlesLanguageStr.get(localeStr);
            }
            
            if (bundle == null) {
                // Keine Texte zu aktuellen Sprache gefunden: Default-Sprache verwenden
                bundle = defaultBundle;
                locale = defaultLocale;                                //NOSONAR
            }
            
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.finest("bundle == null");
                LOGGER.finest("locale=" + locale);
                LOGGER.finest("localeStr=" + localeStr);
            }
        }
        
        String msgPattern;
        try {
            msgPattern = bundle.getString(msgKey);
        } catch (MissingResourceException e) {
            LOGGER.warning("In ApplicationMessages.properties gibt es keinen Eintrag fuer " + msgKey);
            return;
        }
        final MessageFormat formatter = new MessageFormat(msgPattern, locale);
        final String msg = formatter.format(args);
        
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("msgPattern=" + msgPattern);
            LOGGER.finest("msg=" + msg);
            LOGGER.finest("idUiKomponente=" + idUiKomponente);
        }
        
        final FacesMessage facesMsg = new FacesMessage(severity, msg, null);
        ctx.addMessage(idUiKomponente, facesMsg);
    }
}
