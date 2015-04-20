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

package de.shop.kundenverwaltung.jsf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import java.io.IOException;
import java.nio.file.Paths;
import javax.enterprise.inject.Model;

import static com.lowagie.text.PageSize.A4;
import static java.lang.System.getenv;

/**
 * Aufbereitung des produzierenden PDF-Dokuments
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Model
public class PreProcessPDF {
    public void addLogo(Object document) throws IOException, DocumentException { //NOSONAR
        final Document pdf = (Document) document;
        pdf.open();
        pdf.setPageSize(A4);
        
        final String jbossHome = getenv("JBOSS_HOME");
        final String logo = Paths.get(jbossHome, "filesDB", "hs-logo.gif").toString();
        pdf.add(Image.getInstance(logo));
    }
}
