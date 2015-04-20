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

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public final class Constants {
    public static final String REST_PATH = "/rest";
    
    // Domain-Klassen
    public static final int HASH_PRIME = 31;
    public static final int HASH_SHIFT = 32;
    
    // Zeitzone
    public static final ZoneId TIMEZONE = ZoneId.of("Europe/Berlin");
    // Offset der Zeitzone zu UTC bzw. GMT: 1 h
    public static final ZoneOffset ZONE_OFFSET = ZoneOffset.ofHours(1);
    
    // Bean Validation
    public static final String EMAIL_PATTERN = "[\\w.%-]+@[\\w.%-]+\\.[A-Za-z]{2,4}";
    
    // Header-Links
    public static final String SELF_LINK = "self";
    public static final String LIST_LINK = "list";
    public static final String ADD_LINK = "add";
    public static final String UPDATE_LINK = "update";
    public static final String REMOVE_LINK = "remove";
    public static final String FIRST_LINK = "first";
    public static final String LAST_LINK = "last";
    
    // JPA
    public static final String DEFAULT_PU = "default";
    public static final String LOADGRAPH = "javax.persistence.loadgraph";
    public static final int ERSTE_VERSION = 0;
    public static final int MAX_AUTOCOMPLETE = 10;
    
    // JSF
    public static final String JSF_INDEX = "/p/index";
    public static final String JSF_DEFAULT_ERROR = "/p/error/defaultError";
    public static final String JSF_REDIRECT_SUFFIX = "?faces-redirect=true";
    public static final int BESTELLVORGANG_TIMEOUT = 5;
    public static final int BESTAETIGUNG_TIMEOUT = 5;
    public static final TimeUnit TIMEOUT_UNIT = MINUTES;

    private Constants() {
    }
}
