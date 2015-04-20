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

package de.shop.artikelverwaltung.jsf;

import de.shop.artikelverwaltung.domain.Artikel;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@FacesConverter(forClass = Artikel.class)
public class ArtikelConverter implements Converter {
    @Override
    public String getAsString(FacesContext ctx, UIComponent comp, Object obj) {
        if (obj == null) {
            return "";
        }
        
        final Artikel artikel = (Artikel) obj;
        return String.valueOf(artikel.getId());
    }
    
    @Override
    public Object getAsObject(FacesContext ctx, UIComponent comp, String str) {
        final Long artikelId = Long.valueOf(str);
        final Map<String, Object> sessionMap = ctx.getExternalContext().getSessionMap();
        @SuppressWarnings("unchecked")
        final Map<Long, Artikel> verfuegbareArtikel = (Map<Long, Artikel>) sessionMap.get("verfuegbareArtikel");
        if (verfuegbareArtikel == null) {
            throw new ConverterException("Es gibt es keine verfuegbaren Artikel");
        }
        final Object artikel = verfuegbareArtikel.get(artikelId);
        if (artikel == null) {
            throw new ConverterException("Zur ID " + artikelId + " gibt es keinen verfuegbaren Artikel");
        }
        return artikel;
    }
}
