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

import de.shop.util.interceptor.Log;
import de.shop.util.persistence.DefaultEM;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Provider
@ApplicationScoped
@Log
public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {
    private static final String MESSAGE_KEY = "persistence.concurrentUpdate";
    
    @Context
    private HttpHeaders headers;
    
    @Inject
    private Messages messages;
    
    @Inject
    @DefaultEM
    private EntityManager em;
    
    @Override
    public Response toResponse(OptimisticLockException e) {
        final Object id = e.getEntity() == null
                          ? null
                          : em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(e.getEntity());
        final String msg = messages.getMessage(headers, MESSAGE_KEY, id);
        return Response.status(CONFLICT)
                       .type(TEXT_PLAIN)
                       .entity(msg)
                       .build();
    }
}
