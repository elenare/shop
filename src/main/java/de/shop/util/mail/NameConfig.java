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

package de.shop.util.mail;

import java.io.Serializable;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@ApplicationScoped
public class NameConfig implements Serializable {
    private static final long serialVersionUID = 3916523726340426731L;
    
    // In src\webapp\WEB-INF\web.xml koennen die einzelnen Werte gesetzt bzw. ueberschrieben werden

    @Resource(name = "absenderEmail")
    @Produces
    @AbsenderEmail
    private static String absenderEmail;
    
    @Resource(name = "absenderName")
    @Produces
    @AbsenderName
    private static String absenderName;
    
    @Resource(name = "empfaengerEmail")
    @Produces
    @EmpfaengerEmail
    private static String empfaengerEmail;
    
    @Resource(name = "empfaengerName")
    @Produces
    @EmpfaengerName
    private static String empfaengerName;
    
    NameConfig() {
        super();
    }
}
