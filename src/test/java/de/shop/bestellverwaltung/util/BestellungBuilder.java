/*
 * Copyright (C) 2015 Juergen Zimmermann, Hochschule Karlsruhe
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
package de.shop.bestellverwaltung.util;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static java.math.BigDecimal.ZERO;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class BestellungBuilder {
    private final Bestellung bestellung = new Bestellung();
    
    public BestellungBuilder bestellpositionen(List<Bestellposition> bestellpositionen) {
        bestellung.setBestellpositionen(bestellpositionen);
        return this;
    }
    
    public BestellungBuilder addBestellposition(Artikel artikel, int anzahl) {
        final Bestellposition bp = new BestellpositionBuilder()
                                   .artikel(artikel)
                                   .anzahl(anzahl)
                                   .build();
        bestellung.addBestellposition(bp);
        return this;
    }
    
    public BestellungBuilder addBestellposition(String artikelUri, int anzahl) throws URISyntaxException {
        final Bestellposition bp = new BestellpositionBuilder()
                                   .artikelUri(artikelUri)
                                   .anzahl(anzahl)
                                   .build();
        bestellung.addBestellposition(bp);
        return this;
    }
    
    public Bestellung build() {
        // Dummy, damit @NotNull nicht verletzt wird
        bestellung.setGesamtbetrag(ZERO);
        return bestellung;
    }
    
    private class BestellpositionBuilder {
        private final Bestellposition bestellposition = new Bestellposition();

        public BestellpositionBuilder artikel(Artikel artikel) {
            bestellposition.setArtikel(artikel);
            return this;
        }

        public BestellpositionBuilder artikelUri(String uri) throws URISyntaxException {
            bestellposition.setArtikelUri(new URI(uri));
            return this;
        }

        public BestellpositionBuilder anzahl(int anzahl) {
            bestellposition.setAnzahl(anzahl);
            return this;
        }

        public Bestellposition build() {
            return bestellposition;
        }
    }
}
