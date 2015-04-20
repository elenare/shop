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

package de.shop.util.cdi;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.servlet.ServletContext;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Dependent
public class Initializer {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    Initializer() {
        super();
    }
    
    //@Transactional
    @SuppressWarnings("unused")
    private static void onStartup(@Observes @Initialized(ApplicationScoped.class) ServletContext ctx) {
        LOGGER.info("Der Web-Container " + ctx.getServerInfo() + " unterstuetzt die Servlet-Spezifikation "
                    + ctx.getMajorVersion() + "." + ctx.getMinorVersion());
        
        LOGGER.info("Default Charset: " + Charset.defaultCharset().displayName());
        
        // Eigene Initialisierungen, z.B initiale Daten fuer die DB
    }
}
