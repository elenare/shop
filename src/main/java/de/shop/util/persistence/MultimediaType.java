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

package de.shop.util.persistence;

import de.shop.util.ShopRuntimeException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public enum MultimediaType {
    IMAGE("I"),
    VIDEO("V"),
    AUDIO("A");

    private static final Locale LOCALE_DEFAULT = Locale.getDefault();
    
    private static final Map<String, MultimediaType> NAME_CACHE = new HashMap<>();
    static {
        Arrays.stream(MultimediaType.values())
              .forEach(multimedia -> {
            NAME_CACHE.put(multimedia.value, multimedia);
            NAME_CACHE.put(multimedia.name(), multimedia);
            NAME_CACHE.put(multimedia.name().toLowerCase(LOCALE_DEFAULT), multimedia);
        });
    }

    private final String value;
    
    private MultimediaType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static MultimediaType build(String value) {
        final MultimediaType multimedia = NAME_CACHE.get(value);
        if (multimedia == null) {
            throw new ShopRuntimeException(value + " ist kein gueltiger Wert fuer MultimediaType");
        }
        return multimedia;
    }
}
