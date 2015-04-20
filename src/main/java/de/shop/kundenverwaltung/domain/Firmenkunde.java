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

package de.shop.kundenverwaltung.domain;

import javax.enterprise.inject.Vetoed;
import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.xml.bind.annotation.XmlRootElement;

import static de.shop.kundenverwaltung.domain.AbstractKunde.FIRMENKUNDE;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@XmlRootElement
//MOXy statt Jackson
//@XmlDiscriminatorValue(AbstractKunde.FIRMENKUNDE)
@Entity
@Inheritance
@DiscriminatorValue(FIRMENKUNDE)
@Cacheable
@Vetoed
@SuppressWarnings("CloneableImplementsClone")
public class Firmenkunde extends AbstractKunde {
    @Override
    public String toString() {
        return "Firmenkunde {" + super.toString() + '}';
    }
}
