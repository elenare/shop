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

package de.shop.iam.jsf;

import de.shop.util.interceptor.Log;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;


/**
 * Dialogsteuerung f&uuml;r Authentifizierung (Login und Logout) und Authorisierung (rollenbasierte Berechtigungen).
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@SessionScoped
@Log
public class LastPageModel implements Serializable {
    private static final long serialVersionUID = -2802484734242983569L;

    // DI: Instance ist ein nicht serialisierbares Interface, weshalb transient
    // wegen @ConversationScoped notwendig ist. Kap. 6.6.5 der Spezifikation:
    // "If a managed bean which declares a passivating scope, ...:
    // * is not passivation capable,
    // * has an injection point that is not passivation capable,
    // * ...
    // then the container automatically detects the problem and treats it as a deployment problem."
    
    @Inject
    private transient Instance<FacesContext> ctxInstance;
    
    private String path;
    
    LastPageModel() {
        super();
    }

    public String getPath() {
        return path;
    }
    
    public void remember() {
        final String tmp = ctxInstance.get().getViewRoot().getViewId();
        if (!tmp.endsWith("login.xhtml")) {
            path = tmp;
        }
    }
    
    public void reset() {
        path = null;
    }
}
