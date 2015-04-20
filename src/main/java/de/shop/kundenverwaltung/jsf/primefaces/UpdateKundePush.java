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
package de.shop.kundenverwaltung.jsf.primefaces;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;

import static de.shop.kundenverwaltung.jsf.primefaces.UpdateKundePush.UPDATE_ENDPOINT;
import static java.util.logging.Level.FINEST;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@PushEndpoint(UPDATE_ENDPOINT)
public class UpdateKundePush {
    static final String UPDATE_ENDPOINT = "/updateKunde";
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    @OnMessage(encoders = JSONEncoder.class)
    // Argument vom Typ String, InputStream oder Message
    public String onMessage(String value) {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("value = " + value);
        }
        return value;
    }
}
