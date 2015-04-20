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

package de.shop.kundenverwaltung.soap;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.FamilienstandType;
import de.shop.kundenverwaltung.domain.GeschlechtType;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.util.ShopRuntimeException;
import de.shop.util.persistence.AbstractVersionedAuditable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static de.shop.util.Constants.HASH_PRIME;
import static java.util.stream.Collectors.toList;

/**
 * Value Object (VO) fuer die Domain-Klasse Privatkunde
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class PrivatkundeVO {
    private long id;
    private int version;
    private String loginname;
    private IdentitySoapVO identity;
    private int kategorie;
    private BigDecimal rabatt;
    private BigDecimal umsatz;
    private Date seit;
    private boolean agbAkzeptiert;
    private List<Long> bestellungenIds;
    private String bemerkungen;
    
    private FamilienstandType familienstand;
    private GeschlechtType geschlecht;
    private Set<HobbyType> hobbys;
    
    public PrivatkundeVO() {
        super();
    }
    
    public PrivatkundeVO(Privatkunde pk) {
        super();
        id = pk.getId();
        version = pk.getVersion();
        loginname = pk.getLoginname();
        identity = new IdentitySoapVO(pk.getIdentity());
        kategorie = pk.getKategorie();
        rabatt = pk.getRabatt();
        umsatz = pk.getUmsatz();
        seit = pk.getSeit();
        agbAkzeptiert = pk.isAgbAkzeptiert();
        final List<Bestellung> bestellungen = pk.getBestellungen();
        if (bestellungen != null) {
            bestellungenIds = bestellungen.stream()
                                          .map(Bestellung::getId)
                                          .collect(toList());
        }
        bemerkungen = pk.getBemerkungen();
        familienstand = pk.getFamilienstand();
        geschlecht = pk.getGeschlecht();
        hobbys = pk.getHobbys();
    }
    
    public Privatkunde toPrivatkunde() {
        final Privatkunde pk = new Privatkunde(identity.toIdentityVO(), seit);

        // Die private Attribute "id" und "version" setzen, ohne dass es eine set-Methode gibt
        try {
            final Field idField = AbstractKunde.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pk, id);

            final Field versionField = AbstractVersionedAuditable.class.getDeclaredField("version");
            versionField.setAccessible(true);
            versionField.setInt(pk, version);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
        
        pk.setIdentity(identity.toIdentityVO());
        pk.setKategorie(kategorie);
        pk.setRabatt(rabatt);
        pk.setUmsatz(umsatz);
        pk.setAgbAkzeptiert(agbAkzeptiert);
        // TODO SOAP: Bestellungen aus Bestellungen-IDs ermitteln
        pk.setBemerkungen(bemerkungen);
        pk.setFamilienstand(familienstand);
        pk.setGeschlecht(geschlecht);
        pk.setHobbys(hobbys);
        return pk;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }
    public String getLoginname() {
        return loginname;
    }
    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public IdentitySoapVO getIdentity() {
        return identity;
    }

    public void setIdentity(IdentitySoapVO identity) {
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
    public boolean isAgbAkzeptiert() {
        return agbAkzeptiert;
    }
    public void setAgbAkzeptiert(boolean agbAkzeptiert) {
        this.agbAkzeptiert = agbAkzeptiert;
    }
    public List<Long> getBestellungenIds() {
        return bestellungenIds;
    }
    public void setBestellungenIds(List<Long> bestellungenIds) {
        this.bestellungenIds = bestellungenIds;
    }
    public String getBemerkungen() {
        return bemerkungen;
    }
    public void setBemerkungen(String bemerkungen) {
        this.bemerkungen = bemerkungen;
    }
    public FamilienstandType getFamilienstand() {
        return familienstand;
    }
    public void setFamilienstand(FamilienstandType familienstand) {
        this.familienstand = familienstand;
    }
    public GeschlechtType getGeschlecht() {
        return geschlecht;
    }
    public void setGeschlecht(GeschlechtType geschlecht) {
        this.geschlecht = geschlecht;
    }
    public Set<HobbyType> getHobbys() {
        return hobbys;
    }
    public void setHobbys(Set<HobbyType> hobbys) {
        this.hobbys = hobbys;
    }

    @Override
    public int hashCode() {
        return 29 * HASH_PRIME + (int) (this.id ^ (this.id >>> 32));   //NOSONAR
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PrivatkundeVO other = (PrivatkundeVO) obj;
        return id == other.id;
    }

    
    @Override
    public String toString() {
        return "KundeVO {id=" + id + ", version=" + version + ", loginname=" + loginname
                + ", kategorie=" + kategorie + ", rabatt=" + rabatt
                + ", umsatz=" + umsatz + ", seit=" + seit + ", agbAkzeptiert=" + agbAkzeptiert
                + ", bestellungenIds=" + bestellungenIds
                + ", bemerkungen=" + bemerkungen + ", familienstand="
                + familienstand + ", geschlecht=" + geschlecht + ", hobbys="
                + hobbys + '}';
    }
}
