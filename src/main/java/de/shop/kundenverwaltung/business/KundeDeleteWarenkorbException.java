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

package de.shop.kundenverwaltung.business;

import de.shop.bestellverwaltung.domain.Warenkorbposition;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import java.util.List;
import javax.ejb.ApplicationException;


/**
 * Exception, die ausgel&ouml;st wird, wenn ein Kunde gel&ouml;scht werden soll, aber einen angefangenen Warenkorb hat
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@ApplicationException(rollback = true)
public class KundeDeleteWarenkorbException extends AbstractKundenverwaltungException {
    private static final long serialVersionUID = 2237194289969083093L;
    
    private static final String MESSAGE_KEY = "kunde.deleteMitWarenkorb";
    private final long kundeId;
    private final transient List<Warenkorbposition> warenkorb;
    
    public KundeDeleteWarenkorbException(AbstractKunde kunde, List<Warenkorbposition> warenkorb) {
        super("Kunde mit ID=" + kunde.getId() + " kann nicht geloescht werden: "
              + warenkorb.size() + " Warenkorbposition(en)");
        this.kundeId = kunde.getId();
        this.warenkorb = warenkorb;
    }

    public long getKundeId() {
        return kundeId;
    }
    public List<Warenkorbposition> getWarenkorb() {
        return warenkorb;
    }
    
    @Override
    public String getMessageKey() {
        return MESSAGE_KEY;
    }
}
