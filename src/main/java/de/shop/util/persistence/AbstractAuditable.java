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

package de.shop.util.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import static javax.persistence.TemporalType.TIMESTAMP;
import static javax.xml.bind.annotation.XmlAccessType.FIELD;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@MappedSuperclass
@XmlAccessorType(FIELD)
public abstract class AbstractAuditable {
    @Temporal(TIMESTAMP)
    @Basic(optional = false)
    @XmlTransient
    @JsonIgnore
    private Date erzeugt;

    @Temporal(TIMESTAMP)
    @Basic(optional = false)
    @XmlTransient
    @JsonIgnore
    private Date aktualisiert;
    
    @PrePersist
    protected void prePersist() {
        erzeugt = new Date();
        aktualisiert = new Date();
    }
    
    @PreUpdate
    protected void preUpdate() {
        aktualisiert = new Date();
    }
    
    public Date getErzeugt() {
        return erzeugt == null ? null : (Date) erzeugt.clone();
    }

    public void setErzeugt(Date erzeugt) {
        this.erzeugt = erzeugt == null ? null : (Date) erzeugt.clone();
    }

    public Date getAktualisiert() {
        return aktualisiert == null ? null : (Date) aktualisiert.clone();
    }

    public void setAktualisiert(Date aktualisiert) {
        this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert.clone();
    }
    
    @Override
    public String toString() {
        return "AbstractAuditable {erzeugt=" + erzeugt + ", aktualisiert=" + aktualisiert + '}';
    }
}
