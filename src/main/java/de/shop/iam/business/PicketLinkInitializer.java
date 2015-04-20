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

import de.shop.iam.domain.GruppeType;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.picketlink.annotations.PicketLink;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

import static de.shop.iam.domain.Adresse.ATTR_HAUSNR;
import static de.shop.iam.domain.Adresse.ATTR_ORT;
import static de.shop.iam.domain.Adresse.ATTR_PLZ;
import static de.shop.iam.domain.Adresse.ATTR_STRASSE;
import static de.shop.iam.domain.GruppeType.APAC;
import static de.shop.iam.domain.GruppeType.EMEA;
import static de.shop.iam.domain.GruppeType.FRANCE;
import static de.shop.iam.domain.GruppeType.GERMANY;
import static de.shop.iam.domain.GruppeType.MIDDLE;
import static de.shop.iam.domain.GruppeType.NORTH;
import static de.shop.iam.domain.GruppeType.NORTHAMERICA;
import static de.shop.iam.domain.GruppeType.NORTHERN_EUROPE;
import static de.shop.iam.domain.GruppeType.SALES;
import static de.shop.iam.domain.GruppeType.SEAME;
import static de.shop.iam.domain.GruppeType.SOUTH;
import static de.shop.iam.domain.GruppeType.UK;
import static de.shop.iam.domain.RolleType.ABTEILUNGSLEITER;
import static de.shop.iam.domain.RolleType.ADMIN;
import static de.shop.iam.domain.RolleType.KUNDE;
import static de.shop.iam.domain.RolleType.MITARBEITER;
import static org.picketlink.idm.model.basic.BasicModel.getRole;
import static org.picketlink.idm.model.basic.BasicModel.grantRole;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
//TODO CDI-faehiges Bean mit irgendeinem Scope sowie @Transactional funktioniert nicht (warum?)
@Singleton
@Startup
public class PicketLinkInitializer {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final int MAX_RANDOM_EXCLUSIVE = 100;
    private static final int EFFECTIVE_DATE_NEXT_WEEK = 7;
    
    @Inject
    private PartitionManager partitionManager;
    
    @Inject
    // @PicketLink ist nur fuer Attribute anwendbar, nicht bei Constructor Injection
    @PicketLink
    private EntityManager em;                                          //NOSONAR
    
    @PostConstruct
    public void create() {
        LOGGER.info("Creating groups, roles and users ...");

        final IdentityManager identityMgr = partitionManager.createIdentityManager();
        createGroups(identityMgr);
        createRoles(identityMgr);
        
        final RelationshipManager relationshipMgr = partitionManager.createRelationshipManager();
        createUsers(identityMgr, relationshipMgr);
        
        LOGGER.info("Groups, roles and users created");
    }
    
    //--------------------------------------------------------------------------
    // G r u p p e n   a n l e g e n
    //--------------------------------------------------------------------------
    private void createGroups(IdentityManager identityMgr) {
        final Group sales = createGroup(SALES, identityMgr);
        
        // Pfad: /Sales/Northamerica
        createGroup(NORTHAMERICA, sales, identityMgr);
        final Group salesEMEA = createGroup(EMEA, sales, identityMgr);
        createGroup(APAC, sales, identityMgr);
        
        final Group salesGermany = createGroup(GERMANY, salesEMEA, identityMgr);
        createGroup(UK, salesEMEA, identityMgr);
        createGroup(FRANCE, salesEMEA, identityMgr);
        createGroup(NORTHERN_EUROPE, salesEMEA, identityMgr);
        createGroup(SEAME, salesEMEA, identityMgr);
        
        // Pfad: /Sales/EMEA/Germany/North
        createGroup(NORTH, salesGermany, identityMgr);
        createGroup(MIDDLE, salesGermany, identityMgr);
        createGroup(SOUTH, salesGermany, identityMgr);
    }
    
    private Group createGroup(GruppeType gruppe, IdentityManager identityMgr) {
        final Group group = new Group(gruppe.getValue());
        identityMgr.add(group);
        return group;
    }

    private Group createGroup(GruppeType gruppe, Group parentGroup, IdentityManager identityMgr) {
        final Group group = new Group(gruppe.getValue(), parentGroup);
        identityMgr.add(group);
        return group;
    }
    
    //--------------------------------------------------------------------------
    // R o l l e n   a n l e g e n
    //--------------------------------------------------------------------------
    private void createRoles(IdentityManager identityMgr) {
        identityMgr.add(new Role(ADMIN.getValue()));
        identityMgr.add(new Role(MITARBEITER.getValue()));
        identityMgr.add(new Role(ABTEILUNGSLEITER.getValue()));
        identityMgr.add(new Role(KUNDE.getValue()));
    }

    
    //--------------------------------------------------------------------------
    // U s e r   a n l e g e n
    //--------------------------------------------------------------------------
    private void createUsers(IdentityManager identityMgr, RelationshipManager relationshipMgr) {
//        final Group salesGermanySouth = getGroup(identityMgr, SALES.getValue() + '/'
//                                                              + EMEA.getValue() + '/'
//                                                              + GERMANY.getValue() + '/'
//                                                              + SOUTH.getValue());   //NOSONAR

        final Role admin = getRole(identityMgr, ADMIN.getValue());
        final Role mitarbeiter = getRole(identityMgr, MITARBEITER.getValue());
        final Role abteilungsleiter = getRole(identityMgr, ABTEILUNGSLEITER.getValue());
        final Role kunde = getRole(identityMgr, KUNDE.getValue());
        
        createUser("admin", "admin@hs-karlsruhe.de", "Admin", null, "p",
                   identityMgr, relationshipMgr, admin, mitarbeiter, abteilungsleiter, kunde);
        createUser("adriana.alpha", "adriana.alpha@hs-karlsruhe.de", "Alpha", "Adriana", "p",   //NOSONAR
                   identityMgr, relationshipMgr, admin, mitarbeiter, kunde);
        createUser("alfred.alpha", "alfred.alpha@hs-karlsruhe.de", "Alpha", "Alfred", "p",
                   identityMgr, relationshipMgr, mitarbeiter, kunde);
        createUser("anton.alpha", "anton.alpha@hs-karlsruhe.de", "Alpha", "Anton", "p",
                   identityMgr, relationshipMgr, mitarbeiter, kunde);
        createUser("dirk.delta", "dirk.delta@hs-karlsruhe.de", "Delta", "Dirk", "p",
                   identityMgr, relationshipMgr, kunde);
        createUser("emil.epsilon", "emil.epsilon@hs-karlsruhe.de", "Epsilon", "Emil", "p",
                   identityMgr, relationshipMgr);
        
        createUser("na.nu", "na.nu@hs-karlsruhe.de", "Nu", "Na", "x", identityMgr, relationshipMgr);
        createUser("nb.nu", "nb.nu@hs-karlsruhe.de", "Nu", "Nb", "x", identityMgr, relationshipMgr);
        createUser("nc.nu", "nc.nu@hs-karlsruhe.de", "Nu", "Nc", "x", identityMgr, relationshipMgr);
        createUser("nd.nu", "nd.nu@hs-karlsruhe.de", "Nu", "Nd", "x", identityMgr, relationshipMgr);
        createUser("ne.nu", "ne.nu@hs-karlsruhe.de", "Nu", "Ne", "x", identityMgr, relationshipMgr);
        createUser("nf.nu", "nf.nu@hs-karlsruhe.de", "Nu", "Nf", "x", identityMgr, relationshipMgr);
        createUser("ng.nu", "ng.nu@hs-karlsruhe.de", "Nu", "Ng", "x", identityMgr, relationshipMgr);
        createUser("nh.nu", "nh.nu@hs-karlsruhe.de", "Nu", "Nh", "x", identityMgr, relationshipMgr);
        createUser("ni.nu", "ni.nu@hs-karlsruhe.de", "Nu", "Ni", "x", identityMgr, relationshipMgr);
        createUser("nj.nu", "nj.nu@hs-karlsruhe.de", "Nu", "Nj", "x", identityMgr, relationshipMgr);
        createUser("nk.nu", "nk.nu@hs-karlsruhe.de", "Nu", "Nk", "x", identityMgr, relationshipMgr);
        createUser("nl.nu", "nl.nu@hs-karlsruhe.de", "Nu", "Nl", "x", identityMgr, relationshipMgr);
        createUser("nm.nu", "nm.nu@hs-karlsruhe.de", "Nu", "Nm", "x", identityMgr, relationshipMgr);
        createUser("nn.nu", "nn.nu@hs-karlsruhe.de", "Nu", "Nn", "x", identityMgr, relationshipMgr);
        createUser("no.nu", "no.nu@hs-karlsruhe.de", "Nu", "No", "x", identityMgr, relationshipMgr);
        createUser("np.nu", "np.nu@hs-karlsruhe.de", "Nu", "Np", "x", identityMgr, relationshipMgr);
        createUser("nq.nu", "nq.nu@hs-karlsruhe.de", "Nu", "Nq", "x", identityMgr, relationshipMgr);
        
        createUser("oa.omega", "oa.omega@hs-karlsruhe.de", "Omega", "Oa", "x", identityMgr, relationshipMgr);   //NOSONAR
        createUser("ob.omega", "ob.omega@hs-karlsruhe.de", "Omega", "Ob", "x", identityMgr, relationshipMgr);
        createUser("oc.omega", "oc.omega@hs-karlsruhe.de", "Omega", "Oc", "x", identityMgr, relationshipMgr);
        createUser("od.omega", "od.omega@hs-karlsruhe.de", "Omega", "Od", "x", identityMgr, relationshipMgr);
        createUser("oe.omega", "oe.omega@hs-karlsruhe.de", "Omega", "Oe", "x", identityMgr, relationshipMgr);
        createUser("of.omega", "of.omega@hs-karlsruhe.de", "Omega", "Of", "x", identityMgr, relationshipMgr);
        createUser("og.omega", "og.omega@hs-karlsruhe.de", "Omega", "Og", "x", identityMgr, relationshipMgr);
        createUser("oh.omega", "oh.omega@hs-karlsruhe.de", "Omega", "Oh", "x", identityMgr, relationshipMgr);
        createUser("oi.omega", "oi.omega@hs-karlsruhe.de", "Omega", "Oi", "x", identityMgr, relationshipMgr);
        createUser("oj.omega", "oj.omega@hs-karlsruhe.de", "Omega", "Oj", "x", identityMgr, relationshipMgr);
        createUser("ok.omega", "ok.omega@hs-karlsruhe.de", "Omega", "Ok", "x", identityMgr, relationshipMgr);
        createUser("ol.omega", "ol.omega@hs-karlsruhe.de", "Omega", "Ol", "x", identityMgr, relationshipMgr);
        createUser("om.omega", "om.omega@hs-karlsruhe.de", "Omega", "Om", "x", identityMgr, relationshipMgr);
    }
    
    private void createUser(String loginname, String email, String nachname, String vorname, String password,   //NOSONAR
                            IdentityManager identityMgr, RelationshipManager relationshipMgr, Role... rollen) {
        final User user = new User(loginname);
        user.setEmail(email);
        user.setLastName(nachname);
        user.setFirstName(vorname);
        
        // Expiration Date: 1 Monat ab dem aktuellen Datum (in der Zeitzone des Betriebssystems)
        // Der Zeitpunkt wird +1 Stunde bzgl. UTC gerechnet
        // Bei Sommerzeit muesste es +2 sein; bei Testdaten kommt es auf 1 Stunde nicht an ;-)
        final Instant expirationInstant = LocalDateTime.now()
                                                       .plusMonths(1)
                                                       .toInstant(ZoneOffset.ofHours(1));
        final Date expirationDate = Date.from(expirationInstant);
        
        if ("Nu".equals(nachname) || "Omega".equals(nachname)) {
            user.setEnabled(false);
            user.setExpirationDate(expirationDate);
        }
        
        // Adresse setzen
        user.setAttribute(new Attribute<>(ATTR_PLZ, "76133"));
        user.setAttribute(new Attribute<>(ATTR_ORT, "Karlsruhe"));
        user.setAttribute(new Attribute<>(ATTR_STRASSE, "Moltkeweg"));
        final Random random = new Random(new Date().getTime());
        final int hausnr = random.nextInt(MAX_RANDOM_EXCLUSIVE);
        user.setAttribute(new Attribute<>(ATTR_HAUSNR, String.valueOf(hausnr)));
                
        // IdentityManagementException, falls der loginname bereits existiert
        // Doppelte Email-Adressen sind moeglich
        identityMgr.add(user);
        
        // Effective Date: 1 Woche ab dem aktuellen Datum (in der Zeitzone des Betriebssystems)
        // Der Zeitpunkt wird +1 Stunde bzgl. UTC gerechnet
        // Bei Sommerzeit muesste es +2 sein; bei Testdaten kommt es auf 1 Stunde nicht an ;-)
        final Instant effectiveInstant = LocalDateTime.now()
                                                      .plusDays(EFFECTIVE_DATE_NEXT_WEEK)
                                                      .toInstant(ZoneOffset.ofHours(1));
        final Date effectiveDate = Date.from(effectiveInstant);
        if ("Nu".equals(nachname) || "Omega".equals(nachname)) {
            identityMgr.updateCredential(user, new Password(password), effectiveDate, expirationDate);
        } else {
            identityMgr.updateCredential(user, new Password(password));
        }
        
        if (rollen != null && rollen.length != 0) {
            Arrays.stream(rollen)
                  .forEach(r -> grantRole(relationshipMgr, user, r));
        }
    }
}
