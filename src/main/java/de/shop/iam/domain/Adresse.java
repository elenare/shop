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

package de.shop.iam.domain;


import com.wordnik.swagger.annotations.ApiModelProperty;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.logging.Logger;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.FormParam;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.User;

import static de.shop.util.Constants.HASH_PRIME;
import static java.util.logging.Level.FINER;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class Adresse  {
    private static final long serialVersionUID = 4618817696314640065L;
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private static final int PLZ_LENGTH = 5;
    private static final int ORT_LENGTH_MIN = 2;
    private static final int ORT_LENGTH_MAX = 32;
    private static final int STRASSE_LENGTH_MIN = 2;
    private static final int STRASSE_LENGTH_MAX = 32;
    private static final int HAUSNR_LENGTH_MAX = 4;
    
    public static final String ATTR_PLZ = "plz";
    public static final String ATTR_ORT = "ort";
    public static final String ATTR_STRASSE = "strasse";
    public static final String ATTR_HAUSNR = "hausnr";
    
    @NotNull(message = "{adresse.plz.notNull}")
    @Pattern(regexp = "\\d{" + PLZ_LENGTH + '}', message = "{adresse.plz}")
    @FormParam(value = "plz")
    @ApiModelProperty(required = true)
    private String plz;

    @NotNull(message = "{adresse.ort.notNull}")
    @Size(min = ORT_LENGTH_MIN, max = ORT_LENGTH_MAX, message = "{adresse.ort.length}")
    @FormParam(value = "ort")
    @ApiModelProperty(required = true)
    private String ort;

    @NotNull(message = "{adresse.strasse.notNull}")
    @Size(min = STRASSE_LENGTH_MIN, max = STRASSE_LENGTH_MAX, message = "{adresse.strasse.length}")
    @FormParam(value = "strasse")
    @ApiModelProperty(required = true)
    private String strasse;

    @Size(max = HAUSNR_LENGTH_MAX, message = "{adresse.hausnr.length}")
    @FormParam(value = "hausnr")
    private String hausnr;

    public Adresse() {
        super();
    }

    public Adresse(String plz, String ort, String strasse, String hausnr) {
        super();
        this.plz = plz;
        this.ort = ort;
        this.strasse = strasse;
        this.hausnr = hausnr;
    }
    
    public Adresse(User user) {
        Attribute<String> attr = user.getAttribute(ATTR_PLZ);
        plz = attr.getValue();
        
        attr = user.getAttribute(ATTR_ORT);
        ort = attr.getValue();
        
        attr = user.getAttribute(ATTR_STRASSE);
        strasse = attr.getValue();
        
        attr = user.getAttribute(ATTR_HAUSNR);
        hausnr = attr.getValue();
    }
    
    /**
     * Werte aus einem anderen Objekt uebernehmen
     * @param newValues neue Werte aus einem anderen Objekt
     */
    public void setValues(Adresse newValues) {
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("Original-Adresse VOR setValues: " + this);
            LOGGER.finer("Zu setzende Werte VOR setValues: " + newValues);
        }
        
        plz = newValues.plz;
        ort = newValues.ort;
        strasse = newValues.strasse;
        hausnr = newValues.hausnr;
        
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("Adresse NACH setValues: " + this);
        }
    }

    public void updateUser(User user) {
        user.setAttribute(new Attribute<>(ATTR_PLZ, plz));
        user.setAttribute(new Attribute<>(ATTR_ORT, ort));
        user.setAttribute(new Attribute<>(ATTR_STRASSE, strasse));
        user.setAttribute(new Attribute<>(ATTR_HAUSNR, hausnr));
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
    public String toString() {
        return "Adresse {plz=" + plz + ", ort=" + ort + ", strasse=" + strasse
               + ", hausnr=" + hausnr + ", " + super.toString() + '}';
    }

    @Override
    public int hashCode() {
        final int prime = HASH_PRIME;
        int result = prime + Objects.hashCode(plz);
        result = prime * result + Objects.hashCode(ort);
        return prime * result + Objects.hashCode(strasse);
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
        final Adresse other = (Adresse) obj;
        if (!Objects.equals(plz, other.plz)) {
            return false;
        }
        if (!Objects.equals(ort, other.ort)) {
            return false;
        }
        if (!Objects.equals(strasse, other.strasse)) {
            return false;
        }
        return Objects.equals(hausnr, other.hausnr);
    }
}
