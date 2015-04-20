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

import de.shop.iam.domain.Adresse;
import de.shop.iam.domain.IdentityVO;
import java.util.Date;
import java.util.Objects;

import static de.shop.util.Constants.HASH_PRIME;

/**
 * Value Object (VO) fuer die Domain-Klasse Privatkunde
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class IdentitySoapVO {
    private String loginname;
    private boolean enabled;
    private Date expirationDate;
    private String password;
    private String passwordWdh;
    private String nachname;
    private String vorname;
    private String email;
    
    // wsconsume generiert Stub-Klassen nur fuer direkt-referenzierte Klassen, nicht fuer indirekte
    private String plz;
    private String ort;
    private String strasse;
    private String hausnr;
    
    public IdentitySoapVO() {
        super();
    }
    
    public IdentitySoapVO(IdentityVO identity) {
        super();
        loginname = identity.getLoginname();
        enabled = identity.isEnabled();
        expirationDate = identity.getExpirationDate();
        password = identity.getPassword();
        passwordWdh = identity.getPasswordWdh();
        nachname = identity.getNachname();
        vorname = identity.getVorname();
        email = identity.getEmail();
        
        plz = identity.getAdresse().getPlz();
        ort = identity.getAdresse().getOrt();
        strasse = identity.getAdresse().getStrasse();
        hausnr = identity.getAdresse().getHausnr();
    }
    
    public IdentityVO toIdentityVO() {
        final IdentityVO identity = new IdentityVO();
        identity.setLoginname(loginname);
        identity.setEnabled(enabled);
        identity.setExpirationDate(expirationDate);
        identity.setPassword(password);
        identity.setPasswordWdh(passwordWdh);
        identity.setNachname(nachname);
        identity.setVorname(vorname);
        identity.setEmail(email);
        final Adresse adresse = new Adresse(plz, ort, strasse, hausnr);
        identity.setAdresse(adresse);
        return identity;
    }


    public String getLoginname() {
        return loginname;
    }
    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordWdh() {
        return passwordWdh;
    }

    public void setPasswordWdh(String passwordWdh) {
        this.passwordWdh = passwordWdh;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getOrt() {
        return ort;
    }

    public void setOrt(String ort) {
        this.ort = ort;
    }

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getHausnr() {
        return hausnr;
    }

    public void setHausnr(String hausnr) {
        this.hausnr = hausnr;
    }
    
    
    @Override
    public int hashCode() {
        return HASH_PRIME + ((loginname == null) ? 0 : loginname.hashCode());
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
        final IdentitySoapVO other = (IdentitySoapVO) obj;
        return Objects.equals(loginname, other.loginname);
    }
    
    @Override
    public String toString() {
        return "KundeVO {loginname=" + loginname
                + ", enabled=" + enabled  + ", expirationDate=" + expirationDate 
                + ", password=" + password  + ", passwordWdh=" + passwordWdh 
                + ", nachname=" + nachname  + ", vorname=" + vorname   + ", email=" + email + '}';
    }
}
