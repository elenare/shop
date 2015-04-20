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

package de.shop.iam.jsf.primefaces;

import de.shop.iam.business.IdentityAccessManagement;
import de.shop.iam.domain.IdentityVO;
import de.shop.iam.domain.RolleType;
import de.shop.util.interceptor.Log;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.primefaces.model.DualListModel;

import static de.shop.iam.domain.RolleType.ADMIN_STRING;
import static de.shop.iam.domain.RolleType.MITARBEITER_STRING;
import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;
import static java.util.logging.Level.FINEST;
import static java.util.stream.Collectors.toList;


/**
 * Dialogsteuerung f&uuml;r Authentifizierung (Login und Logout) und Authorisierung (rollenbasierte Berechtigungen).
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@ViewScoped
@Transactional
@Log
public class UpdateRollenModel implements Serializable {
    private static final long serialVersionUID = -8604525347843804815L;
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    // Field Injection: IdentityAccessManagement ist nicht serialisierbar,
    // weshalb transient wegen @ViewScoped notwendig ist. Kap. 6.6.5 der Spezifikation:
    // "If a managed bean which declares a passivating scope, ...:
    // * is not passivation capable,
    // * has an injection point that is not passivation capable,
    // * ...
    // then the container automatically detects the problem and treats it as a deployment problem."
    
    @Inject
    private transient IdentityAccessManagement iam;

    private String loginname;
    private transient IdentityVO identity;
    private DualListModel<RolleType> rollen;
    private transient List<RolleType> vorhandeneRollen;
    private static final List<RolleType> ALLE_ROLLEN = RolleType.valuesAsList();
    
    UpdateRollenModel() {
        super();
    }
    
    @Override
    public String toString() {
        return "UpdateRollenModel {loginnameUpdateRollen=" + loginname + '}';
    }
    
    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public DualListModel<RolleType> getRollen() {
        return rollen;
    }

    public void setRollen(DualListModel<RolleType> rollen) {
        this.rollen = rollen;
    }

    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public List<String> findLoginnamenByPrefix(String loginnamePrefix) {
        return iam.findLoginnamenByPrefix(loginnamePrefix);
    }
    
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public void findRollenByLoginname() {
        final Optional<IdentityVO> identityOpt = iam.findIdentityByLoginname(loginname);
        
        if (!identityOpt.isPresent()) {
            rollen = new DualListModel<>();
            return;
        }
        
        identity = identityOpt.get();
        final Optional<List<RolleType>> rollenOpt = iam.findRollen(loginname);
        vorhandeneRollen = new ArrayList<>(rollenOpt.orElse(Collections.emptyList()));
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Vorhandene Rollen von " + loginname + ": " + vorhandeneRollen);
        }
        
        final List<RolleType> restlicheRollen = ALLE_ROLLEN.stream()
                                                           .filter(r -> !vorhandeneRollen.contains(r))
                                                           .collect(toList());
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Restliche Rollen: " + restlicheRollen);
        }
        rollen = new DualListModel<>(restlicheRollen, vorhandeneRollen);
    }
    
    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public String updateRollen() {
        if (identity == null) {
            final Optional<IdentityVO> identityOpt = iam.findIdentityByLoginname(loginname);
            if (!identityOpt.isPresent()) {
                return null;
            }
            
            identity = identityOpt.get();
        }
        
        // Zusaetzliche Rollen, die urspruengl. noch nicht vorhandenen waren
        final List<RolleType> vorhandeneRollenAktuell = rollen.getTarget();
        final List<RolleType> zusaetzlicheRollen = vorhandeneRollenAktuell.stream()
                                                                          .filter(rolle -> !vorhandeneRollen.contains(rolle))
                                                                          .collect(toList());
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Zusaetzliche Rollen: " + zusaetzlicheRollen);
        }
        iam.grant(identity, zusaetzlicheRollen);
        
        // Zu entfernende Rollen, die nicht mehr ausgewaehlt sind
        final List<RolleType> zuEntfernendeRollen = vorhandeneRollen.stream()
                                                                    .filter(rolle -> !vorhandeneRollenAktuell.contains(rolle))
                                                                    .collect(toList());
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("Zu entfernende Rollen: " + zuEntfernendeRollen);
        }
        iam.revoke(identity, zuEntfernendeRollen);

        return JSF_INDEX + JSF_REDIRECT_SUFFIX;
    }
}
