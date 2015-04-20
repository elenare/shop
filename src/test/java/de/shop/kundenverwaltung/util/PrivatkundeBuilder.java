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
package de.shop.kundenverwaltung.util;

import de.shop.iam.domain.IdentityVO;
import de.shop.kundenverwaltung.domain.FamilienstandType;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class PrivatkundeBuilder {
    private final Privatkunde kunde = new Privatkunde();
    
    public PrivatkundeBuilder identity(IdentityVO identity) {
        kunde.setIdentity(identity);
        kunde.setLoginname(identity.getLoginname());
        return this;
    }

    public PrivatkundeBuilder kategorie(int kategorie) {
        kunde.setKategorie(kategorie);
        return this;        
    }

    public PrivatkundeBuilder rabatt(BigDecimal rabatt) {
        kunde.setRabatt(rabatt);
        return this;        
    }

    public PrivatkundeBuilder umsatz(BigDecimal umsatz) {
        kunde.setUmsatz(umsatz);
        return this;        
    }
    
    public PrivatkundeBuilder seit(Date seit) {
        kunde.setSeit(seit);
        return this;
    }
    
    public PrivatkundeBuilder newsletter(boolean newsletter) {
        kunde.setNewsletter(newsletter);
        return this;
    }
    
    public PrivatkundeBuilder familienstand(FamilienstandType familienstand) {
        kunde.setFamilienstand(familienstand);
        return this;
    }
    
    public PrivatkundeBuilder hobbys(Set<HobbyType> hobbys) {
        kunde.setHobbys(hobbys);
        return this;
    }
    
    public PrivatkundeBuilder agbAkzeptiert(boolean agbAkzeptiert) {
        kunde.setAgbAkzeptiert(agbAkzeptiert);
        return this;
    }
    
    public Privatkunde build() {
        return kunde;
    }
}
