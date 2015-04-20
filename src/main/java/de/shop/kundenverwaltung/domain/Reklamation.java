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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wordnik.swagger.annotations.ApiModelProperty;
import de.shop.util.persistence.AbstractVersionedAuditable;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;
import javax.enterprise.inject.Vetoed;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostPersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import static de.shop.util.Constants.HASH_PRIME;
import static de.shop.util.Constants.HASH_SHIFT;
import static java.util.logging.Level.FINER;
import static javax.persistence.TemporalType.DATE;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@XmlRootElement
@Entity
@Table(indexes = @Index(columnList = "kunde_fk"))
@NamedQueries({
    @NamedQuery(name  = Reklamation.FIND_BY_KUNDE_ID,
                query = "SELECT r"
                        + " FROM   Reklamation r"
                        + " WHERE  r.kunde.id = :" + Reklamation.PARAM_KUNDE_ID)
})
@IdClass(ReklamationId.class)
@Vetoed
@SuppressWarnings("CloneableImplementsClone")
public class Reklamation  extends AbstractVersionedAuditable {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    public static final String PREFIX = "Reklamation.";
    public static final String FIND_BY_KUNDE_ID =
                               PREFIX + "findByKundeId";
    public static final String PARAM_KUNDE_ID = "kundeId";

    @Id
    @ApiModelProperty(required = true)
    private long nr;

    @Id
    @NotNull(message = "{Reklamation.datum.notNull}")
    @Temporal(DATE)
    @ApiModelProperty(required = true)
    private Date datum;
    
    private String inhalt;

    @NotNull(message = "{reklamation.kunde.notNull}")
    @ManyToOne(optional = false)
    @JoinColumn(name = "kunde_fk", nullable = false, insertable = false, updatable = false)
    @XmlTransient
    @JsonIgnore
    private AbstractKunde kunde;
    
    public Reklamation() {
        super();
    }
    
    public Reklamation(long nr, Date datum) {
        super();
        this.nr = nr;
        this.datum = datum == null ? null : (Date) datum.clone();
    }
    
    @PostPersist
    private void postPersist() {
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("Neue Reklamation mit Nr=" + nr + "/Datum=" + datum);
        }
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

    public String getInhalt() {
        return inhalt;
    }

    public void setInhalt(String inhalt) {
        this.inhalt = inhalt;
    }

    public AbstractKunde getKunde() {
        return kunde;
    }

    public void setKunde(AbstractKunde kunde) {
        this.kunde = kunde;
    }

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
        final Reklamation other = (Reklamation) obj;
        if (!Objects.equals(datum, other.datum)) {
            return false;
        }
        return nr != other.nr;
    }

    @Override
    public String toString() {
        return "Reklamation {nr=" + nr + ", datum=" + datum + ", inhalt=" + inhalt + ", " + super.toString() + '}';
    }
}
