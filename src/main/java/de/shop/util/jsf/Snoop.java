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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Model
public class Snoop {
    private MemoryUsage heapMemoryUsage;
    private MemoryUsage nonHeapMemoryUsage;
    private List<MemoryPoolMXBean> memoryPoolMXBeans;
    
    private List<String> headerNames;
    private List<String> parameterNames;
    private List<String> attributeNames;
    private List<String> initParameterNames;
    
    private HttpServletRequest request;

    /**
     * Package-private Default-Konstruktor f&uuml;r JSF
     */
    Snoop() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param request zu injizierendes Objekt f&uuml;r HttpServletRequest
     */
    @Inject
    Snoop(HttpServletRequest request) {
        this.request = request;
    }

    @PostConstruct
    private void postConstruct() {
        final MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
        heapMemoryUsage = memoryMxBean.getHeapMemoryUsage();
        nonHeapMemoryUsage = memoryMxBean.getNonHeapMemoryUsage();
        
        memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        
        Enumeration<String> e = request.getHeaderNames();
        headerNames = new ArrayList<>();
        if (e != null && e.hasMoreElements()) {
            while (e.hasMoreElements()) {
                headerNames.add(e.nextElement());
            }
        }
        
        e = request.getParameterNames();
        parameterNames = new ArrayList<>();
        if (e != null && e.hasMoreElements()) {
            while (e.hasMoreElements()) {
                parameterNames.add(e.nextElement());
            }
        }
        
        e = request.getAttributeNames();
        attributeNames = new ArrayList<>();
        if (e != null && e.hasMoreElements()) {
            while (e.hasMoreElements()) {
                attributeNames.add(e.nextElement());
            }
        }
        
        e = request.getServletContext().getInitParameterNames();
        initParameterNames = new ArrayList<>();
        if (e != null && e.hasMoreElements()) {
            while (e.hasMoreElements()) {
                initParameterNames.add(e.nextElement());
            }
        }
    }

    public MemoryUsage getHeapMemoryUsage() {
        return heapMemoryUsage;
    }

    public MemoryUsage getNonHeapMemoryUsage() {
        return nonHeapMemoryUsage;
    }

    public List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        return memoryPoolMXBeans;
    }

    public List<String> getHeaderNames() {
        return headerNames;
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public List<String> getInitParameterNames() {
        return initParameterNames;
    }
}
