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

package de.shop.artikelverwaltung.rest;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.annotations.Authorization;
import de.shop.artikelverwaltung.business.ArtikelBroker;
import de.shop.artikelverwaltung.business.KatalogBroker;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.rest.REST;
import de.shop.util.rest.UriHelper;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Optional;
import java.util.logging.Logger;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.jboss.resteasy.api.validation.ViolationReport;
import org.picketlink.authorization.annotations.RolesAllowed;

import static de.shop.artikelverwaltung.rest.KatalogResource.FIND_BY_ID;
import static de.shop.iam.domain.RolleType.ADMIN_STRING;
import static de.shop.iam.domain.RolleType.MITARBEITER_STRING;
import static de.shop.util.Constants.SELF_LINK;
import static java.util.logging.Level.FINEST;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@REST
@Path("/artikel")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Api(value = "/artikel",
     description = "Artikel verwalten",
     authorizations = { @Authorization(type = "BASIC", value = "admin,mitarbeiter") })
public class ArtikelResource {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    public static final String BEZEICHNUNG_QUERY_PARAM = "bezeichnung";
    
    // public fuer Testklassen
    public static final String ID_PATH_PARAM = "id";
    
    private ArtikelBroker artikelBroker;
    private KatalogBroker katalogBroker;
    private UriHelper uriHelper;
    private Instance<HttpServletRequest> requestInstance;

    /**
     * Public Default-Konstruktor f&uuml;r JAX-RS
     */
    public ArtikelResource() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param artikelBroker zu injizierendes Objekt f&uuml;r ArtikelBroker
     * @param katalogBroker zu injizierendes Objekt f&uuml;r KatalogBroker
     * @param uriHelper zu injizierendes Objekt f&uuml;r UriHelper
     * @param requestInstance zu injizierendes Instance-Objekt f&uuml;r HttpServletRequest
     */
    @Inject
    ArtikelResource(ArtikelBroker artikelBroker, KatalogBroker katalogBroker, UriHelper uriHelper, Instance<HttpServletRequest> requestInstance) {
        super();
        this.artikelBroker = artikelBroker;
        this.katalogBroker = katalogBroker;
        this.uriHelper = uriHelper;
        this.requestInstance = requestInstance;
    }

    /**
     * Mit der URI /artikel einen Artikel per POST anlegen.
     * @param artikel neuer Artikel
     * @param uriInfo von JAX-RS injiziertes Objekt zu UriInfo
     * @return Response-Objekt mit URI des neuen Artikels
     */
    @POST
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    @ApiOperation(value = "Einen neuen Artikel anlegen")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Created (OK)"),
        @ApiResponse(code = 400, message = "Die Daten zum neuen Artikel sind nicht korrekt oder unvollst\u00E4ndig", response = ViolationReport.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden.")
    })
    public Response save(@Valid
                         @ApiParam(value = "Der Datensatz f\u00FCr den neuen Artikel", required = true)
                         Artikel artikel,
                         @Context
                         UriInfo uriInfo) {
        final Artikel result = artikelBroker.save(artikel);
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest(result.toString());
        }
        
        final URI location = uriHelper.getUri(KatalogResource.class, FIND_BY_ID, result.getId(), uriInfo);
        return Response.created(location)
                       .build();
    }
    
    /**
     * Mit der URI /artikel/form einen Artikel per POST durch FORM-Parameter anlegen oder aktualisieren.
     * @param artikel neuer oder zu aktualisierender Artikel
     * @param uriInfo von JAX-RS injiziertes Objekt zu UriInfo
     * @return Response-Objekt mit ggf. der URI des neuen Artikels
     */
    @POST
    @PUT
    @Consumes(APPLICATION_FORM_URLENCODED)
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public Response saveForm(@BeanParam @Valid Artikel artikel, @Context UriInfo uriInfo) {
        final String method = requestInstance.get().getMethod();
        if (artikel.getId() == 0 && "POST".equals(method)) {
            return save(artikel, uriInfo);
        }
        if (artikel.getId() != 0 && "PUT".equals(method)) {
            return update(artikel, uriInfo);
        }
        return Response.status(BAD_REQUEST).build();
    }

    /**
     * Mit der URI /artikel einen Artikel per PUT aktualisieren
     * @param artikel zu aktualisierende Daten des Artikels
     * @param uriInfo von JAX-RS injiziertes Objekt zu UriInfo
     * @return Aktualisierter Artikel
     */
    @PUT
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    @ApiOperation(value = "Einen vorhandenen Artikel \u00E4ndern", response = Artikel.class)
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = Artikel.class),
        @ApiResponse(code = 400, message = "Die neuen Daten zum vorhandenen Artikel sind nicht korrekt oder unvollst\u00E4ndig", response = ViolationReport.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden.")
    })
    public Response update(@Valid Artikel artikel, @Context UriInfo uriInfo) {
        // Vorhandenen Artikel ermitteln
        final Optional<Artikel> artikelOpt = katalogBroker.findById(artikel.getId());
        if (!artikelOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        
        final Artikel origArtikel = artikelOpt.get();
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Artikel vorher = " + origArtikel);
        }
    
        // Daten des vorhandenen Artikels ueberschreiben
        origArtikel.setValues(artikel);
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Artikel nachher = " + origArtikel);
        }
        
        // Update durchfuehren
        final Artikel result = artikelBroker.updateArtikel(origArtikel);        
        return Response.ok(result)
                       .links(getTransitionalLinks(artikel, uriInfo))
                       .build();
    }
    
    //--------------------------------------------------------------------------
    // Methoden fuer URIs und Links
    //--------------------------------------------------------------------------
    
    private Link[] getTransitionalLinks(Artikel artikel, UriInfo uriInfo) {
        final URI selfUri = uriHelper.getUri(KatalogResource.class, FIND_BY_ID, artikel.getId(), uriInfo);
        final Link self = Link.fromUri(selfUri)
                              .rel(SELF_LINK)
                              .build();

        return new Link[] { self };
    }
}
