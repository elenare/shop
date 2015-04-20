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

package de.shop.iam.business;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Dependent
public class HttpSecurityConfig {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    //private static final String REST_GROUP = "REST";                 //NOSONAR
    
    HttpSecurityConfig() {
        super();
    }
    
    private void config(/* @Observes SecurityConfigurationEvent event */) {
        LOGGER.info("Configuring HTTP security ...");

        //SONAR_COMMENTED_OUT_BEGIN
//        event.getBuilder()
//             .http()
//             .forPath("/rest/artikel")
//             .authenticateWith()
//             .basic()
//             .realmName("Shop");

//        event.getBuilder()
//             .http()
//             .forPath("/rest/bestellungen")
//             .authorizeWith()
//             .role("kunde");

//             .forGroup(REST_GROUP)
//             .forPath("/rest/artikel", REST_GROUP)
//             .forPath("/rest/artikel/*", REST_GROUP)
//             .forPath("/rest/bestellungen", REST_GROUP)
//             .forPath("/rest/bestellungen/*", REST_GROUP)
//             .forPath("/rest/kunden", REST_GROUP)
//             .forPath("/rest/kunden/*", REST_GROUP)
//             .forPath("/rest/iam/*", REST_GROUP);
        //SONAR_COMMENTED_OUT_END
        
        LOGGER.info("HTTP security is configured");
    }
}
