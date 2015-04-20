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

import de.shop.util.interceptor.Log;
import java.io.Serializable;
import java.util.Locale;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@SessionScoped
@Log
public class Sprache implements Serializable {
    private static final long serialVersionUID = 1986565724093259408L;
    
    // DI: ExternalContext ist nicht serialisierbar, weshalb transient
    // wegen @ConversationScoped notwendig ist. Kap. 6.6.5 der Spezifikation:
    // "If a managed bean which declares a passivating scope, ...:
    // * is not passivation capable,
    // * has an injection point that is not passivation capable,
    // * ...
    // then the container automatically detects the problem and treats it as a deployment problem."
    
    private transient Instance<FacesContext> ctx;
    
    // FIXME Java EE 8: Producer fuer UIViewRoot
    //SONAR_COMMENTED_OUT_BEGIN
    //@Inject
    //private UIViewRoot viewRoot;
    //SONAR_COMMENTED_OUT_END
    
    @Produces
    @Named
    @Client
    private Locale locale;
    
    /**
     * Package-private Default-Konstruktor f&uuml;r JSF
     */
    Sprache() {
        super();
    }
    
    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param externalCtx Injiziertes Objekt fuer ExternalContext in JSF
     * @param ctx Injiziertes Instance-Objekt fuer FacesContext
     */
    @Inject
    Sprache(ExternalContext externalCtx, Instance<FacesContext> ctx) {
        super();
        this.ctx = ctx;

        locale = externalCtx.getRequestLocale();
    }

    public void change(String localeStr) {
        final Locale newLocale = new Locale(localeStr);
        if (newLocale.equals(locale)) {
            return;
        }
        
        locale = newLocale;
        
        final FacesContext facesContext = ctx.get();
        facesContext.getViewRoot().setLocale(locale);
        facesContext.renderResponse();
    }
    
    //SONAR_COMMENTED_OUT_BEGIN
//    @Produces
//    @Client
//    public Locale getLocale() {
//        return viewRoot != null
//               ? viewRoot.getLocale()
//               : ctx.getApplication().getViewHandler().calculateLocale(ctx);
//    }
    //SONAR_COMMENTED_OUT_END
    
    @Override
    public String toString() {
        return "Sprache {locale=" + locale + '}';
    }
}
