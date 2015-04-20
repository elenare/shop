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
package de.shop.util;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ByteArrrayAssert extends AbstractAssert<ByteArrrayAssert, byte[]> {
    protected ByteArrrayAssert(byte[] actual) {
        super(actual, ByteArrrayAssert.class);
    }
    
    public static ByteArrrayAssert assertThatByteArrray(byte[] actual) {
        return new ByteArrrayAssert(actual);
    }

    public ByteArrrayAssert hasEqualLength(byte[] other) {
        assertThat(actual.length)
            .overridingErrorMessage("Die beiden Byte-Arrays haben unterschiedliche Laenge")
            .isEqualTo(other.length);
        return this;
    }

    public ByteArrrayAssert isEqualTo(byte[] other) {
        assertThat(actual)
            .overridingErrorMessage("Die beiden Byte-Arrays stimmen nicht ueberein")
            .isEqualTo(other);
        return this;
    }
}
