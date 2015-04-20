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

package de.shop.bestellverwaltung.domain;

import de.shop.util.ShopRuntimeException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public enum TransportType {
    STRASSE("ST"),
    SCHIENE("SCH"),
    LUFT("L"),
    WASSER("W");
    
    private static final Locale LOCALE_DEFAULT = Locale.getDefault();
    
    private static final Map<String, TransportType> NAME_CACHE = new HashMap<>();
    static {
        Arrays.stream(TransportType.values())
              .forEach(transport -> {
            NAME_CACHE.put(transport.value, transport);
            NAME_CACHE.put(transport.name(), transport);
            NAME_CACHE.put(transport.name().toLowerCase(LOCALE_DEFAULT), transport);
        });
    }
    
    private final String value;
    
    private TransportType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static TransportType build(String value) {
        final TransportType transport = NAME_CACHE.get(value);
        if (transport != null) {
            return transport;
        }
        throw new ShopRuntimeException(value + " ist kein gueltiger Wert fuer TransportType");
    }
}
