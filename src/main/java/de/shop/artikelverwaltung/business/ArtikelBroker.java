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

package de.shop.artikelverwaltung.business;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.interceptor.Log;
import de.shop.util.persistence.DefaultEM;
import java.util.Optional;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;



/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Dependent
@Log
public class ArtikelBroker {
    @SuppressWarnings("FieldMayBeFinal")
    private EntityManager em;
    
    @Inject
    ArtikelBroker(@DefaultEM EntityManager em) {
        super();
        this.em = em;
    }
    
    public Optional<Artikel> findById(long id) {
        final Artikel artikel = em.find(Artikel.class, id);
        return Optional.ofNullable(artikel);
    }
    
    /**
     * Einen neuen Artikel anlegen
     * @param artikel Der neue Artikel
     * @return Der neue Artikel einschliesslich generierter ID
     */
    public Artikel save(Artikel artikel) {
        if (artikel == null) {
            return null;
        }
    
        em.persist(artikel);
        return artikel;
    }
    
    /**
     * Einen vorhandenen Artikel aktualisieren
     * @param artikel Der aktualisierte Artikel
     * @return Der aktualisierte Artikel
     */
    public Artikel updateArtikel(Artikel artikel) {
        if (artikel == null) {
            return null;
        }
    
        em.merge(artikel);
        return artikel;
    }
}
