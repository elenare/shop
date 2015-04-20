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

import java.lang.reflect.Method;
import java.net.URI;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriInfo;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@ApplicationScoped
public class UriHelper {
    UriHelper() {
        super();
    }
    
    public URI getUri(Class<?> clazz, UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                      .path(clazz)
                      .build();
    }

    public URI getUri(Class<?> clazz, Method method, long id, UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                      .path(clazz)
                      .path(clazz, method.getName())
                      .build(id);
    }
}
