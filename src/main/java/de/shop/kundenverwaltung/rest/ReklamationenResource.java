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

import de.shop.util.rest.REST;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.Response.Status.NOT_IMPLEMENTED;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@REST
@Path("/reklamationen")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
public class ReklamationenResource {
    /**
     * Mit der URI /reklamationen/kunde/{id} die Reklamationen ermitteln
     * zu einem bestimmten Kunden ermitteln
     * @param id ID des Kunden
     * @return reklamationen, falls die ID vorhanden ist
     */
    @GET
    @Path("/kunde/{id:[1-9]\\d*}")
    public Response findByKundeId(@PathParam("id") long id) {   //NOSONAR
        // Diese Methode ist bewusst NICHT implementiert, um zu zeigen,
        // wie man Methodensignaturen an der Schnittstelle fuer andere
        // Teammitglieder schon mal bereitstellt, indem einfach ein "Not implemented" (501)
        // produziert wird.
        // Die Kolleg/inn/en koennen nun weiterarbeiten, waehrend man selbst
        // gerade keine Zeit hat, weil andere Aufgaben Vorrang haben.
        
        // TODO findByKundeId noch nicht implementiert
        return Response.status(NOT_IMPLEMENTED)
                       .entity("findByKundeId: NOT YET IMPLEMENTED")
                       .type(TEXT_PLAIN)
                       .build();
    }
}
