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

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.ws.rs.FormParam;

import static de.shop.util.Constants.ERSTE_VERSION;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@MappedSuperclass
public abstract class AbstractVersionedAuditable extends AbstractAuditable implements Cloneable {
    private static final long serialVersionUID = -239222312857331551L;
    
    @Version
    @FormParam("version")
    private int version = ERSTE_VERSION;

    public int getVersion() {
        return version;
    }
    
    /**
     * Fuer die Uebernahme von Werten aus einem PUT-Request in das aus der DB gelesene Objekt
     * @param newValues das Objekt aus dem PUT-Request
     */
    public void setValues(AbstractVersionedAuditable newValues) {
        version = newValues.version;        
    }
    
    // Workaround: In KundeService.update() ist "aus heiterem Himmel" die Versionsnummer erhoeht...
    public void decrVersion() {
        version--;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        final AbstractVersionedAuditable neuesObjekt = (AbstractVersionedAuditable) super.clone();
        neuesObjekt.version = getVersion();
        return neuesObjekt;
    }

    @Override
    public String toString() {
        return "AbstractVersionedAuditable {version=" + version + ", " + super.toString() + '}';
    }
}
