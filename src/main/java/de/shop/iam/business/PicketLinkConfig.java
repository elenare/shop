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

import de.shop.util.interceptor.Log;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.jpa.model.sample.simple.AccountTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.AttributeTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.GroupTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.IdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PartitionTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PasswordCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipIdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RoleTypeEntity;
import org.picketlink.idm.model.Relationship;
import org.picketlink.internal.EEJPAContextInitializer;


/**
 * Konfiguration von PicketLink mit einer DB als Speichermedium (default: Datei).
 * In Anlehnung an die Klasse IDMConfiguration aus dem PicketLink Quickstart "picketlink-authorization-idm-jpa" von Shane Bryzak.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@ApplicationScoped
@Log
public class PicketLinkConfig {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private static final Class<?>[] ENTITY_CLASSES = { AccountTypeEntity.class,
                                                       RoleTypeEntity.class,
                                                       GroupTypeEntity.class,
                                                       IdentityTypeEntity.class,
                                                       RelationshipTypeEntity.class,
                                                       RelationshipIdentityTypeEntity.class,
                                                       PartitionTypeEntity.class,
                                                       PasswordCredentialTypeEntity.class,
                                                       AttributeTypeEntity.class };
    
    private final EEJPAContextInitializer contextInitializer;
    //private final EntityManagerContextInitializer contextInitializer;      //NOSONAR

    @Produces
    private IdentityConfiguration identityConfig;
    
    @Inject
    PicketLinkConfig(EEJPAContextInitializer contextInitializer) {
        super();
        this.contextInitializer = contextInitializer;
    }

    @PostConstruct
    @SuppressWarnings("unchecked")
    private void postConstruct() {
        LOGGER.info("Creating IdentityConfiguration ...");
        
        final IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        identityConfig = builder.named("default")
                                .stores()
                                .jpa()
                                .mappedEntity(ENTITY_CLASSES)
                                .supportGlobalRelationship(Relationship.class)
                                .addContextInitializer(contextInitializer)
                                .supportAllFeatures()
                                .build();

        //SONAR_COMMENTED_OUT_BEGIN
//        identityConfig = builder.named("default")
//                                .stores()
//                                .jpa()
//                                .supportAllFeatures()
//                                .build();
        //SONAR_COMMENTED_OUT_END
        
        LOGGER.info("IdentityConfiguration is created");
    }
}
