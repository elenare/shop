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

package de.shop.util;

import java.io.File;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

import static de.shop.util.TestConstants.TEST_WAR;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public enum ArchiveBuilder {
	INSTANCE;

    private static final String COMMONS_FILEUPLOAD_VERSION = "1.3.1";
    private static final String DELTASPIKE_VERSION = "1.3.0";
    
	private final WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_WAR);
    private final WebArchive baseTestArchive = ShrinkWrap.create(WebArchive.class, TEST_WAR);
	// http://exitcondition.alrubinger.com/2012/09/13/shrinkwrap-resolver-new-api
	private final PomEquippedResolveStage pomResolver = Maven.configureResolver()
				                                             .workOffline()
				                                             .loadPomFromFile("pom.xml");
    private File[] assertjJars;
    
    // Funktioniert nicht wegen der Metamodell-Klassen von JPA fuer Criteria-Queries:
    // org.jboss.shrinkwrap.resolver.impl.maven.archive.packaging.AbstractCompilingProcessor.compile()
    //
    //private final WebArchive archive = ShrinkWrap.create(MavenImporter.class, TEST_WAR)
    //                                             .loadPomFromFile("pom.xml")
    //                                             .importBuildOutput()
    //                                             .as(WebArchive.class);
    //private final WebArchive archive = ShrinkWrap.create(MavenImporter.class, TEST_WAR)
    //                                             .loadPomFromFile("pom.xml")
    //                                             .importBuildOutput()
    //                                             .importTestBuildOutput()
    //                                             .as(WebArchive.class);

	private ArchiveBuilder() {
		addWebInfWebseiten();
		addKlassen();
		addJars();
        
        baseTestArchive.merge(archive);
        baseTestArchive.addAsLibraries(pomResolver.resolve("org.mockito:mockito-core")
                                                  .withTransitivity()
                                                  .asFile());
	}
	
	private void addWebInfWebseiten() {
        // https://community.jboss.org/wiki/HowDoIAddAllWebResourcesToTheShrinkWrapArchive
        // https://issues.jboss.org/browse/SHRINKWRAP-247
        
		// XML-Konfigurationsdateien und Webseiten als JAR einlesen
		GenericArchive tmp = ShrinkWrap.create(GenericArchive.class)
		                               .as(ExplodedImporter.class)
		                               .importDirectory("src/main/webapp")
		                               .as(GenericArchive.class);
		archive.merge(tmp, "/");
        
        // minifizierte CSS-Dateien als JAR einlesen
        tmp = ShrinkWrap.create(GenericArchive.class)
				        .as(ExplodedImporter.class)
				        .importDirectory("target/css-primefaces")
				        .as(GenericArchive.class);
        archive.merge(tmp, "/contracts/shop/css/1_0");
        
        // minifizierte JavaScript-Dateien als JAR einlesen
        tmp = ShrinkWrap.create(GenericArchive.class)
				        .as(ExplodedImporter.class)
				        .importDirectory("target/babel/src/main/webapp/contracts/shop/js/1_0")
				        .as(GenericArchive.class);
        archive.merge(tmp, "/contracts/shop/js/1_0");
	}
	
	private void addKlassen() {
		final GenericArchive tmp = ShrinkWrap.create(GenericArchive.class)
				                             .as(ExplodedImporter.class)
				                             .importDirectory("target/classes")
				                             .as(GenericArchive.class);
		archive.merge(tmp, "WEB-INF/classes");
	}
	
	private void addJars() {
		archive.addAsLibraries(pomResolver.resolve("org.picketlink:picketlink-deltaspike",
                                                   "org.apache.deltaspike.modules:deltaspike-security-module-api:" + DELTASPIKE_VERSION,
                                                   "org.apache.deltaspike.modules:deltaspike-security-module-impl:" + DELTASPIKE_VERSION,
                                                   "org.apache.deltaspike.core:deltaspike-core-api:" + DELTASPIKE_VERSION,
                                                   "org.apache.deltaspike.core:deltaspike-core-impl:" + DELTASPIKE_VERSION,
                                                   "org.primefaces:primefaces",
                                                   "org.primefaces.themes:all-themes",
                                                   "org.atmosphere:atmosphere-runtime",
                                                   "com.lowagie:itext",
                                                   "commons-fileupload:commons-fileupload:" + COMMONS_FILEUPLOAD_VERSION)
				                              .withoutTransitivity()
				                              .asFile());
	}
    
	public static ArchiveBuilder getInstance() {
		return INSTANCE;
	}
	
	public Archive<?> getArchive(Class<?>... testklassen) {
        if (testklassen.length == 0) {
            return archive;
        }
        
        if (assertjJars == null) {
            assertjJars = pomResolver.resolve("org.assertj:assertj-core")
                                     .withoutTransitivity()
                                     .asFile();
        }
        
        final WebArchive testArchive = ShrinkWrap.create(WebArchive.class, TEST_WAR);
        testArchive.merge(baseTestArchive);
        testArchive.addClasses(testklassen);
        testArchive.addAsLibraries(assertjJars);
        return testArchive;
	}
}
