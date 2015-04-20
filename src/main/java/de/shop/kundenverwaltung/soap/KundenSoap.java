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

import de.shop.kundenverwaltung.business.KundenBroker;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Privatkunde;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.MIT_BESTELLUNGEN;
import static java.util.stream.Collectors.toSet;

/**
 * https://localhost:8443/shop/KundeSOAPService/KundeSOAP?wsdl
 * standalone\data\wsdl\shop.war\KundeSoapService.wsdl
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@WebService(name = "KundeSoap",
            targetNamespace = "urn:shop:soap:kunde",
            serviceName = "KundeSoapService")
// default: document/literal (einzige Option fuer Integration mit .NET)
@SOAPBinding
@Dependent
//@Interceptors(ConstraintViolationInterceptor.class)
//@WebContext(authMethod = "BASIC",
//            // TLS durch "CONFIDENTIAL" oder "INTEGRAL" (Schutz vor Aenderungen waehrend der Uebertragung, ABER: 302)
//            // sonst "NONE"
//            transportGuarantee = "CONFIDENTIAL",
//            secureWSDLAccess = true)
public class KundenSoap {
    private KundenBroker kundenBroker;

    /**
     * Public Default-Konstruktor f&uuml;r JAX-WS
     */
    public KundenSoap() {
        super();
    }

    /**
     * Package-private Konstruktor mit "Constructor Injection" f&uuml;r CDI
     * @param kundenBroker zu injizierendes Objekt f&uuml;r KundenBroker
     */
    @Inject
    KundenSoap(KundenBroker kundenBroker) {
        super();
        this.kundenBroker = kundenBroker;
    }

    /**
     * Einen Privatkunden anhand seiner ID suchen: Bei SOAP muss der Rueckgabetyp eine konkrete Klasse sein.
     * @param id Id des gesuchten Privatkunden
     * @return Der gefundene Privatkunde oder null
     * 
     */
    @WebResult(name = "privatkunde")
    //@RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public PrivatkundeVO findById(@WebParam(name = "id") long id) {
        final Optional<? extends AbstractKunde> kundeOpt = kundenBroker.findById(id, MIT_BESTELLUNGEN);
        if (!kundeOpt.isPresent()) {
            return null;
        }
        final AbstractKunde kunde = kundeOpt.get();
        if (!(kunde instanceof Privatkunde)) {
            return null;
        }
        
        final Privatkunde privatkunde = Privatkunde.class.cast(kunde);
        return new PrivatkundeVO(privatkunde);
    }
    
    /**
     * Privatkunden suchen: Bei SOAP muss der Rueckgabetyp eine konkrete Klasse sein.
     * @param nachname Der Nachname der gesuchten Privatkunden
     * @return Die Menge der gefundenen Privatkunden
     */
    @WebResult(name = "privatkunden")
//  @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
    public Set<PrivatkundeVO> findByNachname(@WebParam(name = "nachname") String nachname) {
        return kundenBroker.findByNachname(nachname, MIT_BESTELLUNGEN)
                           .orElse(Collections.emptyList())
                           .stream()
                           .filter(kunde -> kunde instanceof Privatkunde)
                           .map(privatkunde -> new PrivatkundeVO((Privatkunde) privatkunde))
                           .collect(toSet());
    }
}
