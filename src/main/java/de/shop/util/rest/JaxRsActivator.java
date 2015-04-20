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

package de.shop.util.rest;

import com.wordnik.swagger.jaxrs.config.BeanConfig;
import de.shop.util.ShopRuntimeException;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import static de.shop.util.Constants.REST_PATH;
import static java.util.logging.Level.WARNING;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@ApplicationPath(REST_PATH)
public class JaxRsActivator extends Application {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    public JaxRsActivator() {
        // Name der WAR-Datei mittels JNDI ermitteln: "shop" bei "shop.war"
        Context ctx = null;
        String appName;
        try {
            ctx = new InitialContext();
            appName = (String) ctx.lookup("java:app/AppName");
        } catch (NamingException e) {
            throw new ShopRuntimeException(e);
        } finally {
            // Context ist nicht AutoClosable
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    LOGGER.log(WARNING, e.getMessage(), e);
                }
            }
        }
        
        // Die REST-Schnittstelle als JSON-Datensatz fuer Swagger-UI:
        // https://localhost:8443/shop/rest/api-docs
        final String basePath = appName + REST_PATH;
        LOGGER.info("Basepath fuer Swagger: " + basePath);
        
        final BeanConfig beanConfig = new BeanConfig();
        beanConfig.setBasePath(basePath);
        beanConfig.setVersion("1.0");
        beanConfig.setTitle("Webshop");
        beanConfig.setDescription("Das semesterbegleitende Beispiel in Software Engineering mit APIs f\u00FCr REST, JSF/PrimeFaces und SOAP");
        beanConfig.setContact("Juergen.Zimmermann@HS-Karlsruhe.de");
        beanConfig.setLicense("GNU General Public License (GPL)");
        beanConfig.setLicenseUrl("http://www.gnu.org/licenses/gpl-3.0.html");
        // FIXME Aufzaehlung von Packages durch Komma getrennt funktioniert nicht
        beanConfig.setResourcePackage("de.shop");
        beanConfig.setScan(true);
    }
}
