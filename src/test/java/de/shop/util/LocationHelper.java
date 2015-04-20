/*
 * Copyright (C) 2015 Juergen Zimmermann, Hochschule Karlsruhe
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

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public final class LocationHelper {
    private LocationHelper() {}
    
    public static long extractId(Response response) {
        final String location = response.getLocation().toString();
        final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
        assertThat(idStr)
            .overridingErrorMessage("Die ID im Location_Header muss eine Zahl sein, ist aber %s", idStr)
            .matches("\\d+");
        
		return Long.parseLong(idStr);
    }
}
