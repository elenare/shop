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
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import de.shop.bestellverwaltung.business.BestellungenBroker;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.rest.BestellungenResource;
import de.shop.iam.business.IdentityAccessManagement;
import de.shop.kundenverwaltung.business.KundenBroker;
import de.shop.kundenverwaltung.business.KundenBroker.OrderByType;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.GeschlechtType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.util.ShopRuntimeException;
import de.shop.util.Strings;
import de.shop.util.persistence.File;
import de.shop.util.rest.REST;
import de.shop.util.rest.UriHelper;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.jboss.resteasy.api.validation.ViolationReport;
import org.picketlink.authorization.annotations.RolesAllowed;

import static de.shop.bestellverwaltung.business.BestellungenBroker.FetchType.NUR_BESTELLUNG;
import static de.shop.bestellverwaltung.rest.BestellungenResource.FIND_BY_KUNDE_ID;
import static de.shop.iam.domain.IdentityVO.NACHNAME_PATTERN;
import static de.shop.iam.domain.RolleType.ADMIN_STRING;
import static de.shop.iam.domain.RolleType.KUNDE_STRING;
import static de.shop.iam.domain.RolleType.MITARBEITER_STRING;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.MIT_BESTELLUNGEN;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.NUR_KUNDE;
import static de.shop.util.Constants.ADD_LINK;
import static de.shop.util.Constants.FIRST_LINK;
import static de.shop.util.Constants.LAST_LINK;
import static de.shop.util.Constants.LIST_LINK;
import static de.shop.util.Constants.REMOVE_LINK;
import static de.shop.util.Constants.SELF_LINK;
import static de.shop.util.Constants.UPDATE_LINK;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.logging.Level.FINEST;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@REST
@Path("/kunden")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Api(value = "/kunden",
     description = "Registrierte Kunden verwalten",
     basePath = "https://localhost:8443/shop/rest")
// Falsche Warning bei NetBeans: "If a managed bean has a public field, it must have scope @Dependent"
// weil in der Spez. von CDI 1.0 bzw. Java EE 6 "static" vergessen wurde
public class KundenResource {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    public static final Method FIND_BY_ID;
    public static final Method DOWNLOAD_IMAGE;
    public static final Method DOWNLOAD_BAS64;
    public static final Method DELETE;

    // public fuer Testklassen
    public static final String ID_PATH_PARAM = "kundeId";
    public static final String BESTELLUNGEN_ID_PATH_PARAM = "bestellungId";
    public static final String NACHNAME_QUERY_PARAM = "nachname";
    public static final String PLZ_QUERY_PARAM = "plz";
    public static final String EMAIL_QUERY_PARAM = "email";
    public static final String SEIT_QUERY_PARAM = "seit";
    public static final String GESCHLECHT_QUERY_PARAM = "geschlecht";
    public static final String MINBESTMENGE_QUERY_PARAM = "minBestMenge";
    
    private static final String NEUE_WERTE_DURCH_DEN_PUT_REQUEST = "Neue Werte durch den PUT-Request = ";
    
    private KundenBroker kundenBroker;
    private IdentityAccessManagement iam;
    private Instance<BestellungenBroker> bestellungenBrokerInstance;
    private Instance<UriHelper> uriHelperInstance;
    private Instance<ManagedExecutorService> managedExecutorServiceInstance;
    
    static {
        try {
            FIND_BY_ID = KundenResource.class.getMethod("findById", long.class, UriInfo.class);
            DOWNLOAD_IMAGE = KundenResource.class.getMethod("downloadImage", long.class);
            DOWNLOAD_BAS64 = KundenResource.class.getMethod("downloadBase64", long.class);
            DELETE = KundenResource.class.getMethod("delete", long.class);
                    
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
    }

    /**
     * Public Default-Konstruktor f&uuml;r JAX-RS
     */
    public KundenResource() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param kundenBroker zu injizierendes Objekt f&uuml;r KundenBroker
     * @param iam zu injizierendes Objekt f&uuml;r IdentityAccessManagement
     * @param bestellungenBrokerInstance zu injizierendes Instance-Objekt f&uuml;r BestellungenBroker
     * @param uriHelperInstance zu injizierendes Instance-Objekt f&uuml;r UriHelper
     * @param managedExecutorServiceInstance zu injizierendes Instance-Objekt f&uuml;r ManagedExecutorService
     */
    @Inject
    public KundenResource(KundenBroker kundenBroker,
                          IdentityAccessManagement iam,
                          Instance<BestellungenBroker> bestellungenBrokerInstance,
                          Instance<UriHelper> uriHelperInstance,
                          Instance<ManagedExecutorService> managedExecutorServiceInstance) {
        super();
        this.kundenBroker = kundenBroker;
        this.bestellungenBrokerInstance = bestellungenBrokerInstance;
        this.uriHelperInstance = uriHelperInstance;
        this.iam = iam;
        this.managedExecutorServiceInstance = managedExecutorServiceInstance;
    }
    
    /**
     * Mit der URI /kunden/{id} einen Kunden ermitteln
     * @param id ID des Kunden
     * @param uriInfo von JAX-RS injiziertes Objekt zu UriInfo
     * @return Objekt mit Kundendaten, falls die ID vorhanden ist
     */
    @GET
    @Path("/{" + ID_PATH_PARAM + ":[1-9]\\d*}")
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    @ApiOperation(value = "Einen Kunden mit der Kundennummer suchen",
                  response = AbstractKunde.class)
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = AbstractKunde.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keinen Kunden mit der angegebenen Kundennummer")
    })
    public Response findById(@PathParam(ID_PATH_PARAM) long id, @Context UriInfo uriInfo) {
        final Optional<? extends AbstractKunde> kundeOpt = kundenBroker.findById(id, NUR_KUNDE);
        if (!kundeOpt.isPresent()) {
            iam.checkAdminMitarbeiter();
            return Response.status(NOT_FOUND).build();
        }
        
        final AbstractKunde kunde = kundeOpt.get();
        iam.checkSameIdentity(kunde.getLoginname());
        setStructuralLinks(kunde, uriInfo);
        
        return Response.ok(kunde)
                       .links(getTransitionalLinks(kunde, uriInfo))
                       .build();
    }
    
    /**
     * Mit der URI /kunden/{id} einen Kunden asynchron ermitteln. UriInfo ist nicht verf&uuml;gbar.
     * @param id ID des Kunden
     * @param asyncResponse asynchrones Response-Objekt
     */
    @GET
    @Path("/{" + ID_PATH_PARAM + ":[1-9]\\d*}/async")
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    @ApiOperation(value = "Einen Kunden mit der Kundennummer asynchron suchen",
                  response = AbstractKunde.class)
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = AbstractKunde.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keinen Kunden mit der angegebenen Kundennummer")
    })
    public void findByIdAsync(@PathParam(ID_PATH_PARAM) long id,
                              @Suspended AsyncResponse asyncResponse) {
        // Definition des parallelen Threads
        // TODO ManagedExecutorService kann nur Runnable und Callable verwenden, aber nicht CompletableFuture aus Java 8
        final Runnable findKunde = () -> {
            final Optional<? extends AbstractKunde> kundeOpt = kundenBroker.findById(id, NUR_KUNDE);

            Response response;
            if (kundeOpt.isPresent()) {
                final AbstractKunde kunde = kundeOpt.get();
                iam.checkSameIdentity(kunde.getLoginname());
                setStructuralLinks(kunde, null);
                response = Response.ok(kunde)
                                   .links(getTransitionalLinks(kunde, null))
                                   .build();
            } else {
                iam.checkAdminMitarbeiter();
                response = Response.status(NOT_FOUND).build();
            }

            asyncResponse.resume(response);            
        };
        
        // Ausfuehrung des parallelen Threads
        managedExecutorServiceInstance.get().execute(findKunde);
    }
    
    /**
     * Kunden-IDs zu gegebenem Praefix suchen
     * @param idPrefix Praefix zu gesuchten IDs
     * @return Collection mit IDs zu gegebenem Praefix
     */
    @GET
    @Path("/prefix/id/{" + ID_PATH_PARAM + ":[1-9]\\d*}")
    @Produces({ APPLICATION_JSON, TEXT_PLAIN })
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    @ApiOperation(value = "Kundennummern zum angegebenen Pr\u00E4fix suchen",
                  response = Long.class,
                  responseContainer = "List")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = Long.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keine Kunden mit dem angegebenen Pr\u00E4fix")
    })
    public Collection<Long> findIdsByPrefix(@PathParam(ID_PATH_PARAM) String idPrefix) {
        return kundenBroker.findIdsByPrefix(idPrefix);
    }
    

    /**
     * Mit der URI /kunden werden alle Kunden ermittelt oder
     * mit kunden?nachname=... diejenigen mit einem bestimmten Nachnamen.
     * @param nachname Der gemeinsame Nachname der gesuchten Kunden
     * @param seit Datum seit wann
     * @param geschlecht Geschlecht
     * @param minBestMenge Mindestbestellmenge
     * @param uriInfo von JAX-RS injiziertes Objekt zu UriInfo
     * @return Collection mit den gefundenen Kundendaten
     */
    @GET
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    @ApiOperation(value = "Kunden mit dem Nachnamen, seit-Datum, Geschlecht oder Mindestbestellmenge suchen",
                  response = AbstractKunde.class,
                  responseContainer = "List")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = AbstractKunde.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keine Kunden zu den angegebenen Suchkriterien")
    })
    public Response find(@QueryParam(NACHNAME_QUERY_PARAM)
                         @Pattern(regexp = NACHNAME_PATTERN, message = "{identity.nachname.pattern}")
                         String nachname,
                         // Default-Format, z.B. 31 Oct 2001
                         @QueryParam(SEIT_QUERY_PARAM)
                         Date seit,
                         @QueryParam(GESCHLECHT_QUERY_PARAM)
                         GeschlechtType geschlecht,
                         @QueryParam(MINBESTMENGE_QUERY_PARAM)
                         Integer minBestMenge,
                         @Context
                         UriInfo uriInfo) {
        
        Optional<List<AbstractKunde>> kundenOpt;
        
        if (Strings.isNullOrEmpty(nachname) && seit == null && geschlecht == null && minBestMenge == null) {
            // Kein Query-Parameter
            kundenOpt = kundenBroker.findAll(NUR_KUNDE, OrderByType.ID);
            
        // Genau Ein Query-Parameter
        } else if (!Strings.isNullOrEmpty(nachname) && seit == null && geschlecht == null && minBestMenge == null) {
            kundenOpt = kundenBroker.findByNachname(nachname, NUR_KUNDE);
        } else if (seit != null && Strings.isNullOrEmpty(nachname)
                 && geschlecht == null && minBestMenge == null) {
            kundenOpt = kundenBroker.findBySeit(seit);
        } else if (geschlecht != null && Strings.isNullOrEmpty(nachname)
                 && seit == null && minBestMenge == null) {
            final Optional<List<Privatkunde>> privatkundenOpt = kundenBroker.findByGeschlecht(geschlecht);
            if (privatkundenOpt.isPresent()) {
                final List<Privatkunde> privatkunden = privatkundenOpt.get();
                final List<AbstractKunde> tmpKunden = new ArrayList<>(privatkunden);
                kundenOpt = of(tmpKunden);
            } else {
                kundenOpt = empty();
            }
        } else if (minBestMenge != null && Strings.isNullOrEmpty(nachname)
                && seit == null && geschlecht == null) {
            kundenOpt = kundenBroker.findByMindestBestellmenge(minBestMenge.intValue());
        } else {
            // Mehrere Query-Parameter
            kundenOpt = kundenBroker.findByCriteria(seit, geschlecht, minBestMenge);
        }
        
        if (!kundenOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        
        final List<AbstractKunde> kunden = kundenOpt.get();
        kunden.forEach(k -> setStructuralLinks(k, uriInfo));
        return Response.ok(new GenericEntity<List<AbstractKunde>>(kunden){})   //NOSONAR
                       .links(getTransitionalLinksKunden(kunden, uriInfo))
                       .build();
    }
    
    /**
     * Nachnamen zu gegebenem Praefix suchen
     * @param nachnamePrefix Praefix zu gesuchten Nachnamen
     * @return Collection mit Nachnamen zu gegebenem Praefix
     */
    @GET
    @Path("/prefix/nachname/{nachname}")
    @Produces({ APPLICATION_JSON, TEXT_PLAIN })
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    @ApiOperation(value = "Nachnamen zum angegebenen Pr\u00E4fix suchen",
                  response = String.class,
                  responseContainer = "List")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keine Nachnamen zu den angegebenen Pr\u00E4fix")
    })
    public Collection<String> findNachnamenByPrefix(@PathParam("nachname") String nachnamePrefix) {
        return iam.findNachnamenByPrefix(nachnamePrefix);
    }

    
    
    /**
     * Mit der URI /kunden/bestellungen/{bestellungId} den Kunden einer Bestellung ermitteln
     * @param bestellungId ID der Bestellung
     * @param uriInfo injiziertes Objekt f&uuml;r UriInfo
     * @return Objekt mit Kundendaten, falls die ID vorhanden ist
     */
    @GET
    @Path("/bestellungen/{bestellungId:[1-9]\\d*}")
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    @ApiOperation(value = "Einen Kunden zur angegebenen Bestellnummer suchen",
                  response = AbstractKunde.class)
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = AbstractKunde.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keinen Kunden mit einer Bestellung zur angegebenen Bestellnummer")
    })
    public Response findByBestellungId(@PathParam("bestellungId") long bestellungId, @Context UriInfo uriInfo) {
        final Optional<AbstractKunde> kundeOpt = kundenBroker.findByBestellungId(bestellungId);
        if (!kundeOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }

        AbstractKunde kunde = kundeOpt.get();
        setStructuralLinks(kunde, uriInfo);

        // Link Header setzen
        return Response.ok(kunde)
                       .links(getTransitionalLinks(kunde, uriInfo))
                       .build();
    }
    
    /**
     * IDs der Bestellungen zu einem Kunden mit gegebener ID suchen
     * @param kundeId ID des Kunden
     * @return Liste der Bestellungen-IDs
     */
    @GET
    @Path("/{" + ID_PATH_PARAM + ":[1-9]\\d*}/bestellungenIds")
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    @ApiOperation(value = "Bestellnummern zur angegebenen Kundennummer suchen",
                  response = Long.class,
                  responseContainer = "List")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = Long.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keine Bestellnummern zurn angegebenen Kundennummer")
    })
    public Response findBestellungenIdsByKundeId(@PathParam(ID_PATH_PARAM) long kundeId) {
        final Optional<? extends AbstractKunde> kundeOpt = kundenBroker.findById(kundeId, MIT_BESTELLUNGEN);
        if (!kundeOpt.isPresent()) {
            iam.checkAdminMitarbeiter();
            return Response.status(NOT_FOUND).build();
        }
        
        final AbstractKunde kunde = kundeOpt.get();
        iam.checkSameIdentity(kunde.getLoginname());
        final Optional<List<Bestellung>> bestellungenOpt =
                                         bestellungenBrokerInstance.get().findBestellungenByKunde(kunde, NUR_BESTELLUNG);
        
        if (!bestellungenOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        
        final List<Bestellung> bestellungen = bestellungenOpt.get();
        final List<Long> bestellungenIds = bestellungen.stream()
                                                       .map(Bestellung::getId)
                                                       .collect(toList());
        
        return Response.ok(new GenericEntity<Collection<Long>>(bestellungenIds) {})   //NOSONAR
                       .build();
    }

    /**
     * Mit der URI /kunden einen Kunden per PUT aktualisieren
     * @param kunde zu aktualisierende Daten des Kunden
     * @param uriInfo von JAX-RS injiziertes Objekt zu UriInfo
     * @return Aktualisierter Kunde
     */
    @PUT
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    @ApiOperation(value = "Einen vorhandenen Kunden modifizieren",
                  response = AbstractKunde.class)
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = AbstractKunde.class),
        @ApiResponse(code = 400, message = "Die neuen Daten zum Kunden sind nicht korrekt oder unvollst\u00E4ndig", response = ViolationReport.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keinen Kunden zur angegebenen Kundennummer")
    })
    public Response update(@Valid AbstractKunde kunde, @Context UriInfo uriInfo) {
        // Vorhandenen Kunden ermitteln
        final Optional<? extends AbstractKunde> origKundeOpt = kundenBroker.findById(kunde.getId(), NUR_KUNDE);
        if (!origKundeOpt.isPresent()) {
            iam.checkAdminMitarbeiter();
            return Response.status(NOT_FOUND).build();
        }
        final AbstractKunde origKunde = origKundeOpt.get();
        iam.checkSameIdentity(origKunde.getLoginname());
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Kunde vorher = " + origKunde);
            LOGGER.finest(NEUE_WERTE_DURCH_DEN_PUT_REQUEST + kunde);
            LOGGER.finest(NEUE_WERTE_DURCH_DEN_PUT_REQUEST + kunde.getIdentity());
            LOGGER.finest(NEUE_WERTE_DURCH_DEN_PUT_REQUEST + kunde.getIdentity().getAdresse());
        }
    
        // Daten des vorhandenen Kunden ueberschreiben
        origKunde.setValues(kunde);
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Kunde nachher = " + origKunde);
        }
        
        // Update durchfuehren
        final AbstractKunde result = kundenBroker.update(origKunde, true).get();
        setStructuralLinks(result, uriInfo);
        
        return Response.ok(result)
                       .links(getTransitionalLinks(result, uriInfo))
                       .build();
    }
    
    @Path("/{" + ID_PATH_PARAM + ":[1-9]\\d*}")
    @PUT
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    @ApiOperation(value = "Einen vorhandenen Kunden modifizieren",
                  response = AbstractKunde.class)
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = AbstractKunde.class),
        @ApiResponse(code = 400, message = "Die neuen Daten zum Kunden sind nicht korrekt oder unvollst\u00E4ndig", response = ViolationReport.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keinen Kunden zur angegebenen Kundennummer")
    })
    public Response update(@PathParam(ID_PATH_PARAM) long id, @Valid AbstractKunde kunde, @Context UriInfo uriInfo) {
        iam.checkSameIdentity(kunde.getIdentity().getLoginname());
        if (id != kunde.getId()) {
            return Response.status(BAD_REQUEST).build();
        }
        
        return update(kunde, uriInfo);
    }

    
    /**
     * Mit der URI /kunden/privat/update einen Privatkunden per PUT durch FORM-Parameter aktualisieren.
     * @param kunde zu aendernder Kunde
     * @param uriInfo von JAX-RS injiziertes Objekt zu UriInfo
     * @return Response-Objekt
     */
    @Path("/privat")
    @PUT
    @Consumes(APPLICATION_FORM_URLENCODED)
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    public Response updateForm(@BeanParam @Valid Privatkunde kunde, @Context UriInfo uriInfo) {
        iam.checkSameIdentity(kunde.getLoginname());
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest(kunde.getIdentity().toString());
            LOGGER.finest(kunde.getIdentity().getAdresse().toString());
        }
        
        return update(kunde, uriInfo);
    }

    /**
     * Mit der URI /kunden{id} einen Kunden per DELETE l&ouml;schen
     * @param kundeId des zu l&ouml;schenden Kunden
     *         gel&ouml;scht wurde, weil es zur gegebenen id keinen Kunden gibt
     */
    @Path("/{" + ID_PATH_PARAM + ":[1-9]\\d*}")
    @DELETE
    @RolesAllowed(ADMIN_STRING)
    @ApiOperation(value = "Einen vorhandenen Kunden l\u00F6schen")
    @ApiResponses({
        @ApiResponse(code = 204, message = "NoContent (OK)"),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden.")
    })
    public void delete(@PathParam(ID_PATH_PARAM) long kundeId) {
   //     kundenBroker.deleteById(kundeId);
    }

    // <security-constraint> in web.xml: Wildcards in <url-pattern> nur am Ende
    @Path("/image/{" + ID_PATH_PARAM + ":[1-9]\\d*}")
    @POST
    // RESTEasy unterstuetzt nicht video/mp4
    @Consumes({ "image/jpeg", "image/pjpeg", "image/png" })
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    @ApiOperation(value = "Zu einem vorhandenen Kunden ein Bild oder ein Video hochladen")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Created (OK)"),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keinen Kunden zur angegebenen Kundennummer")
    })
    public Response uploadImage(@PathParam(ID_PATH_PARAM) long kundeId, byte[] bytes, @Context UriInfo uriInfo) {
        final Optional<? extends AbstractKunde> kundeOpt = kundenBroker.findById(kundeId, NUR_KUNDE);
        if (!kundeOpt.isPresent()) {
            iam.checkAdminMitarbeiter();
            return Response.status(BAD_REQUEST).build();
        }
        
        final AbstractKunde kunde = kundeOpt.get();
        iam.checkSameIdentity(kunde.getIdentity().getLoginname());
        
        kundenBroker.setFile(kunde, bytes);
        return Response.created(uriHelperInstance.get().getUri(KundenResource.class, DOWNLOAD_IMAGE, kundeId, uriInfo))
                       .build();
    }
    
    /**
     * Bild zu einem Kunden mit gegebener ID herunterladen
     * @param kundeId ID des Kunden
     * @return Byte[] mit dem Bild
     */
    @Path("/image/{" + ID_PATH_PARAM + ":[1-9]\\d*}")
    @GET
    @Produces({ "image/jpeg", "image/pjpeg", "image/png" })
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    @ApiOperation(value = "Von einem vorhandenen Kunden ein Bild oder ein Video herunterladen", response = Byte.class, responseContainer = "list")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = Byte.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keinen Kunden zur angegebenen Kundennummer")
    })
    public byte[] downloadImage(@PathParam(ID_PATH_PARAM) long kundeId) {
        final Optional<? extends AbstractKunde> kundeOpt = kundenBroker.findById(kundeId, NUR_KUNDE);
        if (!kundeOpt.isPresent()) {
            iam.checkAdminMitarbeiter();
            return new byte[0];
        }
        
        final AbstractKunde kunde = kundeOpt.get();
        iam.checkSameIdentity(kunde.getIdentity().getLoginname());
        
        final File file = kunde.getFile();
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest(file.toString());
        }
        
        return file.getBytes();
    }
    
    @Path("/base64/{" + ID_PATH_PARAM + ":[1-9]\\d*}")
    @POST
    @Consumes({ TEXT_PLAIN })
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    @ApiOperation(value = "Zu einem vorhandenen Kunden ein Bild oder ein Video mit Base64-Codierung hochladen")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Created (OK)"),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keinen Kunden zur angegebenen Kundennummer")
    })
    public Response uploadBase64(@PathParam(ID_PATH_PARAM) long kundeId, String base64, @Context UriInfo uriInfo) {
        final Optional<? extends AbstractKunde> kundeOpt = kundenBroker.findById(kundeId, NUR_KUNDE);
        if (!kundeOpt.isPresent()) {
            iam.checkAdminMitarbeiter();
            return Response.status(BAD_REQUEST).build();
        }
        
        final AbstractKunde kunde = kundeOpt.get();
        iam.checkSameIdentity(kunde.getIdentity().getLoginname());
        
        final byte[] bytes = Base64.getDecoder().decode(base64);
        kundenBroker.setFile(kunde, bytes);
        return Response.created(uriHelperInstance.get().getUri(KundenResource.class, DOWNLOAD_BAS64, kundeId, uriInfo))
                       .build();
    }
    
    /**
     * Multimedia-Datei (mit Base64-Codierung) zu einem Kunden mit gegebener ID herunterladen
     * @param kundeId ID des Kunden
     * @return String mit Base64-Codierung einer Multimedia-Datei
     */
    @Path("/base64/{" + ID_PATH_PARAM + ":[1-9]\\d*}")
    @GET
    @Produces({ TEXT_PLAIN })
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    @ApiOperation(value = "Von einem vorhandenen Kunden ein Bild oder ein Video mit Base64-Codierung herunterladen", response = String.class)
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keinen Kunden zur angegebenen Kundennummer")
    })
    public String downloadBase64(@PathParam(ID_PATH_PARAM) long kundeId) {
        final Optional<? extends AbstractKunde> kundeOpt = kundenBroker.findById(kundeId, NUR_KUNDE);
        if (!kundeOpt.isPresent()) {
            iam.checkAdminMitarbeiter();
            return "";
        }
        
        final AbstractKunde kunde = kundeOpt.get();
        iam.checkSameIdentity(kunde.getIdentity().getLoginname());
        
        final File file = kunde.getFile();
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest(file.toString());
        }
        
        return Base64.getEncoder().encodeToString(file.getBytes());
    }
    
    //--------------------------------------------------------------------------
    // Methoden fuer URIs und Links
    //--------------------------------------------------------------------------
    
    private URI getUriKunde(AbstractKunde kunde, UriInfo uriInfo) {
        return uriHelperInstance.get().getUri(KundenResource.class, FIND_BY_ID, kunde.getId(), uriInfo);
    }

    public void setStructuralLinks(AbstractKunde kunde, UriInfo uriInfo) {
        if (uriInfo == null) {
            return;
        }
        
        // URI fuer Bestellungen setzen
        final URI uri = uriHelperInstance.get().getUri(BestellungenResource.class, FIND_BY_KUNDE_ID, kunde.getId(), uriInfo);
        kunde.setBestellungenUri(uri);
        
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest(kunde.toString());
        }
    }
    
    private Link[] getTransitionalLinks(AbstractKunde kunde, UriInfo uriInfo) {
        if (uriInfo == null) {
            return new Link[0];
        }
        
        final Link self = Link.fromUri(getUriKunde(kunde, uriInfo))
                              .rel(SELF_LINK)
                              .build();

        final UriHelper uriHelper = uriHelperInstance.get();
        final Link list = Link.fromUri(uriHelper.getUri(KundenResource.class, uriInfo))
                              .rel(LIST_LINK)
                              .build();
        
        final Link add = Link.fromUri(uriHelper.getUri(KundenResource.class, uriInfo))
                             .rel(ADD_LINK)
                             .build();

        final Link update = Link.fromUri(uriHelper.getUri(KundenResource.class, uriInfo))
                                .rel(UPDATE_LINK)
                                .build();

        final Link remove = Link.fromUri(uriHelper.getUri(KundenResource.class, DELETE, kunde.getId(), uriInfo))
                                .rel(REMOVE_LINK)
                                .build();

        return new Link[] { self, list, add, update, remove };
    }
    
    private Link[] getTransitionalLinksKunden(List<? extends AbstractKunde> kunden, UriInfo uriInfo) {
        if (kunden == null || kunden.isEmpty()) {
            return new Link[0];
        }
        
        final Link first = Link.fromUri(getUriKunde(kunden.get(0), uriInfo))
                               .rel(FIRST_LINK)
                               .build();
        final int lastPos = kunden.size() - 1;
        final Link last = Link.fromUri(getUriKunde(kunden.get(lastPos), uriInfo))
                              .rel(LAST_LINK)
                              .build();
        
        return new Link[] { first, last };
    }
}
