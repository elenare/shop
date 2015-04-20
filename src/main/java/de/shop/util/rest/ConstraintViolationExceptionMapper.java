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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ResteasyViolationException;
import org.jboss.resteasy.api.validation.ViolationReport;
import org.jboss.resteasy.plugins.providers.validation.ViolationsContainer;

import static java.util.stream.Collectors.toSet;
import static javax.validation.ElementKind.RETURN_VALUE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.jboss.resteasy.api.validation.ConstraintType.Type.PARAMETER;
import static org.jboss.resteasy.api.validation.Validation.VALIDATION_HEADER;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Provider
@ApplicationScoped
@Log
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        return getAsResponse(exception);
    }
    
    static Response getAsResponse(ConstraintViolationException exception) {
        final Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        // Rueckgabewert null oder leere Liste? d.h. NOT_FOUND?
        if (violations.size() == 1) {
            final ConstraintViolation<?> violation = violations.iterator().next();
            
            // Ende des Validation-Pfades suchen
            final Iterator<Path.Node> pathIterator = violation.getPropertyPath().iterator();
            Path.Node node = null;
            while (pathIterator.hasNext()) {
                node = pathIterator.next();
            }
            
            // Verletzung des Rueckgabewertes?
            if (node != null && node.getKind() == RETURN_VALUE) {
                final Object invalidValue = violation.getInvalidValue();
                // null oder leere Liste?
                if (invalidValue == null || (invalidValue instanceof List && ((List<?>) invalidValue).isEmpty())) {
                    return Response.status(NOT_FOUND)
                                   .type(TEXT_PLAIN)
                                   .entity(violation.getMessage())
                                   .build();
                }
            }
        }
        
        return Response.status(BAD_REQUEST)
                       .entity(toViolationReport(violations))
                       .header(VALIDATION_HEADER, "true")
                       .build();
    }
        
    @SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown"})
    private static ViolationReport toViolationReport(Set<ConstraintViolation<?>> violations) {
        final Set<ResteasyConstraintViolation> parameterViolations = violations.stream()
                                                                               .map(v -> {
            final String path = v.getPropertyPath().toString();
            final String message = v.getMessage();
            final Object invalidValue = v.getInvalidValue();
            final String inalidValueStr = Objects.toString(invalidValue);
            return new ResteasyConstraintViolation(PARAMETER, path, message, inalidValueStr);
        }).collect(toSet());
        
        return new ViolationReport(new ResteasyViolationException(new ViolationsContainer<>(parameterViolations)));
    }
}
