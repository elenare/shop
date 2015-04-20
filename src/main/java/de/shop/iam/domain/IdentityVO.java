/*
 * Copyright (C) 2014 Juergen Zimmermann, Hochschule Karlsruhe
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

package de.shop.iam.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wordnik.swagger.annotations.ApiModelProperty;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlTransient;
import org.jboss.resteasy.annotations.Form;
import org.picketlink.idm.model.basic.User;

import static de.shop.util.Constants.HASH_PRIME;
import static java.util.logging.Level.FINER;


/**
 * ValueObject fuer die Daten im Identity and Access Management. Durch das ValueObject werden
 * Annotationen fuer Bean Validation und fuer das JSON-Mapping moeglich.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class IdentityVO {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private static final int LOGINNAME_LENGTH_MAX = 32;
    private static final int PASSWORD_LENGTH_MAX = 88;
    private static final int VORNAME_LENGTH_MAX = 32;
    public static final int NACHNAME_LENGTH_MAX = 32;
    private static final String NAME_PATTERN = "[A-Z\u00C4\u00D6\u00DC][a-z\u00E4\u00F6\u00FC\u00DF]+";
    private static final String PREFIX_ADEL = "(o'|von|von der|von und zu|van)?";
    public static final String NACHNAME_PATTERN = PREFIX_ADEL + NAME_PATTERN + "(-" + NAME_PATTERN + ")?";
    private static final int EMAIL_LENGTH_MAX = 64;
    public static final String EMAIL_PATTERN = "[\\w.%-]+@[\\w.%-]+\\.[A-Za-z]{2,4}";

    @Column(nullable = false, unique = true)
    @Size(max = LOGINNAME_LENGTH_MAX, message = "{identity.loginname.length}")
    @FormParam(value = "loginname")
    private String loginname;

    @FormParam(value = "enabled")
    private boolean enabled = true;
    
    @FormParam(value = "expirationDate")
    private Date expirationDate;
    
    @Size(max = PASSWORD_LENGTH_MAX, message = "{password.length}")
    @FormParam(value = "password")
    private String password;
    
    @FormParam(value = "passwordWdh")
    private String passwordWdh;
    
    @Size(max = NACHNAME_LENGTH_MAX, message = "{identity.nachname.length}")
    @Pattern(regexp = NACHNAME_PATTERN, message = "{identity.nachname.pattern}")
    @FormParam(value = "nachname")
    private String nachname;

    @Size(max = VORNAME_LENGTH_MAX, message = "{identity.vorname.length}")
    @FormParam(value = "vorname")
    private String vorname;

    @Size(max = EMAIL_LENGTH_MAX, message = "{identity.email.length}")
    @Pattern(regexp = EMAIL_PATTERN, message = "{identity.email}")
    @FormParam(value = "email")
    private String email;
    
    @Valid
    @NotNull(message = "{identity.adresse.notNull}")
    @Form
    @ApiModelProperty(required = true)
    private Adresse adresse;
    
    public IdentityVO() {
        super();
    }

    public IdentityVO(User user) {
        super();
        loginname = user.getLoginName();
        enabled = user.isEnabled();
        expirationDate = user.getExpirationDate();
        nachname = user.getLastName();
        vorname = user.getFirstName();
        email = user.getEmail();
        
        adresse = new Adresse(user);
    }
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public IdentityVO(IdentityVO identity) {
        super();
        adresse = new Adresse();
        setValues(identity);
    }

    @AssertTrue(message = "{identity.password.notEqual}")
    @XmlTransient
    @JsonIgnore
    public boolean isPasswortEqual() {
        return Objects.equals(password, passwordWdh);
    }
    
    /**
     * Werte aus einem anderen Objekt uebernehmen
     * @param newValues neue Werte aus einem anderen Objekt
     */
    public void setValues(IdentityVO newValues) {
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("Original-User VOR setValues: " + this);
            LOGGER.finer("Zu setzende Werte VOR setValues: " + newValues);
        }
        
        enabled = newValues.enabled;
        expirationDate = newValues.expirationDate;
        password = newValues.password;
        passwordWdh = newValues.passwordWdh;
        loginname = newValues.loginname;
        vorname = newValues.vorname;
        nachname = newValues.nachname;
        email = newValues.email;
        
        if (adresse != null) {
            adresse.setValues(newValues.getAdresse());
        }
        
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("User NACH setValues: " + this);
        }
    }
    
    public User toUser() {
        final User user = new User();
        updateUser(user);
        return user;
    }
    
    public boolean equalsUser(User user) {
        if (!Objects.equals(loginname, user.getLoginName())) {
            return false;
        }
        if (!Objects.equals(enabled, user.isEnabled())) {
            return false;
        }
        if (!Objects.equals(expirationDate, user.getExpirationDate())) {
            return false;
        }
        if (!Objects.equals(nachname, user.getLastName())) {
            return false;
        }
        if (!Objects.equals(vorname, user.getFirstName())) {
            return false;
        }
        return Objects.equals(email, user.getEmail());
    }
    
    public void updateUser(User user) {
        user.setLoginName(loginname);
        user.setEnabled(enabled);
        user.setExpirationDate(expirationDate);
        user.setLastName(nachname);
        user.setFirstName(vorname);
        user.setEmail(email);
        
        if (adresse != null) {
            adresse.updateUser(user);
        }
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

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }

    @Override
    public int hashCode() {
        final int prime = HASH_PRIME;
        return prime + Objects.hashCode(this.loginname);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IdentityVO other = (IdentityVO) obj;
        return Objects.equals(this.loginname, other.loginname);
    }

    @Override
    public String toString() {
        return "IdentityVO {loginname=" + loginname
               + ", enabled=" + enabled + ", expirationDate=" + expirationDate
               + ", password=" + password + ", passwordWdh=" + passwordWdh
               + ", vorname=" + vorname + ", nachname=" + nachname + ", email=" + email + '}';
    }
}
