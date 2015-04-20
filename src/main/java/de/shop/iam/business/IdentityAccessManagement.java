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

package de.shop.iam.business;

import de.shop.iam.domain.IdentityVO;
import de.shop.iam.domain.RolleType;
import de.shop.util.Strings;
import de.shop.util.interceptor.Log;
import de.shop.util.persistence.StringIam;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;
import org.picketlink.Identity;
import org.picketlink.annotations.PicketLink;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.RelationshipQuery;

import static de.shop.iam.domain.RolleType.ADMIN;
import static de.shop.iam.domain.RolleType.ADMIN_STRING;
import static de.shop.iam.domain.RolleType.KUNDE;
import static de.shop.iam.domain.RolleType.MITARBEITER;
import static de.shop.iam.domain.RolleType.MITARBEITER_STRING;
import static de.shop.kundenverwaltung.domain.AbstractKunde.PARAM_LOGINNAME;
import static de.shop.util.Constants.MAX_AUTOCOMPLETE;
import static de.shop.util.Strings.isNullOrEmpty;
import static de.shop.util.persistence.StringIam.FIND_EMAIL;
import static de.shop.util.persistence.StringIam.FIND_LOGINNAMEN_BY_PREFIX;
import static de.shop.util.persistence.StringIam.FIND_NACHNAMEN_BY_PREFIX;
import static de.shop.util.persistence.StringIam.PARAM_LOGINNAME_PREFIX;
import static de.shop.util.persistence.StringIam.PARAM_NACHNAME_PREFIX;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.logging.Level.FINEST;
import static java.util.stream.Collectors.toList;
import static org.picketlink.idm.model.basic.BasicModel.getRole;
import static org.picketlink.idm.model.basic.BasicModel.getUser;
import static org.picketlink.idm.model.basic.BasicModel.grantRole;
import static org.picketlink.idm.model.basic.BasicModel.revokeRole;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Dependent
@Log
public class IdentityAccessManagement {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private static final SecurityViolation SEC_VIOLATION_NOT_ADMIN_MITARBEITER = () -> "Nicht in der Rolle " + ADMIN_STRING
                                                                                       + " oder " + MITARBEITER_STRING + " eingeloggt";
    private static final Set<SecurityViolation> ERR_NOT_ADMIN_MITARBEITER = Collections.singleton(SEC_VIOLATION_NOT_ADMIN_MITARBEITER);
    
    private static final SecurityViolation SEC_VIOLATION_NOT_LOGGED_IN = () -> "Nicht eingeloggt";
    private static final Set<SecurityViolation> ERR_NOT_LOGGED_IN = Collections.singleton(SEC_VIOLATION_NOT_LOGGED_IN);
    
    private static final SecurityViolation SEC_VIOLATION_DIFFERENT_USER = () -> "Eigener Loginname ist nicht der Loginname des gesuchten Users";
    private static final Set<SecurityViolation> ERR_DIFFERENT_USER = Collections.singleton(SEC_VIOLATION_DIFFERENT_USER);

    private static final String EINGELOGGT_IN_DER_ROLLE = "Eingeloggt in der Rolle: ";

    private static final Map<RolleType, Role> ROLLEN = new EnumMap<>(RolleType.class);

    
    IdentityManager identityManager;
    RelationshipManager relationshipManager;
    Identity identity;
    
    // FIXME ab PicketLink 2.7.0 enthaelt @PicketLink auch das Target PARAMETER und kann bei "Constructor Injection" genutzt werden
    @Inject
    @PicketLink
    EntityManager em;

    @Inject
    IdentityAccessManagement(IdentityManager identityManager, RelationshipManager relationshipManager, Identity identity) {
        super();
        this.identityManager = identityManager;
        this.relationshipManager = relationshipManager;
        this.identity = identity;
    }

    @PostConstruct
    private void initRollen() {
        if (!ROLLEN.isEmpty()) {
            return;
        }
        
        Arrays.stream(RolleType.values())
              .forEach(r -> {
                  Role role = getRole(identityManager, r.getValue());
                  ROLLEN.put(r, role);
              });
    }
    
    /**
     * Zu einem Loginnamen den passenden User ermitteln
     * @param loginname Loginname
     * @return Identity zum Loginnamen oder null
     */
    public Optional<IdentityVO> findIdentityByLoginname(String loginname) {
        if (isNullOrEmpty(loginname)) {
            return empty();
        }
        
        final User user = getUser(identityManager, loginname);
        return user == null ? empty() : of(new IdentityVO(user));
    }
    
    
    /**
     * Suche nach Loginnamen mit dem gleichen Praefix
     * @param loginnamePrefix der gemeinsame Praefix fuer die potenziellen Loginnamen 
     * @return Liste der Loginnamen mit gleichem Praefix
     */
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public List<String> findLoginnamenByPrefix(String loginnamePrefix) {
        return em.createNamedQuery(FIND_LOGINNAMEN_BY_PREFIX, StringIam.class)
                 .setParameter(PARAM_LOGINNAME_PREFIX, loginnamePrefix + '%')
                 .setMaxResults(MAX_AUTOCOMPLETE)
                 .getResultList()
                 .stream()
                 .map(StringIam::getVal)
                 .collect(toList());
    }
    
    /**
     * Suche nach Emailadressen mit dem gleichen Loginnamen
     * @param loginname der Loginnamen 
     * @return Liste der Emailadressen
     */
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public Optional<List<String>> findEmailByLoginname(String loginname) {
        final List<String> email = em.createNamedQuery(FIND_EMAIL, StringIam.class)
                                     .setParameter(PARAM_LOGINNAME, loginname)
                                     .setMaxResults(MAX_AUTOCOMPLETE)
                                     .getResultList()
                                     .stream()
                                     .map(StringIam::getVal)
                                     .collect(toList());
        return email.isEmpty() ? empty() : of(email);
    }
    
    /**
     * Zu einem Nachnamen die passenden User ermitteln
     * @param nachname Nachname
     * @return Liste der Identities zum Nachnamen oder leere Liste
     */
    public Optional<List<IdentityVO>> findIdentitiesByNachname(String nachname) {
        if (Strings.isNullOrEmpty(nachname)) {
            return empty();
        }
        
        // TODO Migration PicketLink 2.6.x -> 2.7.0
        //final IdentityQueryBuilder builder = identityManager.getQueryBuilder();
        //final Condition condition = builder.equal(User.LAST_NAME, nachname);
        //final List<IdentityVO> identities = builder.createIdentityQuery(User.class)
        //                                           .where(condition)
        final List<IdentityVO> identities = identityManager.createIdentityQuery(User.class)
                                                           .setParameter(User.LAST_NAME, nachname)
                                                           .getResultList()
                                                           .stream()
                                                           .map(user -> new IdentityVO(user))
                                                           .collect(toList());
        return identities.isEmpty() ? empty() : of(identities);
    }
    
    /**
     * Suche nach Nachnamen mit dem gleichen Praefix
     * @param nachnamePrefix der gemeinsame Praefix fuer die potenziellen Nachnamen 
     * @return Liste der Nachnamen mit gleichem Praefix
     */
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public List<String> findNachnamenByPrefix(String nachnamePrefix) {
        return em.createNamedQuery(FIND_NACHNAMEN_BY_PREFIX, StringIam.class)
                 .setParameter(PARAM_NACHNAME_PREFIX, nachnamePrefix + '%')
                 .setMaxResults(MAX_AUTOCOMPLETE)
                 .getResultList()
                 .stream()
                 .map(StringIam::getVal)
                 .collect(toList());
    }
    
    /**
     * Zu einem Nachnamen die passenden Loginnamen ermitteln
     * @param nachname Nachname
     * @return Liste der Loginnamen oder leere Liste
     */
    public Optional<List<String>> findLoginnamenByNachname(String nachname) {
        if (Strings.isNullOrEmpty(nachname)) {
            return empty();
        }
        
        // TODO Migration PicketLink 2.6.x -> 2.7.0
        //final IdentityQueryBuilder builder = identityManager.getQueryBuilder();
        //final Condition condition = builder.equal(User.LAST_NAME, nachname);
        //final List<String> loginnamen = builder.createIdentityQuery(User.class)
        //                                       .where(condition)
        final List<String> loginnamen = identityManager.createIdentityQuery(User.class)
                                                       .setParameter(User.LAST_NAME, nachname)
                                                       .getResultList()
                                                       .stream()
                                                       .map(User::getLoginName)
                                                       .collect(toList());
        return loginnamen.isEmpty() ? empty() : of(loginnamen);
    }
    
    /**
     * Zu einer Email die passenden User ermitteln
     * @param email Email
     * @return Liste der Identities zur Email oder leere Liste
     */
    public Optional<List<IdentityVO>> findIdentitiesByEmail(String email) {
        if (Strings.isNullOrEmpty(email)) {
            return empty();
        }
        
        // TODO Migration PicketLink 2.6.x -> 2.7.0
        //final IdentityQueryBuilder builder = identityManager.getQueryBuilder();
        //final Condition condition = builder.equal(User.EMAIL, email);
        //final List<IdentityVO> identities = builder.createIdentityQuery(User.class)
        //                                           .where(condition)
        final List<IdentityVO> identities = identityManager.createIdentityQuery(User.class)
                                                           .setParameter(User.EMAIL, email)
                                                           .getResultList()
                                                           .stream()
                                                           .map(u -> new IdentityVO(u))
                                                           .collect(toList());
        return identities.isEmpty() ? empty() : of(identities);
    }
    
    public Optional<String> getLoginname() {
        if (!identity.isLoggedIn()) {
            return empty();
        }
        final User user = (User) identity.getAccount();
        return of(user.getLoginName());
    }
    
    public IdentityVO createIdentity(IdentityVO identity) {
        identity.setEnabled(true);
        final User user = identity.toUser();
        identityManager.add(user);

        final String password = identity.getPassword();
        if (!Strings.isNullOrEmpty(password)) {
            identityManager.updateCredential(user, new Password(password));
        }

        return identity;
    }

    public void updateIdentity(IdentityVO identity) {
        final User user = getUser(identityManager, identity.getLoginname());
        if (!identity.equalsUser(user)) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.finest("Das User-Objekt muss aktualisiert werden");
            }
            identity.updateUser(user);
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.finest("Der IdentityManager fuehrt mit dem aktualisierten User-Objekt ein Update durch");
            }
            identityManager.update(user);
        }
    }
    
    public void removeIdentity(IdentityVO identity) {
        final IdentityType user = getUser(identityManager, identity.getLoginname());
        identityManager.remove(user);
    }
    
    public Optional<List<RolleType>> findRollen(String loginname) {
        if (!identity.isLoggedIn()) {
            return empty();
        }
        
        final User user = getUser(identityManager, loginname);
        if (user == null) {
            return empty();
        }
        
        final RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);
        query.setParameter(Grant.ASSIGNEE, user);
        final List<RolleType> rollen = query.getResultList()
                                            .stream()
                                            .map(grant -> grant.getRole().getName())
                                            .map(RolleType::build)
                                            .collect(toList());
        
        return rollen.isEmpty() ? empty() : of(rollen);
    }
    
    public boolean hasRole(IdentityType identityType, String rolleStr) {
        final Role role = getRole(identityManager, rolleStr);
        return hasRole(identityType, role);
    }

    private boolean hasRole(IdentityType identityType, Role role) {
        if (identityType == null) {
            return false;
        }
        return BasicModel.hasRole(relationshipManager, identityType, role);
    }
    
    /**
     * Zu einem User eine neue Rolle hinzufuegen
     * @param identity Eingeloggter User
     * @param rolle Neue Rolle
     */
    public void grant(IdentityVO identity, RolleType rolle) {
        if (identity == null) {
            return;
        }

        final IdentityType user = getUser(identityManager, identity.getLoginname());
        grant(user, rolle);
    }
    
    private void grant(IdentityType identityType, RolleType rolle) {
        final Role newRole = getRole(identityManager, rolle.getValue());
        grantRole(relationshipManager, identityType, newRole);
    }
    
    /**
     * Zu einem User neue Rollen hinzufuegen
     * @param identity Eingeloggter User
     * @param rollen Neue Rollen
     */
    public void grant(IdentityVO identity, Collection<RolleType> rollen) {
        if (identity == null || rollen == null || rollen.isEmpty()) {
            return;
        }
        
        final IdentityType identityType = getUser(identityManager, identity.getLoginname());
        rollen.forEach(r -> grant(identityType, r));
    }

    /**
     * Von einem User Rollen wegnehmen
     * @param identity Eingeloggter User
     * @param rollen Die wegzunehmenden Rollen
     */
    public void revoke(IdentityVO identity, Collection<RolleType> rollen) {
        if (identity == null || rollen == null || rollen.isEmpty()) {
            return;
        }

        final IdentityType identityType = getUser(identityManager, identity.getLoginname());
        
        rollen.forEach(r -> {
            final Role role = getRole(identityManager, r.getValue());
            revokeRole(relationshipManager, identityType, role);
        });
    }

    
    public void checkSameIdentity(String loginname) {
        // Falls nur die Rolle "kunde" vorhanden ist, dann darf nur nach dem eigenen Loginnamen gesucht werden
        
        if (!identity.isLoggedIn()) {
            throw new AccessDeniedException(ERR_NOT_LOGGED_IN);
        }

        final IdentityType identityType = identity.getAccount();
        final User user = (User) identityType;
        final String currentLoginname = user.getLoginName();
        
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Eingeloggt mit dem Loginnamen: " + currentLoginname);
        }
        
        if (hasRole(identityType, ROLLEN.get(ADMIN))) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.finest(EINGELOGGT_IN_DER_ROLLE + ADMIN.getValue());
            }
            return;
        }
        if (hasRole(identityType, ROLLEN.get(MITARBEITER))) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.finest(EINGELOGGT_IN_DER_ROLLE + MITARBEITER.getValue());
            }
            return;
        }

        // Weder die Rolle "admin" noch die Rolle "mitarbeiter"
        if (hasRole(identityType, ROLLEN.get(KUNDE)) && Objects.equals(currentLoginname, loginname)) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.finest("Gleiche Identity");
            }
            return;
        }
       
        LOGGER.info("Versuchter Zugriff auf Loginname " + loginname + " durch den Loginnamen " + currentLoginname);
        throw new AccessDeniedException(ERR_DIFFERENT_USER);
    }
    
    public void checkAdminMitarbeiter() {
        if (!identity.isLoggedIn()) {
            throw new AccessDeniedException(ERR_NOT_ADMIN_MITARBEITER);
        }
        
        final IdentityType identityType = identity.getAccount();

        if (hasRole(identityType, ROLLEN.get(ADMIN))) {
            return;
        }
        if (hasRole(identityType, ROLLEN.get(MITARBEITER))) {
            return;
        }
        
        throw new AccessDeniedException(ERR_NOT_ADMIN_MITARBEITER);
    }

    /**
     * Rollen zum eingeloggten User ermitteln
     * @return Collection der Rollen des eingeloggten Users
     */
    public Optional<List<RolleType>> eigeneRollen() {
        if (!identity.isLoggedIn()) {
            return empty();
        }
        
        final RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);
        query.setParameter(Grant.ASSIGNEE, identity.getAccount());
        final List<RolleType> rollen = query.getResultList()
                                            .stream()
                                            .map(grant -> grant.getRole().getName())
                                            .map(RolleType::build)
                                            .collect(toList());
        
        return rollen.isEmpty() ? empty() : of(rollen);
    }
    
    public void logout() {
        if (identity == null) {
            identity.logout();
        }
    }
}
