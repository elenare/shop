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

package de.shop.kundenverwaltung.soap;

import de.shop.kundenverwaltung.business.RegistrierungBroker;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.transaction.Transactional;

/**
 * https://localhost:8443/shop/KundeSOAPService/KundeSOAP?wsdl
 * standalone\data\wsdl\shop.war\KundeSoapService.wsdl
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@WebService(name = "RegistrierungSoap",
            targetNamespace = "urn:shop:soap:registrierung",
            serviceName = "RegistrierungSoapService")
// default: document/literal (einzige Option fuer Integration mit .NET)
@SOAPBinding
@Dependent
@Transactional
public class RegistrierungSoap {
    @Inject
    private RegistrierungBroker registrierungBroker;

    /**
     * Public Default-Konstruktor f&uuml;r JAX-WS
     */
    public RegistrierungSoap() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param registrierungBroker zu injizierendes Objekt f&uuml;r RegistrierungBroker
     */
    @Inject
    RegistrierungSoap(RegistrierungBroker registrierungBroker) {
        super();
        this.registrierungBroker = registrierungBroker;
    }
    
    /**
     * Ermittlung der Versionsnummer
     * @return Versionsnummer
     */
    @WebResult(name = "version")
    public String getVersion() {
        return "1.0";
    }
    
    /**
     * Einen Privatkunden anlegen: Bei SOAP muss der Argumenttyp eine konkrete Klasse sein.
     * @param  privatkundeVO Objekt mit den Daten eines neuen Privatkunden
     * @return ID des neuen Privatkunden
     */
    @WebResult(name = "id")
    public long savePrivatkunde(@WebParam(name = "privatkunde") PrivatkundeVO privatkundeVO) {
        final AbstractKunde neuerKunde = registrierungBroker.save(privatkundeVO.toPrivatkunde());
        return neuerKunde.getId();
    }
}
