/*
 * Copyright (C) 2014 Juergen Zimmermann, Hochschule Karlsruhe
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

package de.shop.iam.rest;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.annotations.Authorization;
import de.shop.iam.business.IdentityAccessManagement;
import de.shop.iam.domain.IdentityVO;
import de.shop.iam.domain.RolleType;
import de.shop.util.rest.REST;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.picketlink.Identity;
import org.picketlink.idm.model.basic.User;

import static java.lang.Boolean.TRUE;
import static java.util.logging.Level.FINEST;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@REST
@Path("/iam")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Api(value = "/iam",
     description = "Benutzer, Rollen und Gruppen verwalten",
     authorizations = { @Authorization(type = "BASIC", value = "admin,mitarbeiter") })
public class IamResource {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private Identity identity;
    private Instance<IdentityAccessManagement> iamInstance;

    /**
     * Public Default-Konstruktor f&uuml;r JAX-RS
     */
    public IamResource() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param identity zu injizierendes Objekt f&uuml;r Identity
     * @param iamInstance zu injizierendes Instance-Objekt f&uuml;r IdentityAccessManagement
     */
    @Inject
    IamResource(Identity identity, Instance<IdentityAccessManagement> iamInstance) {
        super();
        this.identity = identity;
        this.iamInstance = iamInstance;
    }
    
    @Path("/login")
    @GET
    @ApiOperation(value = "Mit BASIC-Authentifizierung einloggen",
                  response = AuthenticationResponse.class)
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = AuthenticationResponse.class),
        @ApiResponse(code = 403, message = "Der Benutzername oder das Passwort ist nicht korrekt."),
    })
    public Response login() {
        if (!identity.isLoggedIn()) {
            return Response.status(UNAUTHORIZED).build();
        }
        
        final User user = (User) identity.getAccount();
        final IdentityVO identityVO = new IdentityVO(user);
        final Optional<List<RolleType>> rollen = iamInstance.get().eigeneRollen();
        final AuthenticationResponse authenticationResponse = new AuthenticationResponse(identityVO, rollen);
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("identityRollen: " + authenticationResponse);
        }
        return Response.ok(authenticationResponse).build();
    }

    
    @Path("/logout")
    @POST
    @ApiOperation(value = "Ausloggen")
    @ApiResponses({
        @ApiResponse(code = 204, message = "NoContent (OK)"),
    })
    public void logout() throws ServletException {
        if (identity.isLoggedIn()) {
            iamInstance.get().logout();
        }
    }
    
    private class AuthenticationResponse extends IdentityVO {
        private final Map<String, Boolean> rollen = new HashMap<>();

        public AuthenticationResponse(IdentityVO identity, Optional<List<RolleType>> rollen) {
            super(identity);
            
            if (rollen.isPresent()) {
                rollen.get()
                      .stream()
                      .map(RolleType::getValue)
                      .forEach(r -> this.rollen.put(r, TRUE));
            }
        }

        public Map<String, Boolean> getRollen() {
            return rollen;
        }

        @Override
        public String toString() {
            return "AuthenticationResponse {" + super.toString() + ", rollen=" + rollen + '}';
        }
    }
}
