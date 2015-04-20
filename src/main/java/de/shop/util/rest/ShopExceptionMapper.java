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

import de.shop.util.AbstractShopException;
import de.shop.util.interceptor.Log;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 * Wichtig: Kein allgemeiner ExceptionMapper fuer RuntimeException, weil javax.ws.rs.NotFoundException und
 * javax.ws.rs.NotSupportedException sonst nicht auf 404 bzw. 415 abgebildet werden
 */
@Provider
@ApplicationScoped
@Log
public class ShopExceptionMapper implements ExceptionMapper<AbstractShopException> {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    @Override
    public Response toResponse(AbstractShopException e) {
        final String classname = e.getClass().getSimpleName();
        final String msg = "Kein ExceptionMapper fuer die Exception " + classname;
        LOGGER.log(WARNING, msg, e);
        final String headerReason = LOGGER.isLoggable(FINER) ? e.getMessage() : msg;
        return Response.status(INTERNAL_SERVER_ERROR)
                       .header("x-reason", headerReason)
                       .build();
    }
}
