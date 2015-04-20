/*
 * Copyright (C) 2014 Hochschule Karlsruhe
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
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.persistence.AbstractVersionedAuditable;
import java.net.URI;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import static de.shop.util.Constants.HASH_PRIME;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@XmlRootElement
@Entity
@Table(indexes = {
    @Index(columnList = "artikel_fk"),
    @Index(columnList = "kunde_fk")
})
@NamedQueries({
    @NamedQuery(name = Warenkorbposition.FIND_POSITIONEN_BY_KUNDE,
                query = "SELECT wp FROM Warenkorbposition wp WHERE wp.kunde = :" + Warenkorbposition.PARAM_KUNDE)
})
public class Warenkorbposition extends AbstractVersionedAuditable {
    private static final String PREFIX = "Warenkorbposition.";
    public static final String FIND_POSITIONEN_BY_KUNDE = PREFIX + "findPositionenByKunde";
    public static final String PARAM_KUNDE = "kunde";
    
    @Id
    @GeneratedValue
    private long id;
    
    @Min(value = 1, message = "{warenkorbposition.anzahl.min}")
    @FormParam(value = "anzahl")
    @ApiModelProperty(required = true, value = "range[1, infinity]")
    private int anzahl;
    
    @ManyToOne
    @JoinColumn(name = "artikel_fk", nullable = false)
    @XmlTransient
    @JsonIgnore
    private Artikel artikel;
    
    @FormParam(value = "artikelUri")
    @Transient
    private URI artikelUri;
    
    @ManyToOne
    @JoinColumn(name = "kunde_fk", nullable = false)
    @XmlTransient
    @JsonIgnore
    private AbstractKunde kunde;
    
    @Transient
    private URI kundeUri;

    public long getId() {
        return id;
    }

    public int getAnzahl() {
        return anzahl;
    }

    public void setAnzahl(int anzahl) {
        this.anzahl = anzahl;
    }

    public Artikel getArtikel() {
        return artikel;
    }

    public void setArtikel(Artikel artikel) {
        this.artikel = artikel;
    }

    public URI getArtikelUri() {
        return artikelUri;
    }

    public void setArtikelUri(URI artikelUri) {
        this.artikelUri = artikelUri;
    }

    public AbstractKunde getKunde() {
        return kunde;
    }

    public void setKunde(AbstractKunde kunde) {
        this.kunde = kunde;
    }

    public URI getKundeUri() {
        return kundeUri;
    }

    public void setKundeUri(URI kundeUri) {
        this.kundeUri = kundeUri;
    }

    @Override
    public int hashCode() {
        final int prime = HASH_PRIME;
        int hash = prime + this.anzahl;
        hash = prime * hash + Objects.hashCode(artikel);
        hash = prime * hash + Objects.hashCode(artikelUri);
        hash = prime * hash + Objects.hashCode(kunde);
        return prime * hash + Objects.hashCode(kundeUri);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Warenkorbposition other = (Warenkorbposition) obj;
        if (!Objects.equals(artikelUri, other.artikelUri)) {
            return false;
        }
        if (!Objects.equals(artikel, other.artikel)) {
            return false;
        }
        if (anzahl != other.anzahl) {
            return false;
        }
        if (!Objects.equals(kundeUri, other.kundeUri)) {
            return false;
        }
        return Objects.equals(kunde, other.kunde);
    }

    @Override
    public String toString() {
        final String artikelId = artikel == null ? "" : ", artikelId=" + artikel.getId();
        final String kundeId = kunde == null ? "" : ", kundeId=" + kunde.getId();
        return "Warenkorbposition{" + "id=" + id + ", anzahl=" + anzahl + artikelId + ", artikelUri="
               + artikelUri + kundeId + ", kundeUri=" + kundeUri + '}';
    }
    
}
