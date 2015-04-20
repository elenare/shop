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

import javax.annotation.Resource;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.mail.Session;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
//@MailSessionDefinition(name = "java:comp/ShopMailSession",
//                       host = "smtp.hs-karlsruhe.de",
//                       transportProtocol = "smtp",
//                       properties = {                                //NOSONAR
//                            "mail.smtp.ssl.enable=false",
//                            "mail.smtp.auth=false",
//                            "mail.debug=true"
//                       })
@Dependent
public class SessionConfig {
    @Resource(lookup = "java:jboss/mail/Default")
    //@Resource(lookup = "java:comp/ShopMailSession")
    @Produces
    private static Session session;
    
    SessionConfig() {
        super();
    }
}
