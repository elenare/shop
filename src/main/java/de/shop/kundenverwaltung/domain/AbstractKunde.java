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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wordnik.swagger.annotations.ApiModelProperty;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.iam.domain.IdentityVO;
import de.shop.util.persistence.AbstractVersionedAuditable;
import de.shop.util.persistence.File;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.enterprise.inject.Vetoed;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import org.jboss.resteasy.annotations.Form;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;
import static de.shop.util.Constants.HASH_PRIME;
import static java.util.logging.Level.FINER;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.TemporalType.DATE;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@JsonTypeInfo(use = NAME, include = PROPERTY, property = "type")
@JsonSubTypes({
    @Type(value = Privatkunde.class, name = AbstractKunde.PRIVATKUNDE),
    @Type(value = Firmenkunde.class, name = AbstractKunde.FIRMENKUNDE) })
@XmlRootElement
@XmlSeeAlso({ Firmenkunde.class, Privatkunde.class })
//MOXy statt Jackson
//@XmlDiscriminatorNode("@type")
//@Formatted
@Entity
@Table(name = "kunde", indexes = @Index(columnList = "file_fk"))
// Alternativen: strategy = SINGLE_TABLE (=default), TABLE_PER_CLASS, JOINED
@Inheritance
@DiscriminatorColumn(name = "art", length = 1)
@NamedQueries({
    @NamedQuery(name  = AbstractKunde.ALL,
                query = "SELECT  k"
                        + " FROM AbstractKunde k"),
    @NamedQuery(name  = AbstractKunde.ALL_ORDER_BY_ID,
                query = "SELECT   k"
                        + " FROM  AbstractKunde k"
                        + " ORDER BY k.id"),
    @NamedQuery(name  = AbstractKunde.IDS_BY_PREFIX,
                query = "SELECT   k.id"
                        + " FROM  AbstractKunde k"
                        + " WHERE CONCAT('', k.id) LIKE :" + AbstractKunde.PARAM_KUNDE_ID_PREFIX
                        + " ORDER BY k.id"),
    @NamedQuery(name  = AbstractKunde.BY_ID_PREFIX,
                query = "SELECT   k"
                        + " FROM  AbstractKunde k"
                        + " WHERE CONCAT('', k.id) LIKE :" + AbstractKunde.PARAM_KUNDE_ID_PREFIX
                        + " ORDER BY k.id"),
    @NamedQuery(name  = AbstractKunde.BY_LOGINNAME,
                query = "SELECT   k"
                        + " FROM  AbstractKunde k"
                        + " WHERE loginname = :" + AbstractKunde.PARAM_LOGINNAME),
    @NamedQuery(name  = AbstractKunde.BY_BESTELLUNG_ID,
                query = "SELECT DISTINCT b.kunde"
                        + " FROM   Bestellung b"
                        + " WHERE  b.id = :" + AbstractKunde.PARAM_BESTELLUNG_ID),
    @NamedQuery(name  = AbstractKunde.OHNE_BESTELLUNGEN,
                query = "SELECT DISTINCT k"
                        + " FROM   AbstractKunde k"
                        + " WHERE  k.bestellungen IS EMPTY AND NOT k IN (SELECT DISTINCT wp.kunde"
                        + "                                              FROM   Warenkorbposition wp)"),
    @NamedQuery(name = AbstractKunde.BY_SEIT,
                query = "SELECT   k"
                        + " FROM  AbstractKunde k"
                        + " WHERE k.seit = :" + AbstractKunde.PARAM_KUNDE_SEIT),
    @NamedQuery(name = AbstractKunde.PRIVATKUNDEN_FIRMENKUNDEN,
                query = "SELECT   k"
                        + " FROM  AbstractKunde k"
                        + " WHERE TYPE(k) IN (Privatkunde, Firmenkunde)")
})
@NamedEntityGraphs({
    @NamedEntityGraph(name = AbstractKunde.GRAPH_BESTELLUNGEN,
                      attributeNodes = @NamedAttributeNode("bestellungen")),
    @NamedEntityGraph(name = AbstractKunde.GRAPH_REKLAMATIONEN,
                      attributeNodes = @NamedAttributeNode("reklamationen"))
})
@EntityListeners(KundeListener.class)
@Vetoed
public abstract class AbstractKunde extends AbstractVersionedAuditable implements Cloneable {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private static final long KATEGORIE_MAX = 5;
    private static final String RABATT_MAX = "0.5";
    private static final int BEMERKUNGEN_LENGTH_MAX = 2000;
    
    public static final String PRIVATKUNDE = "P";
    public static final String FIRMENKUNDE = "F";
    
    private static final String PREFIX = "AbstractKunde.";
    public static final String ALL = PREFIX + "all";
    public static final String ALL_ORDER_BY_ID = PREFIX + "allOrderById";
    public static final String IDS_BY_PREFIX = PREFIX + "idsByIdPrefix";
    public static final String BY_ID_PREFIX = PREFIX + "byIdPrefix";
    public static final String BY_LOGINNAME = PREFIX + "byLoginname";
    public static final String BY_BESTELLUNG_ID = PREFIX + "byBestellungId";
    public static final String OHNE_BESTELLUNGEN = PREFIX + "ohneBestellungen";
    public static final String BY_SEIT = PREFIX + "bySeit";
    public static final String PRIVATKUNDEN_FIRMENKUNDEN = PREFIX + "privatkundenFirmenkunden";

    public static final String PARAM_KUNDE_ID = "id";
    public static final String PARAM_LOGINNAME = "loginname";
    public static final String PARAM_KUNDE_ID_PREFIX = "idPrefix";
    public static final String PARAM_KUNDE_SEIT = "seit";
    public static final String PARAM_BESTELLUNG_ID = "bestellungId";
    
    public static final String GRAPH_BESTELLUNGEN = PREFIX + "bestellungen";
    public static final String GRAPH_REKLAMATIONEN = PREFIX + "reklamationen";

    
    @Id
    @GeneratedValue
    @FormParam(value = "id")
    private long id;
    
    @XmlTransient
    @JsonIgnore
    private String loginname;

    @Valid
    @NotNull(message = "{kunde.identity.notNull}")
    @Transient
    @Form
    @ApiModelProperty(required = true)
    private IdentityVO identity;

    @Max(value = KATEGORIE_MAX, message = "{kunde.kategorie.max}")
    @Min(value = 0, message = "{kunde.kategorie.min}")
    @FormParam(value = "kategorie")
    @ApiModelProperty(required = true, value = "range[0," + KATEGORIE_MAX + "]")
    private int kategorie;
    
    @DecimalMax(value = RABATT_MAX, message = "{kunde.rabatt.max}")
    @Digits(integer = 1, fraction = 4, message = "{kunde.rabatt.digits}")
    @FormParam(value = "rabatt")
    @ApiModelProperty(required = true, example = "0.12")
    private BigDecimal rabatt;
    
    @Digits(integer = 10, fraction = 2, message = "{kunde.umsatz.digits}")
    @FormParam(value = "umsatz")
    @ApiModelProperty(required = true, example = "123.45")
    private BigDecimal umsatz;
    
    @NotNull(message = "{kunde.seit.notNull}")
    @Temporal(DATE)
    @Past(message = "{kunde.seit.past}")
    @FormParam(value = "seit")
    @ApiModelProperty(required = true)
    private Date seit;
    
    @FormParam(value = "newsletter")
    private boolean newsletter = false;

    @Transient
    @AssertTrue(message = "{kunde.agb}")
    @FormParam(value = "agbAkzeptiert")
    @ApiModelProperty(required = true)
    private boolean agbAkzeptiert;
    
    @Column
    @Size(max = BEMERKUNGEN_LENGTH_MAX)
    @FormParam(value = "bemerkungen")
    //@SafeHtml
    private String bemerkungen;
    
    // Default: fetch = LAZY, keine Kaskadierungen
    // Alternativen: cascade = PERSIST, cascade = { PERSIST, REMOVE } usw.
    @OneToMany
    @JoinColumn(name = "kunde_fk", nullable = false)
    @OrderColumn(name = "idx", nullable = false)
    @XmlTransient
    @JsonIgnore
    private List<Bestellung> bestellungen;
    
    @Transient
    @ApiModelProperty(required = true, example = "https://localhost:8443/shop/rest/bestellungen/123456")
    private URI bestellungenUri;
    
    @OneToMany
    @JoinColumn(name = "kunde_fk", nullable = false)
    @OrderColumn(name = "idx", nullable = false)
    @XmlTransient
    @JsonIgnore
    private List<Reklamation> reklamationen;
    
    @OneToOne(fetch = LAZY, cascade = { PERSIST, REMOVE })
    @JoinColumn(name = "file_fk")
    @XmlTransient
    @JsonIgnore
    private File file;
    
    public AbstractKunde() {
        super();
    }
    
    // fuer PrivatkundeVO bei SOAP
    public AbstractKunde(IdentityVO identity, Date seit) {
        super();
        this.loginname = identity.getLoginname();
        this.identity = identity;
        this.seit = seit;
    }

    @PostPersist
    protected void postPersist() {
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("Neuer Kunde mit ID=" + id);
        }
    }
    
    @PostLoad
    protected void postLoad() {
        agbAkzeptiert = true;
    }
    
    /**
     * {inheritDoc}
     */
    @Override
    public void setValues(AbstractVersionedAuditable newValues) {
        if (!(newValues instanceof AbstractKunde)) {
            return;
        }
        
        final AbstractKunde k = (AbstractKunde) newValues;
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("Original-Kunde VOR setValues: " + this);
            LOGGER.finer("Zu setzende Werte VOR setValues: " + k);
        }
        super.setValues(newValues);
        
        loginname = k.identity.getLoginname();
        identity.setValues(k.identity);
        
        kategorie = k.kategorie;
        umsatz = k.umsatz;
        seit = k.seit;
        agbAkzeptiert = k.agbAkzeptiert;
        bemerkungen = k.getBemerkungen();
        
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("Kunde NACH setValues: " + this);
        }
    }

    public long getId() {
        return id;
    }


    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public IdentityVO getIdentity() {
        return identity;
    }

    public void setIdentity(IdentityVO identity) {
        this.identity = identity;
    }

    public int getKategorie() {
        return kategorie;
    }

    public void setKategorie(int kategorie) {
        this.kategorie = kategorie;
    }
    
    public BigDecimal getRabatt() {
        return rabatt;
    }
    public void setRabatt(BigDecimal rabatt) {
        this.rabatt = rabatt;
    }

    public BigDecimal getUmsatz() {
        return umsatz;
    }

    public void setUmsatz(BigDecimal umsatz) {
        this.umsatz = umsatz;
    }

    public Date getSeit() {
        return seit == null ? null : (Date) seit.clone();
    }

    public void setSeit(Date seit) {
        this.seit = seit == null ? null : (Date) seit.clone();
    }

    public void setNewsletter(boolean newsletter) {
        this.newsletter = newsletter;
    }

    public boolean isNewsletter() {
        return newsletter;
    }
    
    public void setAgbAkzeptiert(boolean agbAkzeptiert) {
        this.agbAkzeptiert = agbAkzeptiert;
    }

    public boolean isAgbAkzeptiert() {
        return agbAkzeptiert;
    }

    public List<Bestellung> getBestellungen() {
        if (bestellungen == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(bestellungen);
    }
    public void setBestellungen(List<Bestellung> bestellungen) {
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
    
    public AbstractKunde addBestellung(Bestellung bestellung) {
        if (bestellungen == null) {
            bestellungen = new ArrayList<>();
        }
        bestellungen.add(bestellung);
        return this;
    }

    public URI getBestellungenUri() {
        return bestellungenUri;
    }

    public void setBestellungenUri(URI bestellungenUri) {
        this.bestellungenUri = bestellungenUri;
    }

    public List<Reklamation> getReklamationen() {
        if (reklamationen == null) {
            return Collections.emptyList();
        }
        
        return Collections.unmodifiableList(reklamationen);
    }

    public void setReklamationen(List<Reklamation> reklamationen) {
        if (this.reklamationen == null) {
            this.reklamationen = reklamationen;
            return;
        }
        
        // Wiederverwendung der vorhandenen Collection
        this.reklamationen.clear();
        if (reklamationen != null) {
            this.reklamationen.addAll(reklamationen);
        }
    }
    
    public AbstractKunde addReklamation(Reklamation reklamation) {
        if (reklamationen == null) {
            reklamationen = new ArrayList<>();
        }
        reklamationen.add(reklamation);
        return this;
    }

    public File getFile() {
        return file;
    }
        
    public void setFile(File file) {
        this.file = file;
    }

    public String getBemerkungen() {
        return bemerkungen;
    }

    public void setBemerkungen(String bemerkungen) {
        this.bemerkungen = bemerkungen;
    }

    @Override
    public String toString() {
        return "AbstractKunde {id=" + id + ", loginname=" + loginname
               + ", seit=" + seit + ", kategorie=" + kategorie + ", rabatt=" + rabatt
               + ", bestellungenUri=" + bestellungenUri + ", bemerkungen=" + bemerkungen
               + ", " + super.toString() + '}';
    }

    @Override
    public int hashCode() {
        return HASH_PRIME + Objects.hashCode(loginname);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AbstractKunde other = (AbstractKunde) obj;
        return Objects.equals(loginname, other.loginname);
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        final AbstractKunde neuesObjekt = (AbstractKunde) super.clone();
        
        neuesObjekt.id = id;
        neuesObjekt.loginname = loginname;
        neuesObjekt.identity = identity;
        neuesObjekt.kategorie = kategorie;
        neuesObjekt.umsatz = umsatz;
        neuesObjekt.newsletter = newsletter;
        neuesObjekt.agbAkzeptiert = agbAkzeptiert;
        neuesObjekt.bemerkungen = bemerkungen;
        neuesObjekt.setErzeugt(getErzeugt());
        neuesObjekt.setAktualisiert(getAktualisiert());
        return neuesObjekt;
    }
}
