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

package de.shop.util.rest;

import de.shop.util.interceptor.Log;
import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.HttpHeaders;

import static java.util.logging.Level.FINER;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@ApplicationScoped
@Log
public class Messages {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private static final String APPLICATION_MESSAGES = "/ApplicationMessages";
    private static final List<Locale> LOCALES_DEFAULT = Arrays.asList(Locale.ENGLISH);
    private static final int LOCALE_LENGTH = 2;
    
    @Resource(name = "locales")
    private String locales;

    private ResourceBundle defaultBundle;
    
    private Map<Locale, ResourceBundle> bundles;
    // z.B. "en" als Schluessel auch fuer en_US
    private Map<String, ResourceBundle> bundlesLanguageStr;
    
    Messages() {
        super();
    }
    
    @PostConstruct
    private void postConstruct() {
        List<Locale> localesList;
        if (locales == null) {
            localesList = LOCALES_DEFAULT;
        } else {
            final Locale.Builder localeBuilder = new Locale.Builder();
            localesList = new ArrayList<>();
            Arrays.stream(locales.split(","))
                  .forEach(localeStr -> {
                try {
                    localeBuilder.setLanguage(localeStr);
                    localesList.add(localeBuilder.build());
                } catch (IllformedLocaleException e) {
                    if (LOGGER.isLoggable(FINER)) {
                        LOGGER.log(FINER, e.getMessage(), e);
                    }
                    LOGGER.warning("web.xml: " + localeStr + " ist kein gueltiger Sprachcode");
                }
            });
        }
        LOGGER.info("Locales fuer REST: " + localesList);
        
        bundles = new HashMap<>();
        bundlesLanguageStr = new HashMap<>();
        final Set<String> languages = new HashSet<>();
        localesList.forEach(locale -> {
            final ResourceBundle bundle = ResourceBundle.getBundle(APPLICATION_MESSAGES, locale);
            bundles.put(locale, bundle);
            
            String localeStr = locale.toString();
            if (localeStr.length() > LOCALE_LENGTH) {
                localeStr = localeStr.substring(0, LOCALE_LENGTH);
                if (!languages.contains(localeStr)) {
                    bundlesLanguageStr.put(localeStr, bundle);
                    languages.add(localeStr);
                }
                
            }
        });
        
        defaultBundle = bundles.get(localesList.get(0));
    }
    
    public String getMessage(HttpHeaders headers, String key, Object... args) {
        final List<Locale> acceptableLocales = headers == null ? new ArrayList<>(0) : headers.getAcceptableLanguages();
        final ResourceBundle bundle = getBundle(acceptableLocales);
        
        final String pattern = bundle.getString(key);
        final Locale locale = acceptableLocales == null || acceptableLocales.isEmpty()
                              ? Locale.getDefault()
                              : acceptableLocales.get(0);
        final MessageFormat messageFormat = new MessageFormat(pattern, locale);
        return messageFormat.format(args);
    }
    
    private ResourceBundle getBundle(List<Locale> locales) {
        ResourceBundle bundle = null;
        
        for (Locale locale : locales) {                                //NOSONAR
            bundle = bundles.get(locale);
            if (bundle != null) {
                break;
            }
            // wenn es z.B. "en_US" nicht gibt, dann evtl. nur "en"
            String localeStr = locale.toString();
            if (localeStr.length() > LOCALE_LENGTH) {
                localeStr = localeStr.substring(0, LOCALE_LENGTH);
                bundle = bundlesLanguageStr.get(localeStr);
                if (bundle != null) {
                    break;
                }                
            }
        }
        
        return bundle == null ? defaultBundle : bundle;
    }
}
