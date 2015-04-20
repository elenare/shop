/*
 * Copyright (C) 2015 Juergen Zimmermann, Hochschule Karlsruhe
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
package de.shop.util;

import javax.ws.rs.core.Response;
import org.assertj.core.api.AbstractAssert;

import static de.shop.util.Constants.SELF_LINK;
import static de.shop.util.LocationHelper.extractId;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNSUPPORTED_TYPE;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ResponseAssert extends AbstractAssert<ResponseAssert, Response> {
    protected ResponseAssert(Response actual) {
        super(actual, ResponseAssert.class);
    }
    
    public static ResponseAssert assertThatResponse(Response actual) {
        return new ResponseAssert(actual);
    }

    public ResponseAssert hasStatusOk() {
        return hasStatus(HTTP_OK);
    }

    public ResponseAssert hasStatusOkOrNoContent() {
        return hasStatus(HTTP_OK, HTTP_NO_CONTENT);
    }
    
    public ResponseAssert hasStatusOkOrNotFound() {
        return hasStatus(HTTP_OK, HTTP_NOT_FOUND);
    }
    
    public ResponseAssert hasStatusCreated() {
        return hasStatus(HTTP_CREATED);
    }

    public ResponseAssert hasStatusNoContent() {
        return hasStatus(HTTP_NO_CONTENT);
    }

    public ResponseAssert hasStatusBadRequest() {
        return hasStatus(HTTP_BAD_REQUEST);
    }

    public ResponseAssert hasStatusNotFound() {
        return hasStatus(HTTP_NOT_FOUND);
    }
    
    public ResponseAssert hasStatusUnauthorized() {
        return hasStatus(HTTP_UNAUTHORIZED);
    }
    
    public ResponseAssert hasStatusForbidden() {
        return hasStatus(HTTP_FORBIDDEN);
    }
    
    public ResponseAssert hasStatusForbiddenOrNotFound() {
        return hasStatus(HTTP_FORBIDDEN, HTTP_NOT_FOUND);
    }

    public ResponseAssert hasStatusConflict() {
        return hasStatus(HTTP_CONFLICT);
    }

    public ResponseAssert hasStatusUnsupportedType() {
        return hasStatus(HTTP_UNSUPPORTED_TYPE);
    }
    
    private ResponseAssert hasStatus(int status) {
        assertThat(actual.getStatus())
            .overridingErrorMessage("Der Status des Response muss %d sein, ist aber %d", status, actual.getStatus())
            .isEqualTo(status);
        return this;
    }
    
    private ResponseAssert hasStatus(int status1, int status2) {
        assertThat(actual.getStatus())
            .overridingErrorMessage("Der Status des Response muss %d oder %d sein, ist aber %d", status1, status2, actual.getStatus())
            .isIn(status1, status2);
        return this;
    }
    
    public ResponseAssert hasPositiveNewId() {
        final long actualId = extractId(actual);
        assertThat(actualId)
            .overridingErrorMessage("Die ID im Location-Header muss positiv sein, ist aber %d", actualId)
            .isPositive();
        return this;
    }
    
    public ResponseAssert hasId(long id) {
        final long actualId = extractId(actual);
        assertThat(actualId)
            .overridingErrorMessage("Die ID im Location-Header muss %d sein, ist aber %d", id, actualId)
            .isEqualTo(id);
        return this;
    }

    public ResponseAssert hasLinks() {
        assertThat(actual.getLinks())
            .overridingErrorMessage("Es gibt keinen Link-Header")
            .isNotEmpty();
        return this;
    }
    
    public ResponseAssert hasLink(String link) {
        assertThat(actual.getLink(link))
            .overridingErrorMessage("Es gibt keinen Link-Header mit dem Namen %s", link)
            .isNotNull();
        return this;
    }
    
    public ResponseAssert hasSelfLinkMitId(long id) {
        assertThat(actual.getLink(SELF_LINK).getUri().toString())
            .overridingErrorMessage("Es gibt keinen Link-Header self, der die ID %d enthaelt", id)
            .contains(String.valueOf(id));
        return this;
    }
    
    public ResponseAssert hasErrorMessage(String msg) {
        final String errorMessage = actual.readEntity(String.class);
    	assertThat(errorMessage)
            .overridingErrorMessage("Die Fehlermeldung muss %s sein, ist aber %s", msg, errorMessage)
            .isEqualTo(msg);
        return this;
    }
    
    public ResponseAssert hasErrorMessageStartingWith(String prefix) {
        final String errorMessage = actual.readEntity(String.class);
    	assertThat(errorMessage)
            .overridingErrorMessage("Die Fehlermeldung muss mit %s beginnen, ist aber %s", prefix, errorMessage)
            .startsWith(prefix);
        return this;
    }
    
    public ResponseAssert hasErrorMessageStartingEnding(String prefix, String suffix) {
        final String errorMessage = actual.readEntity(String.class);
    	assertThat(errorMessage)
            .overridingErrorMessage("Die Fehlermeldung muss mit %s beginnen, ist aber %s", prefix, errorMessage)
            .startsWith(prefix);
    	assertThat(errorMessage)
            .overridingErrorMessage("Die Fehlermeldung muss mit %s enden, ist aber %s", suffix, errorMessage)
            .endsWith(suffix);
        return this;
    }
    
    public ResponseAssert hasErrorMessageEndingWith(String suffix) {
        final String errorMessage = actual.readEntity(String.class);
    	assertThat(errorMessage)
            .overridingErrorMessage("Die Fehlermeldung muss mit %s enden, ist aber %s", suffix, errorMessage)
            .endsWith(suffix);
        return this;
    }
    
    public ResponseAssert hasValidationException() {
        final String headerKey = "validation-exception";
        final String headerValue = actual.getHeaderString(headerKey).toLowerCase();
        assertThat(headerValue)
            .overridingErrorMessage("Im Header muss der Eintrag %s den Wert true haben, hat aber den Wert %s", headerKey, headerValue)
            .isEqualTo("true");
        return this;
    }
}
