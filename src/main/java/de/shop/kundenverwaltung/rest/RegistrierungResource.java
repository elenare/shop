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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import de.shop.iam.domain.IdentityVO;
import de.shop.kundenverwaltung.business.RegistrierungBroker;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.util.rest.REST;
import de.shop.util.rest.UriHelper;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.jboss.resteasy.api.validation.ViolationReport;

import static de.shop.kundenverwaltung.rest.KundenResource.FIND_BY_ID;
import static java.util.logging.Level.FINEST;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@REST
@Path("/registrierung")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Api(value = "/registrierung",
     description = "Einen neuen Kunden registrieren",
     basePath = "https://localhost:8443/shop/rest")
public class RegistrierungResource {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private RegistrierungBroker registrierungBroker;
    private UriHelper uriHelper;

    /**
     * Public Default-Konstruktor f&uuml;r JAX-RS
     */
    public RegistrierungResource() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param registrierungBroker zu injizierendes Objekt f&uuml;r RegistrierungBroker
     * @param uriHelper zu injizierendes Objekt f&uuml;r UriHelper
     */
    @Inject
    RegistrierungResource(RegistrierungBroker registrierungBroker, UriHelper uriHelper) {
        super();
        this.registrierungBroker = registrierungBroker;
        this.uriHelper = uriHelper;
    }

    /**
     * Mit der URI /registrierung einen Kunden per POST anlegen.
     * @param kunde neuer Kunde
     * @param uriInfo von JAX-RS injiziertes Objekt zu UriInfo
     * @return Response-Objekt mit URI des neuen Kunden
     */
    @POST
    @ApiOperation(value = "Einen neuen Kunden registrieren")
    @ApiResponses({
        @ApiResponse(code = 201, message = "OK"),
        @ApiResponse(code = 400, message = "Die Daten zum neuen Kunden sind nicht korrekt oder unvollst\u00E4ndig", response = ViolationReport.class),
    })
    public Response save(@Valid
                         @ApiParam(required = true)
                         AbstractKunde kunde,
                         @Context
                         UriInfo uriInfo) {
        // Loginname ergaenzen
        kunde.setLoginname(kunde.getIdentity().getLoginname());
        // Es kann noch keine Bestellungen geben
        kunde.setBestellungenUri(null);
        
        final AbstractKunde result = registrierungBroker.save(kunde);
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest(result.toString());
        }
        
        final URI location = uriHelper.getUri(KundenResource.class, FIND_BY_ID, kunde.getId(), uriInfo);
        return Response.created(location)
                       .build();
    }
    
    /**
     * Mit der URI /registrierung/privat einen Privatkunden per POST durch FORM-Parameter anlegen.
     * @param kunde neuer Kunde
     * @param uriInfo von JAX-RS injiziertes Objekt zu UriInfo
     * @return Response-Objekt mit der URI des neuen Kunden
     */
    @Path("/privat")
    @POST
    @Consumes(APPLICATION_FORM_URLENCODED)
    @ApiOperation(value = "Einen neuen Kunden registrieren")
    @ApiResponses({
        @ApiResponse(code = 201, message = "OK"),
        @ApiResponse(code = 400, message = "Die Daten zum neuen Kunden sind nicht korrekt oder unvollst\u00E4ndig", response = ViolationReport.class),
    })
    public Response saveForm(@BeanParam
                             @Valid
                             @ApiParam(required = true)
                             Privatkunde kunde,
                             @Context
                             UriInfo uriInfo) {
        final IdentityVO identity = kunde.getIdentity();
        
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest(identity.toString());
            LOGGER.finest(identity.getAdresse().toString());
        }
        
        // Loginname ergaenzen
        kunde.setLoginname(identity.getLoginname());
        
        return save(kunde, uriInfo);
    }
}
