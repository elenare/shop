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

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Base64;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import static java.util.logging.Level.FINER;
import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Provider
@ApplicationScoped
@Priority(APPLICATION)
public class LogFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final String BASIC_PREFIX = "Basic ";
    private static final int BASIC_PREFIX_LEN = BASIC_PREFIX.length();
    
    @Override
    public void filter(ContainerRequestContext requestCtx) throws IOException {
        if (!LOGGER.isLoggable(FINER)) {
            return;
        }
        LOGGER.finer("<Request> URI: " + requestCtx.getUriInfo().getAbsolutePath());
        LOGGER.finer("<Request> Method: " + requestCtx.getMethod());
        LOGGER.finer("<Request> Acceptable Media Types: " + requestCtx.getAcceptableMediaTypes());
        LOGGER.finer("<Request> Content Type: " + requestCtx.getHeaderString("content-type"));
        final SecurityContext securityCtx = requestCtx.getSecurityContext();
        if (securityCtx == null) {
            LOGGER.finer("<Request> Security Context: null");
        } else {
            LOGGER.finer("<Request> Authentication Scheme: " + securityCtx.getAuthenticationScheme());
        }
        final String auth = requestCtx.getHeaderString("authorization");
        LOGGER.finer("<Request> Authorization: " + auth);
        if (auth != null && auth.startsWith(BASIC_PREFIX)) {
            final String base64 = auth.substring(BASIC_PREFIX_LEN);
            final String decoded = new String(Base64.getDecoder().decode(base64));
            final String[] usernamePassword = decoded.split(":");
            switch (usernamePassword.length) {
                case 1:
                    LOGGER.finer("<Request>    Kein Benutzername in der Base64-Codierung fuer die BASIC-Authentifizierung");
                    break;
                case 2:                                                //NOSONAR
                    LOGGER.finer("<Request>    Benutzername: " + usernamePassword[0]);
                    LOGGER.finer("<Request>    Password: " + usernamePassword[1]);
                    break;
                default:
                    LOGGER.finer("<Request>    Fehlerhafte Base64-Codierung fuer die BASIC-Authentifizierung");
                    break;
            }
            
        }
        LOGGER.finer("<Request> Acceptable Languages: " + requestCtx.getAcceptableLanguages());
    }

    @Override
    public void filter(ContainerRequestContext requestCtx, ContainerResponseContext responseCtx) throws IOException {
        if (!LOGGER.isLoggable(FINER)) {
            return;
        }
        LOGGER.finer("<Response> Status Info: " + responseCtx.getStatus() + " " + responseCtx.getStatusInfo());
        LOGGER.finer("<Response> Location: " + responseCtx.getLocation());        
    }
}
