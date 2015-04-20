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
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@SessionScoped
@Named
@Log
public class Theme implements Serializable {
    private static final long serialVersionUID = -7795677531657784822L;
    
    private static final String CUPERTINO_STRING = "cupertino";   // blau
    private static final String BLITZER_STRING = "blitzer";       // rot
    private static final String HUMANITY_STRING = "humanity";
    private static final String LEFROG_STRING = "le-frog";
    private static final String SUNNY_STRING = "sunny";
    private static final ThemeType DEFAULT = ThemeType.CUPERTINO;
    
    private enum ThemeType {
        CUPERTINO(CUPERTINO_STRING),
        BLITZER(BLITZER_STRING),
        HUMANITY(HUMANITY_STRING),
        LEFROG(LEFROG_STRING),
        SUNNY(SUNNY_STRING);
        
        private final String value;

        private ThemeType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static ThemeType build(String value) {
            switch (value) {
                case CUPERTINO_STRING:
                    return CUPERTINO;
                    
                case BLITZER_STRING:
                    return BLITZER;
                    
                case HUMANITY_STRING:
                    return HUMANITY;
                    
                case LEFROG_STRING:
                    return LEFROG;
                    
                case SUNNY_STRING:
                    return SUNNY;
                    
                default:
                    return DEFAULT;
            }
        }
    }
    
    private ThemeType value = DEFAULT;
    private transient FacesContext ctx;
    
    /**
     * Package-private Default-Konstruktor f&uuml;r JSF
     */
    Theme() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param ctx zu injizierendes Objekt f&uuml;r FacesContext
     */
    @Inject
    Theme(FacesContext ctx) {
        super();
        this.ctx = ctx;
    }
    
    public String getValue() {
        return value.getValue();
    }
    
    public void setValue(String value) {
        this.value = ThemeType.build(value);
    }
    
    public void change(String skinStr) {
        final ThemeType newSkin = ThemeType.build(skinStr);
        if (newSkin.equals(value)) {
            return;
        }
        
        value = newSkin;
        ctx.renderResponse();
    }
}
