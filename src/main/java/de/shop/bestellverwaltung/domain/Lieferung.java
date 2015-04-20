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

package de.shop.bestellverwaltung.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wordnik.swagger.annotations.ApiModelProperty;
import de.shop.util.persistence.AbstractVersionedAuditable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.PostPersist;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import static de.shop.util.Constants.HASH_PRIME;
import static java.util.logging.Level.FINER;
import static javax.persistence.CascadeType.PERSIST;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@XmlRootElement
@Entity
@NamedQueries({
    @NamedQuery(name  = Lieferung.BY_LIEFERNR,
                query = "SELECT l"
                        + " FROM Lieferung l"
                        + " WHERE l.liefernr LIKE :" + Lieferung.PARAM_LIEFER_NR)
})
@NamedEntityGraphs({
    @NamedEntityGraph(name = Lieferung.GRAPH_BESTELLUNGEN,
                      attributeNodes = @NamedAttributeNode("bestellungen"))
})
public class Lieferung extends AbstractVersionedAuditable {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private static final int LIEFERNR_LENGTH_MAX = 12;
    
    private static final String PREFIX = "Lieferung.";
    public static final String BY_LIEFERNR = PREFIX + "byLiefernr";
    public static final String PARAM_LIEFER_NR = "liefernr";
    
    public static final String GRAPH_BESTELLUNGEN = PREFIX + "bestellungen";

    
    @Id
    @GeneratedValue
    private long id;

    @NotNull(message = "{lieferung.lieferNr.notNull}")
    @Size(max = LIEFERNR_LENGTH_MAX, message = "{lieferung.lieferNr.length}")
    @Column(unique = true)
    @ApiModelProperty(required = true)
    private String liefernr;
    
    @Column(name = "transport_art", length = 3)
    @ApiModelProperty(dataType = "string", allowableValues = "STRASSE,SCHIENE,LUFT,WASSER")
    private TransportType transportArt;

    @ManyToMany(mappedBy = "lieferungen", cascade = PERSIST)
    @OrderBy("id ASC")
    @NotNull(message = "{lieferung.bestellungen.notEmpty}")
    @Size(min = 1, message = "{lieferung.bestellungen.notEmpty}")
    @XmlTransient
    @JsonIgnore
    private Set<Bestellung> bestellungen;
    
    @PostPersist
    private void postPersist() {
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("Neue Lieferung mit ID=" + id);
        }
    }

    public long getId() {
        return id;
    }

    public String getLiefernr() {
        return liefernr;
    }
    public void setLiefernr(String liefernr) {
        this.liefernr = liefernr;
    }

    public TransportType getTransportArt() {
        return transportArt;
    }
    public void setTransportArt(TransportType transportArt) {
        this.transportArt = transportArt;
    }

    public Set<Bestellung> getBestellungen() {
        return bestellungen == null ? null : Collections.unmodifiableSet(bestellungen);
    }
    
    @SuppressWarnings("null")
    public void setBestellungen(Set<Bestellung> bestellungen) {
        if (this.bestellungen == null) {
            this.bestellungen = bestellungen;
            return;
        }
        
        // Wiederverwendung der vorhandenen Collection
        this.bestellungen.clear();
        if (bestellungen != null) {
            this.bestellungen.addAll(bestellungen);
        }
    }
    
    public void addBestellung(Bestellung bestellung) {
        if (bestellungen == null) {
            bestellungen = new HashSet<>();
        }
        bestellungen.add(bestellung);
    }
    
    public List<Bestellung> getBestellungenAsList() {
        return bestellungen == null ? null : new ArrayList<>(bestellungen);
    }
    
    public void setBestellungenAsList(List<Bestellung> bestellungen) {
        this.bestellungen = bestellungen == null ? null : new HashSet<>(bestellungen);
    }
    
    @Override
    public String toString() {
        return "Lieferung {id=" + id + ", lieferNr=" + liefernr + ", transportArt=" + transportArt
               + ", " + super.toString() + '}';
    }

    @Override
    public int hashCode() {
        final int prime = HASH_PRIME;
        int result = prime + Objects.hashCode(liefernr);
        return prime * result + getVersion();
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
        final Lieferung other = (Lieferung) obj;
        if (!Objects.equals(liefernr, other.liefernr)) {
            return false;
        }
        return getVersion() == other.getVersion();
    }
}
