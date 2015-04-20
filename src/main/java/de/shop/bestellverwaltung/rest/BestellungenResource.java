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

package de.shop.bestellverwaltung.rest;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.annotations.Authorization;
import de.shop.artikelverwaltung.business.KatalogBroker;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.rest.KatalogResource;
import de.shop.bestellverwaltung.business.BestellungenBroker;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Warenkorbposition;
import de.shop.iam.business.IdentityAccessManagement;
import de.shop.kundenverwaltung.business.KundenBroker;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.rest.KundenResource;
import de.shop.util.ShopRuntimeException;
import de.shop.util.rest.REST;
import de.shop.util.rest.UriHelper;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.picketlink.authorization.annotations.RolesAllowed;

import static de.shop.bestellverwaltung.business.BestellungenBroker.FetchType.NUR_BESTELLUNG;
import static de.shop.iam.domain.RolleType.ADMIN_STRING;
import static de.shop.iam.domain.RolleType.KUNDE_STRING;
import static de.shop.iam.domain.RolleType.MITARBEITER_STRING;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.MIT_BESTELLUNGEN;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.NUR_KUNDE;
import static de.shop.util.Constants.ADD_LINK;
import static de.shop.util.Constants.FIRST_LINK;
import static de.shop.util.Constants.LAST_LINK;
import static de.shop.util.Constants.SELF_LINK;
import static java.util.logging.Level.FINER;
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
@Path("/bestellungen")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Api(value = "/bestellungen",
     description = "Bestellungen verwalten",
     authorizations = { @Authorization(type = "BASIC", value = "kunde") })
public class BestellungenResource {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    public static final Method FIND_BY_ID;
    public static final Method FIND_BY_KUNDE_ID;

    // public fuer Testklassen
    public static final String ID_PATH_PARAM = "bestellungId";
    public static final String KUNDEN_ID_PATH_PARAM = "kundeId";
    
    private BestellungenBroker bestellungenBroker;
    private UriHelper uriHelper;
    private Instance<KatalogBroker> katalogBrokerInstance;
    private Instance<KundenBroker> kundenBrokerInstance;
    private Instance<IdentityAccessManagement> iamInstance;
    
    static {
        try {
            FIND_BY_ID = BestellungenResource.class.getMethod("findById", long.class, UriInfo.class);
            FIND_BY_KUNDE_ID = BestellungenResource.class.getMethod("findByKundeId", long.class, UriInfo.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
    }

    /**
     * Public Default-Konstruktor f&uuml;r JAX-RS
     */
    public BestellungenResource() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param bestellungenBroker zu injizierendes Objekt f&uuml;r BestellungenBroker
     * @param uriHelper zu injizierendes Objekt f&uuml;r UriHelper
     * @param katalogBrokerInstance zu injizierendes Instance-Objekt f&uuml;r KatalogBroker
     * @param kundenBrokerInstance zu injizierendes Instance-Objekt f&uuml;r KundenBroker
     * @param iamInstance zu injizierendes Instance-Objekt f&uuml;r IdentityAccessManagement
     */
    @Inject
    BestellungenResource(BestellungenBroker bestellungenBroker,
                         UriHelper uriHelper,
                         Instance<KatalogBroker> katalogBrokerInstance,
                         Instance<KundenBroker> kundenBrokerInstance,
                         Instance<IdentityAccessManagement> iamInstance) {
        super();
        this.bestellungenBroker = bestellungenBroker;
        this.katalogBrokerInstance = katalogBrokerInstance;
        this.kundenBrokerInstance = kundenBrokerInstance;
        this.iamInstance = iamInstance;
        this.uriHelper = uriHelper;
    }

    /**
     * Mit der URI /bestellungen/{id} eine Bestellung ermitteln
     * @param id ID der Bestellung
     * @param uriInfo injiziertes Objekt f&uuml;r UriInfo
     * @return Objekt mit Bestelldaten, falls die ID vorhanden ist
     */
    @GET
    @Path("/{" + ID_PATH_PARAM + ":[1-9]\\d*}")
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    @ApiOperation(value = "Eine Bestellung anhand der Bestellnummer suchen",
                  response = Bestellung.class)
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = Bestellung.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keine Bestellung mit der angegebenen Bestellnummer")
    })
    public Response findById(@PathParam(ID_PATH_PARAM) long id, @Context UriInfo uriInfo) {
        final IdentityAccessManagement iam = iamInstance.get();
        final Optional<Bestellung> bestellungOpt = bestellungenBroker.findBestellungById(id, NUR_BESTELLUNG);
        if (!bestellungOpt.isPresent()) {
            iam.checkAdminMitarbeiter();
            return Response.status(NOT_FOUND).build();
        }

        final Bestellung bestellung = bestellungOpt.get();
        iam.checkSameIdentity(bestellung.getKunde().getLoginname());
        
        // URIs innerhalb der gefundenen Bestellung anpassen
        setStructuralLinks(bestellung, uriInfo);
        
        // Link-Header setzen
        return Response.ok(bestellung)
                       .links(getTransitionalLinks(bestellung, uriInfo))
                       .build();
    }
    
    
    /**
     * Mit der URI kunden/{id}/bestellungen die Bestellungen zu eine Kunden ermitteln
     * @param kundeId ID des Kunden
     * @param uriInfo von JAX-RS injiziertes Objekt zu UriInfo
     * @return Objekt mit Bestellungsdaten, falls die ID vorhanden ist
     */
    @GET
    @Path("/kunde/{" + KUNDEN_ID_PATH_PARAM + ":[1-9]\\d*}")
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    @ApiOperation(value = "Bestellungen zur angegebenen Kundennummer suchen",
                  response = Bestellung.class,
                  responseContainer = "List")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = Bestellung.class),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keine Bestellungen zur angegebenen Kundennummer")
    })
    public Response findByKundeId(@PathParam(KUNDEN_ID_PATH_PARAM) long kundeId, @Context UriInfo uriInfo) {
        final IdentityAccessManagement iam = iamInstance.get();
        
        final Optional<? extends AbstractKunde> kundeOpt = kundenBrokerInstance.get().findById(kundeId, NUR_KUNDE);
        if (!kundeOpt.isPresent()) {
            iam.checkAdminMitarbeiter();
            return Response.status(NOT_FOUND).build();
        }
        
        final AbstractKunde kunde = kundeOpt.get();
        iam.checkSameIdentity(kunde.getLoginname());
        final Optional<List<Bestellung>> bestellungenOpt = bestellungenBroker.findBestellungenByKunde(kunde,
                                                                                                      NUR_BESTELLUNG);
        
        // URIs innerhalb der gefundenen Bestellungen anpassen
        if (!bestellungenOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        final List<Bestellung> bestellungen = bestellungenOpt.get();
        bestellungen.forEach(bestellung -> setStructuralLinks(bestellung, uriInfo));
        
        return Response.ok(new GenericEntity<List<Bestellung>>(bestellungen) {})   //NOSONAR
                       .links(getTransitionalLinks(bestellungen, kunde, uriInfo))
                       .build();
    }
    
    /**
     * Mit der URI /bestellungen eine neue Bestellung anlegen
     * @param bestellung die neue Bestellung
     * @param uriInfo injiziertes Objekt f&uuml;r UriInfo
     * @return Response mit Location
     */
    @POST
    @RolesAllowed(KUNDE_STRING)
    @ApiOperation(value = "Eine neue Bestellung aufgeben")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Created (OK)"),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden.")
    })
    public Response save(@Valid Bestellung bestellung, @Context UriInfo uriInfo) {
        // IDs der (persistenten) Artikel ermitteln
        final List<Long> artikelIds = new ArrayList<>();
        bestellung.getBestellpositionen()
                  .stream()
                  .map(Bestellposition::getArtikelUri)
                  .filter(Objects::nonNull)
                  .map(URI::toString)
                  .forEach(artikelUriStr -> {
            final int startPos = artikelUriStr.lastIndexOf('/') + 1;
            final String artikelIdStr = artikelUriStr.substring(startPos);
            try {
                final long artikelId = Long.parseLong(artikelIdStr);
                artikelIds.add(artikelId);
            } catch (NumberFormatException ignore) {
                // Ungueltige Artikel-ID: wird nicht beruecksichtigt
                if (LOGGER.isLoggable(FINER)) {
                    LOGGER.finer("Keine gueltige Artikel-Nr.: " + artikelIdStr);
                }
            }
        });
        
        if (artikelIds.isEmpty()) {
            // keine einzige Artikel-ID als gueltige Zahl
            throw new InvalidArtikelIdException();
        }
        
        final Optional<List<Artikel>> gefundeneArtikelOpt = katalogBrokerInstance.get().findByIds(artikelIds);
        if (!gefundeneArtikelOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        
        final List<Artikel> gefundeneArtikel = gefundeneArtikelOpt.get();
        
        // Bestellpositionen haben URIs fuer persistente Artikel.
        // Diese persistenten Artikel wurden in einem DB-Zugriff ermittelt (s.o.)
        // Fuer jede Bestellposition wird der Artikel passend zur Artikel-URI bzw. Artikel-ID gesetzt.
        // Bestellpositionen mit nicht-gefundene Artikel werden eliminiert.
        int i = 0;
        final List<Bestellposition> neueBestellpositionen = new ArrayList<>();
        for (Bestellposition bp : bestellung.getBestellpositionen()) {
            // Artikel-ID der aktuellen Bestellposition (s.o.):
            // artikelIds haben gleiche Reihenfolge wie bestellpositionen
            // i ist nicht final: kein Lambda, sondern for-Schleife
            final long artikelId = artikelIds.get(i++);
            
            // Wurde der Artikel beim DB-Zugriff gefunden?
            final Optional<Artikel> artikel = gefundeneArtikel.stream()
                                                              .filter(a -> a.getId() == artikelId)
                                                              .findAny();
            if (artikel.isPresent()) {
                bp.setArtikel(artikel.get());
                neueBestellpositionen.add(bp);                
            }
        }
        bestellung.setBestellpositionen(neueBestellpositionen);
        
        final Bestellung result = bestellungenBroker.save(bestellung);
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest(result.toString());
        }

        return Response.created(getUriBestellung(result, uriInfo))
                       .build();
    }
    
    /**
     * Mit der URI /bestellungen eine neue Bestellung aus dem persistenten Warenkorb anlegen<br>
     * Header: content-type application/x-www-form-urlencoded<br>
     * @param uriInfo injiziertes Objekt f&uuml;r UriInfo
     * @return Response mit Location
     */
    @POST
    @Consumes(APPLICATION_FORM_URLENCODED)
    @RolesAllowed(KUNDE_STRING)
    @ApiOperation(value = "Eine neue Bestellung aus dem vorhandenen Warenkorb erzeugen")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Created (OK)"),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden.")
    })
    public Response save(@Context UriInfo uriInfo) {
        // Bestellung aus dem persistenten Warenkorb
        final Bestellung neueBestellung = bestellungenBroker.save();
        return neueBestellung == null
               ? Response.status(BAD_REQUEST).build()
               : Response.created(getUriBestellung(neueBestellung, uriInfo)).build();
    }
    
    /**
     * Mit der URI /bestellungen/warenkorbposition eine neue Position im persistenten Warenkorb anlegen<br>
     * Header: content-type application/x-www-form-urlencoded<br>
     * Beispiel: anzahl=1&amp;artikelUri=http://localhost:8443/shop/rest/artikel/301<br>
     * @param warenkorbposition die neue Position im Warenkorb
     * @return Response ohne Location
     */
    @POST
    @Path("/warenkorbposition")
    @Consumes(APPLICATION_FORM_URLENCODED)
    @RolesAllowed(KUNDE_STRING)
    @ApiOperation(value = "Eine neue Position zum vorhandenen Warenkorb hinzuf\u00FCgen")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Created (OK)"),
        @ApiResponse(code = 403, message = "Die notwendigen Berechtigungen f\u00FCr diese Operation sind nicht vorhanden."),
        @ApiResponse(code = 404, message = "Es gibt keinen Kunden mit einer Bestellung zur angegebenen Bestellnummer")
    })
    public Response saveWarenkorbPosition(@BeanParam @Valid Warenkorbposition warenkorbposition) {
        // Artikel zur uebergebenen URI ermitteln
        final String artikelUri = warenkorbposition.getArtikelUri().toString();
        final int startPos = artikelUri.lastIndexOf('/') + 1;
        final String artikelIdStr = artikelUri.substring(startPos);
        long artikelId;
        try {
            artikelId = Long.parseLong(artikelIdStr);
        } catch (NumberFormatException ignore) {
            // Ungueltige Artikel-ID: wird nicht beruecksichtigt
            if (LOGGER.isLoggable(FINER)) {
                LOGGER.finer("Keine gueltige Artikel-Nr.: " + artikelIdStr);
            }
            return Response.status(BAD_REQUEST).build();
        }
        
        final Optional<Artikel> artikelOpt = katalogBrokerInstance.get().findById(artikelId);
        if (!artikelOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        final Artikel artikel = artikelOpt.get();
        
        warenkorbposition.setArtikel(artikel);
        
        // Kunde zum Username ermitteln
        final Optional<? extends AbstractKunde> kundeOpt = kundenBrokerInstance.get().findByLoginname(MIT_BESTELLUNGEN);
        if (!kundeOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        final AbstractKunde kunde = kundeOpt.get();
        warenkorbposition.setKunde(kunde);

        // Ermittelten Artikel mit zugehoeriger Anzahl
        bestellungenBroker.createWarenkorbposition(warenkorbposition);
        // Header ohne "Location"
        return Response.created(null).build();
    }
    
    
    //--------------------------------------------------------------------------
    // Methoden fuer URIs und Links
    //--------------------------------------------------------------------------
    
    public URI getUriBestellung(Bestellung bestellung, UriInfo uriInfo) {
        return uriHelper.getUri(BestellungenResource.class, FIND_BY_ID, bestellung.getId(), uriInfo);
    }
    
    public void setStructuralLinks(Bestellung bestellung, UriInfo uriInfo) {
        // URI fuer Kunde setzen
        final AbstractKunde kunde = bestellung.getKunde();
        if (kunde != null) {
            final URI kundeUri = uriHelper.getUri(KundenResource.class, KundenResource.FIND_BY_ID, bestellung.getKunde().getId(), uriInfo);
            bestellung.setKundeUri(kundeUri);
        }
                
        // URI fuer Artikel in den Bestellpositionen setzen
        final Collection<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
        if (bestellpositionen != null) {
            bestellpositionen.forEach(bp -> {
                final URI artikelUri = uriHelper.getUri(KatalogResource.class, KatalogResource.FIND_BY_ID, bp.getArtikel().getId(), uriInfo);
                bp.setArtikelUri(artikelUri);
            });
        }
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest(bestellung.toString());
        }
    }

    private Link[] getTransitionalLinks(Bestellung bestellung, UriInfo uriInfo) {
        final Link self = Link.fromUri(getUriBestellung(bestellung, uriInfo))
                              .rel(SELF_LINK)
                              .build();
        final Link add = Link.fromUri(uriHelper.getUri(BestellungenResource.class, uriInfo))
                              .rel(ADD_LINK)
                              .build();

        return new Link[] { self, add };
    }
    
    private Link[] getTransitionalLinks(List<Bestellung> bestellungen,
                                        AbstractKunde kunde,
                                        UriInfo uriInfo) {
        if (bestellungen == null || bestellungen.isEmpty()) {
            return new Link[0];
        }
        
        final URI selfUri = uriHelper.getUri(BestellungenResource.class, FIND_BY_KUNDE_ID, kunde.getId(), uriInfo);
        final Link self = Link.fromUri(selfUri)
                              .rel(SELF_LINK)
                              .build();
        
        final Link first = Link.fromUri(getUriBestellung(bestellungen.get(0), uriInfo))
                               .rel(FIRST_LINK)
                               .build();
        
        final int lastPos = bestellungen.size() - 1;
        final Link last = Link.fromUri(getUriBestellung(bestellungen.get(lastPos), uriInfo))
                              .rel(LAST_LINK)
                              .build();
        
        return new Link[] { self, first, last };
    }
}
