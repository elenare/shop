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
import de.shop.artikelverwaltung.business.KatalogBroker;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.ShopRuntimeException;
import de.shop.util.Strings;
import de.shop.util.rest.REST;
import de.shop.util.rest.UriHelper;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static de.shop.util.Constants.SELF_LINK;
import javax.enterprise.context.Dependent;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@REST
@Path("/katalog")
@Dependent
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Api(value = "/katalog",
     description = "Nach Artikel recherchieren",
     basePath = "https://localhost:8443/shop/rest")
public class KatalogResource {
    public static final String BEZEICHNUNG_QUERY_PARAM = "bezeichnung";
    public static final Method FIND_BY_ID;

    // public fuer Testklassen
    public static final String ID_PATH_PARAM = "id";
    
    static {
        try {
            FIND_BY_ID = KatalogResource.class.getMethod("findById", long.class, UriInfo.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
    }
    
    private KatalogBroker katalogBroker;
    private UriHelper uriHelper;

    /**
     * Public Default-Konstruktor f&uuml;r JAX-RS
     */
    public KatalogResource() {
        super();
   }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param katalogBroker zu injizierendes Objekt f&uuml;r KatalogBroker
     * @param uriHelper zu injizierendes Objekt f&uuml;r UriHelper
     */
    @Inject
    KatalogResource(KatalogBroker katalogBroker, UriHelper uriHelper) {
        super();
        this.katalogBroker = katalogBroker;
        this.uriHelper = uriHelper;
    }

    /**
     * Mit der URI /katalog/{id} einen Artikel ermitteln
     * @param id ID des Artikels
     * @param uriInfo von JAX-RS injiziertes Objekt zu UriInfo
     * @return Liste mit Artikeldaten, falls die ID vorhanden ist
     */
    @GET
    @Path("/{" + ID_PATH_PARAM + ":[1-9]\\d*}")
    @ApiOperation(value = "Artikel anhand der Artikelnummer suchen",
                  response = Artikel.class)
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = Artikel.class),
        @ApiResponse(code = 404, message = "Es gibt keinen Artikel mit der angegebenen Artikelnummer")
    })
    public Response findById(@PathParam(ID_PATH_PARAM)
                             @ApiParam(required = true)
                             long id,
                             @Context
                             UriInfo uriInfo) {
        final Optional<Artikel> artikelOpt = katalogBroker.findById(id);
        if (!artikelOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        
        final Artikel artikel = artikelOpt.get();
        return Response.ok(artikel)
                       .links(getTransitionalLinks(artikel, uriInfo))
                       .build();
    }
    
    /**
     * Mit der URI /katalog alle verfuegbaren Artikel ermitteln
     * @param bezeichnung Artikelbezeichnung oder null, um alle verfuegbaren Artikel zu suchen
     * @param uriInfo von JAX-RS injiziertes Objekt zu UriInfo
     * @return Liste mit Artikeldaten, falls die ID vorhanden ist
     */
    @GET
    @ApiOperation(value = "Artikel anhand der Bezeichnung suchen",
                  response = Artikel.class,
                  responseContainer = "List")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = Artikel.class),
        @ApiResponse(code = 404, message = "Es gibt keine Artikel mit der angegebenen Bezeichnung")
    })
    public Response find(@QueryParam(BEZEICHNUNG_QUERY_PARAM)
                         @ApiParam(value = "Die Bezeichnung der gesuchten Artikel")
                         String bezeichnung,
                         @Context
                         UriInfo uriInfo) {
        final Optional<List<Artikel>> artikelListOpt = Strings.isNullOrEmpty(bezeichnung)
                                                       ? katalogBroker.findVerfuegbareArtikel()
                                                       : katalogBroker.findByBezeichnung(bezeichnung);
        if (!artikelListOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        
        final List<Artikel> artikelList = artikelListOpt.get();
        artikelList.forEach(a -> getTransitionalLinks(a, uriInfo));
        return Response.ok(artikelList)
                       .build();
    }
    
    
    //--------------------------------------------------------------------------
    // Methoden fuer URIs und Links
    //--------------------------------------------------------------------------
    
    private URI getUriArtikel(Artikel artikel, UriInfo uriInfo) {
        return uriHelper.getUri(KatalogResource.class, FIND_BY_ID, artikel.getId(), uriInfo);
    }
    
    private Link[] getTransitionalLinks(Artikel artikel, UriInfo uriInfo) {
        final Link self = Link.fromUri(getUriArtikel(artikel, uriInfo))
                              .rel(SELF_LINK)
                              .build();

        return new Link[] { self };
    }
}
