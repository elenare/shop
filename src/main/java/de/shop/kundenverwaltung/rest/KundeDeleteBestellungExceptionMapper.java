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

package de.shop.kundenverwaltung.rest;

import de.shop.kundenverwaltung.business.KundeDeleteBestellungException;
import de.shop.util.interceptor.Log;
import de.shop.util.rest.Messages;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Provider
@ApplicationScoped
@Log
public class KundeDeleteBestellungExceptionMapper implements ExceptionMapper<KundeDeleteBestellungException> {
    @Context
    private HttpHeaders headers;
    
    // Field Injection, weil @Context HttpHeaders durch JAX-RS nur mit Attributen moeglich ist
    @Inject
    private Messages messages;
    
    @Override
    public Response toResponse(KundeDeleteBestellungException e) {
        final String msg = messages.getMessage(headers, e.getMessageKey(), e.getKundeId(), e.getAnzahlBestellungen());
        return Response.status(BAD_REQUEST)
                       .type(TEXT_PLAIN)
                       .entity(msg)
                       .build();
    }
}
