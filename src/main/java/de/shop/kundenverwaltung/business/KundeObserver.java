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

package de.shop.kundenverwaltung.business;

import de.shop.iam.domain.Adresse;
import de.shop.iam.domain.IdentityVO;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.mail.AbsenderEmail;
import de.shop.util.mail.AbsenderName;
import de.shop.util.mail.EmpfaengerEmail;
import de.shop.util.mail.EmpfaengerName;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.jboss.weld.experimental.Priority;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.SEVERE;
import static javax.enterprise.event.TransactionPhase.AFTER_SUCCESS;
import static javax.interceptor.Interceptor.Priority.APPLICATION;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Dependent
public class KundeObserver {
    private static final String NEWLINE = System.getProperty("line.separator");
    
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private final Session session;
    private final String absenderEmail;
    private final String absenderName;
    private final String empfaengerEmail;
    private final String empfaengerName;

    @Inject
    public KundeObserver(Session session, @AbsenderEmail String absenderEmail, @AbsenderName String absenderName, @EmpfaengerEmail String empfaengerEmail, @EmpfaengerName String empfaengerName) {
        super();
        this.session = session;
        this.absenderEmail = absenderEmail;
        this.absenderName = absenderName;
        this.empfaengerEmail = empfaengerEmail;
        this.empfaengerName = empfaengerName;

        if (absenderEmail == null || empfaengerEmail == null) {
            LOGGER.warning("Absender oder Empfaenger fuer Markteting-Emails sind nicht gesetzt.");
            return;
        }
        
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.info("Absender fuer Markteting-Emails: " + absenderName + "<" + absenderEmail + ">");
            LOGGER.info("Empfaenger fuer Markteting-Emails: " + empfaengerName + "<" + empfaengerEmail + ">");
        }
    }
    
    // Loose Kopplung durch @Observes, d.h. ohne JMS
    private void onCreateKunde(@Observes(during = AFTER_SUCCESS)
                               @Priority(APPLICATION + 100)
                               @NeuerKunde
                               final AbstractKunde kunde) {
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("onCreateKunde BEGINN: " + kunde);
        }
        
        if (absenderEmail == null || empfaengerEmail == null || kunde == null) {
            return;
        }
        
        final MimeMessage message = new MimeMessage(session);

        try {
            // Absender setzen
            final InternetAddress absenderObj = new InternetAddress(absenderEmail, absenderName);
            message.setFrom(absenderObj);

            // Empfaenger setzen
            final InternetAddress empfaenger = new InternetAddress(empfaengerEmail, empfaengerName);
            // RecipientType: TO, CC, BCC
            message.setRecipient(RecipientType.TO, empfaenger);

            final Adresse adr = kunde.getIdentity().getAdresse();

            // Subject setzen
            final String subject = adr == null
                    ? "Neuer Kunde ohne Adresse"
                    : "Neuer Kunde in " + adr.getPlz() + " " + adr.getOrt();
            message.setSubject(subject);

            // HTML-Text setzen mit MIME Type "text/html"
            final IdentityVO identity = kunde.getIdentity();
            String text = "<p><b>" + identity.getVorname() + " " + identity.getNachname() + "</b></p>" + NEWLINE;
            if (adr != null) {
                text += "<p>" + adr.getPlz() + " " + adr.getOrt() + "</p>" + NEWLINE
                        + "<p>" + adr.getStrasse() + " " + adr.getHausnr() + "</p>" + NEWLINE;
            }

            message.setContent(text, "text/html");

            Transport.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOGGER.log(SEVERE, e.getMessage(), e);
            return;
        }

        if (LOGGER.isLoggable(FINER)) {
            LOGGER.finer("onCreateKunde ENDE");
        }
    }
}
