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

package de.shop.util.persistence;

import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Entity
@Table(name = "zz_unused_iam")
@NamedNativeQueries({
    @NamedNativeQuery(name = StringIam.FIND_NACHNAMEN_BY_PREFIX,
                      query = "SELECT   DISTINCT lastname"
                              + " FROM  accounttypeentity"
                              + " WHERE UPPER(lastname) LIKE UPPER(:" + StringIam.PARAM_NACHNAME_PREFIX + ')'
                              + " ORDER BY lastname",
                      resultSetMapping = "LastnameResults"),
    @NamedNativeQuery(name = StringIam.FIND_LOGINNAMEN_BY_PREFIX,
                      query = "SELECT   DISTINCT loginname"
                              + " FROM  accounttypeentity"
                              + " WHERE UPPER(loginname) LIKE UPPER(:" + StringIam.PARAM_LOGINNAME_PREFIX + ')'
                              + " ORDER BY loginname",
                      resultSetMapping = "LoginnameResults"),
    @NamedNativeQuery(name = StringIam.FIND_EMAIL,
                      query = "SELECT   DISTINCT email"
                              + " FROM  accounttypeentity"
                              + " WHERE loginname = :" + StringIam.PARAM_LOGINNAME,
                      resultSetMapping = "EmailResults")
})
@SqlResultSetMappings({
    @SqlResultSetMapping(name = "LastnameResults",
                         entities = @EntityResult(entityClass = StringIam.class,
                                                  fields = @FieldResult(name = "val", column = "lastname"))),
    @SqlResultSetMapping(name = "LoginnameResults",
                         entities = @EntityResult(entityClass = StringIam.class,
                                                  fields = @FieldResult(name = "val", column = "loginname"))),
    @SqlResultSetMapping(name = "EmailResults",
                         entities = @EntityResult(entityClass = StringIam.class,
                                                  fields = @FieldResult(name = "val", column = "email")))
})
public class StringIam {
    private static final String PREFIX = "StringIam.";
    public static final String FIND_NACHNAMEN_BY_PREFIX = PREFIX + "findNachnamenByPrefix";
    public static final String FIND_LOGINNAMEN_BY_PREFIX = PREFIX + "findLoginnamenByPrefix";
    public static final String FIND_EMAIL = PREFIX + "findEmail";
    public static final String PARAM_NACHNAME_PREFIX = "nachname";
    public static final String PARAM_LOGINNAME_PREFIX = "loginname";
    public static final String PARAM_LOGINNAME = "loginname";

    @Id
    // "value" ist ein reserviertes Schluesselwort in SQL
    private String val;

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "StringIam {" + "val=" + val + '}';
    }
}
