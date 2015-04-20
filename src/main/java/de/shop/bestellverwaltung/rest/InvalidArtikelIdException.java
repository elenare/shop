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

import de.shop.bestellverwaltung.business.AbstractBestellverwaltungException;
import javax.ejb.ApplicationException;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@ApplicationException(rollback = true)
public class InvalidArtikelIdException extends AbstractBestellverwaltungException {
    private static final long serialVersionUID = 4867667611097919943L;
    
    private static final String MESSAGE_KEY = "bestellung.artikel.id.invalid";
    
    public InvalidArtikelIdException() {
        super("Keine Artikel-ID ist gueltig");
    }

    @Override
    public String getMessageKey() {
        return MESSAGE_KEY;
    }
}
