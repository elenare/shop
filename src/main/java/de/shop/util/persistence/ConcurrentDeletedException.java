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

import de.shop.util.AbstractShopException;
import javax.ejb.ApplicationException;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@ApplicationException(rollback = true)
public class ConcurrentDeletedException extends AbstractShopException {
    private static final long serialVersionUID = 3061376326827538106L;
    private static final String MESSAGE_KEY = "persistence.concurrentDelete";
    
    private final transient Object id;

    public ConcurrentDeletedException(Object id) {
        super("Das Objekt mit der ID " + id + " wurde konkurrierend geloescht");
        this.id = id;
    }
    
    public Object getId() {
        return id;
    }
    
    @Override
    public String getMessageKey() {
        return MESSAGE_KEY;
    }
}
