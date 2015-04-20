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

package de.shop.kundenverwaltung.domain;

import de.shop.util.ShopRuntimeException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public enum FamilienstandType {
    LEDIG("L"),
    VERHEIRATET("VH"),
    GESCHIEDEN("G"),
    VERWITWET("VW");
    
    private static final Locale LOCALE_DEFAULT = Locale.getDefault();
    
    private static final Map<String, FamilienstandType> NAME_CACHE = new HashMap<>();
    static {
        Arrays.stream(FamilienstandType.values())
              .forEach(familienstand -> {
            NAME_CACHE.put(familienstand.value, familienstand);
            NAME_CACHE.put(familienstand.name(), familienstand);
            NAME_CACHE.put(familienstand.name().toLowerCase(LOCALE_DEFAULT), familienstand);
        });
    }
    
    private final String value;
    
    private FamilienstandType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static FamilienstandType build(String value) {
        final FamilienstandType familienstand = NAME_CACHE.get(value);
        if (familienstand == null) {
            throw new ShopRuntimeException(value + " ist kein gueltiger Wert fuer FamilienstandType");
        }
        return familienstand;
    }
}
