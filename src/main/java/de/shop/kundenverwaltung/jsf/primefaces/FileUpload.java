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

package de.shop.kundenverwaltung.jsf.primefaces;

import de.shop.iam.business.IdentityAccessManagement;
import de.shop.kundenverwaltung.business.KundenBroker;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.interceptor.Log;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import static de.shop.iam.domain.RolleType.ADMIN_STRING;
import static de.shop.iam.domain.RolleType.KUNDE_STRING;
import static de.shop.iam.domain.RolleType.MITARBEITER_STRING;
import static de.shop.kundenverwaltung.business.KundenBroker.FetchType.NUR_KUNDE;
import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;
import static java.util.logging.Level.FINER;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
// es koennte aus eine FileUpload-Klasse fuer Artikel geben
@Named("fileUploadKunde")
@ViewScoped
@Transactional
@Log
public class FileUpload implements Serializable {
    private static final long serialVersionUID = 3377481542931338167L;

    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    // Field Injection: KundenBroker und IdentityAccessManagement sind nicht serialisierbar,
    // weshalb transient wegen @ConversationScoped notwendig ist. Kap. 6.6.5 der Spezifikation:
    // "If a managed bean which declares a passivating scope, ...:
    // * is not passivation capable,
    // * has an injection point that is not passivation capable,
    // * ...
    // then the container automatically detects the problem and treats it as a deployment problem."
    
    @Inject
    private transient KundenBroker kundenBroker;
    
    @Inject
    private transient IdentityAccessManagement iam;
    
    private String loginname;
    private transient byte[] bytes;
    private String contentType;

    FileUpload() {
        super();
    }

    @PostConstruct
    private void postConstruct() {
        // TODO Conversation starten und in upload() beenden.
        // Bug in RichFaces: der Upload-Listener beendet die Conversation, bevor die Methode upload() aufgerufen ist
    }

    @Override
    public String toString() {
        final String size = bytes == null ? "null" : String.valueOf(bytes.length);
        return "FileUpload {loginname=" + loginname + ", contentType=" + contentType + ", size=" + size + '}';
    }
    
    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public void uploadListener(FileUploadEvent event) {
        final UploadedFile uploadedFile = event.getFile();
        contentType = uploadedFile.getContentType();
        bytes = uploadedFile.getContents();
        if (bytes == null) {
            LOGGER.warning("FileUpload: 0 (!) Bytes");
        }
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("Hochgeladenen Datei: " + bytes.length + " Bytes mit MIME-Type " + contentType);
        }
    }

    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
    public String upload() {
        final Optional<? extends AbstractKunde> kundeOpt = kundenBroker.findByLoginname(loginname, NUR_KUNDE);
        if (!kundeOpt.isPresent()) {
            iam.checkAdminMitarbeiter();
            // TODO Fehlerbehandlung bei ungueltigem Login
            return JSF_INDEX + JSF_REDIRECT_SUFFIX;
        }

        final AbstractKunde kunde = kundeOpt.get();
        iam.checkSameIdentity(kunde.getLoginname());
        kundenBroker.setFile(kunde, bytes, contentType);
        return JSF_INDEX + JSF_REDIRECT_SUFFIX;
    }
    
    // JSF 2.2 statt RichFaces
    //SONAR_COMMENTED_OUT_BEGIN
//    @RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
//    public String upload() throws IOException {
//        final Optional<AbstractKunde> kundeOpt = kundenBroker.findByLoginname(loginname, FetchType.NUR_KUNDE);
//        if (!kundeOpt.isPresent()) {
//            iam.checkAdminMitarbeiter();
//            // TODO Fehlerbehandlung bei ungueltigem Login
//            return JSF_INDEX + JSF_REDIRECT_SUFFIX;
//        }
//        
//        final AbstractKunde kunde = kundeOpt.get();
//        iam.checkSameIdentity(kunde.getLoginname());
//
//        contentType = file.getContentType();
//        if (LOGGER.isLoggable(FINER)) {
//            LOGGER.finer("MIME-Type der hochgeladenen Datei: " + contentType);
//        }
//        final int size = (int) file.getSize();
//        try (final InputStream inputStream = file.getInputStream()) {
//            bytes = new byte[size];
//            for (int total = 0; total < size; ) {
//                final int result = inputStream.read(bytes, total, size - total);
//                if (result == -1) {
//                    break;
//                }
//                total += result;
//            }
//        }
//        if (LOGGER.isLoggable(FINER)) {
//            LOGGER.finer("Dateigroesse: " + size);
//        }
//        
//        ks.setFile(kunde, bytes, contentType);
//        return JSF_INDEX + JSF_REDIRECT_SUFFIX;
//    }
    //SONAR_COMMENTED_OUT_END
    
    public String resetUpload() {
        loginname = null;
        contentType = null;
        bytes = null;
        
        return null;
    }
}
