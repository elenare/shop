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

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.enterprise.inject.Vetoed;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;

import static de.shop.util.Constants.HASH_PRIME;
import static de.shop.util.Constants.HASH_SHIFT;
import static javax.persistence.TemporalType.DATE;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Embeddable
@Vetoed
// Eine Klasse mit @Embeddable muss das Interface Serializable implementieren
public class ReklamationId implements Serializable {
    private static final long serialVersionUID = 9155270685609346099L;

    private long nr;

    @Temporal(DATE)
    private Date datum;

    @Override
    public int hashCode() {
        final int prime = HASH_PRIME;
        int result = prime + Objects.hashCode(datum);
        return prime * result + (int) (nr ^ (nr >>> HASH_SHIFT));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ReklamationId other = (ReklamationId) obj;
        if (nr != other.nr) {
            return false;
        }
        return Objects.equals(datum, other.datum);
    }

    public long getNr() {
        return nr;
    }

    public void setNr(long nr) {
        this.nr = nr;
    }

    public Date getDatum() {
        return datum == null ? null : (Date) datum.clone();
    }

    public void setDatum(Date datum) {
        this.datum = datum == null ? null : (Date) datum.clone();
    }
}
