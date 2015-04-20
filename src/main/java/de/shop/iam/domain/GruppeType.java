/*
 * Copyright (C) 2014 Juergen Zimmermann, Hochschule Karlsruhe
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

package de.shop.iam.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public enum GruppeType {
    SALES(GruppeType.SALES_STRING),

    NORTHAMERICA(GruppeType.NORTHAMERICA_STRING),
    EMEA(GruppeType.EMEA_STRING),
    APAC(GruppeType.APAC_STRING),

    GERMANY(GruppeType.GERMANY_STRING),
    UK(GruppeType.UK_STRING),
    NORTHERN_EUROPE(GruppeType.NORTHERN_EUROPE_STRING),
    FRANCE(GruppeType.FRANCE_STRING),
    SEAME(GruppeType.SEAME_STRING),
    
    SOUTH(GruppeType.SOUTH_STRING),
    MIDDLE(GruppeType.MIDDLE_STRING),
    NORTH(GruppeType.NORTH_STRING);

    public static final String SALES_STRING = "Sales";
    
    public static final String NORTHAMERICA_STRING = "Northamerica";
    public static final String EMEA_STRING = "EMEA";
    public static final String APAC_STRING = "APAC";

    public static final String GERMANY_STRING = "Germany";
    public static final String UK_STRING = "UK";
    public static final String NORTHERN_EUROPE_STRING = "Northern Europe";
    public static final String FRANCE_STRING = "France";
    public static final String SEAME_STRING = "SEAME";
    
    public static final String SOUTH_STRING = "South";
    public static final String MIDDLE_STRING = "Middle";
    public static final String NORTH_STRING = "North";

    private static final Locale LOCALE_DEFAULT = Locale.getDefault();
    
    private static final Map<String, GruppeType> NAME_CACHE = new HashMap<>();
    static {
        Arrays.stream(GruppeType.values())
              .forEach(gruppe -> {
            NAME_CACHE.put(gruppe.value, gruppe);
            NAME_CACHE.put(gruppe.value.toLowerCase(LOCALE_DEFAULT), gruppe);
            NAME_CACHE.put(gruppe.value.toUpperCase(LOCALE_DEFAULT), gruppe);
        });
    }

    private final String value;
    
    private GruppeType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static GruppeType build(String value) {
        return NAME_CACHE.get(value);
    }
}
