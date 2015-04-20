/*
 * Copyright (C) 2014 Hochschule Karlsruhe
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
import javax.ejb.EJBAccessException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Provider
@ApplicationScoped
@Log
public class EJBTransactionRolledbackExceptionMapper implements ExceptionMapper<EJBTransactionRolledbackException> {
    @Override
    public Response toResponse(EJBTransactionRolledbackException e) {
        @SuppressWarnings("ThrowableResultIgnored")
        final Throwable cause = e.getCause();
        if (cause != null) {
            if (cause.getClass().equals(EJBAccessException.class)) {
                return Response.status(FORBIDDEN).build();
            }
            
            if (cause.getClass().equals(ConstraintViolationException.class)) {
                final ConstraintViolationException cve = (ConstraintViolationException) cause;
                return ConstraintViolationExceptionMapper.getAsResponse(cve);
            }
        }
        
        return Response.status(INTERNAL_SERVER_ERROR).build();
    }
}
