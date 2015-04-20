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

package de.shop.util.jsf;

import java.io.Serializable;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;


/**
 * @deprecated
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Dependent
@Deprecated
// FIXME Java EE 8: implizite Producer
// https://weblogs.java.net/blog/mriem/archive/2014/10/10/jsf-23-inject-facescontext
// https://weblogs.java.net/blog/mriem/archive/2014/10/13/jsf-23-inject-externalcontext
// https://weblogs.java.net/blog/mriem/archive/2014/10/29/jsf-23-injecting-uiviewroot
// https://jcp.org/en/jsr/detail?id=372
public class JsfConfig implements Serializable {
    private static final long serialVersionUID = 3916523726340426731L;
    
    protected JsfConfig() {
        super();
    }
    
    @Produces
    @RequestScoped
    private FacesContext currentFacesContext() {
        return FacesContext.getCurrentInstance();
    }
    
    @Produces
    @RequestScoped
    private ExternalContext currentExternalContext(FacesContext ctx) {
        return ctx.getExternalContext();
    }

    
    @Produces
    @RequestScoped
    @RequestMap
    private Map<String, Object> currentRequestMap(ExternalContext ctx) {
        return ctx.getRequestMap();
    }

    @Produces
    @RequestScoped
    @SessionMap
    private Map<String, Object> currentSessionMap(ExternalContext ctx) {
        return ctx.getSessionMap();
    }
    
    // UIViewRoot enthaelt Methoden mit final, weshalb keine Proxy-Klasse erzeugt werden kann
    //SONAR_COMMENTED_OUT_BEGIN
    //@Produces
    //@RequestScoped
    //private UIViewRoot currentViewRoot(FacesContext ctx) {
    //    return ctx.getViewRoot();
    //}
    //SONAR_COMMENTED_OUT_END
}
