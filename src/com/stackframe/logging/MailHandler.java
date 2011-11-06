/*
 * Copyright 2011 StackFrame, LLC
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * You should have received a copy of the GNU General Public License
 * along with this file.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stackframe.logging;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * A logging handler that sends the log messages as email.
 *
 * @author Gene McCulley
 */
public class MailHandler extends Handler {

    private final String to;

    private final String subject;

    private final String from;

    private final String host;

    private final int port;

    private static Properties makeMailProperties(String from, String host, int port) {
        Properties props = new Properties();
        props.put("mail.from", from);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", Integer.toString(port));
        return props;
    }

    private static void sendEmail(String to, String from, String subject, String body, String host, int port) throws Exception {
        Session session = Session.getDefaultInstance(makeMailProperties(from, host, port), null);
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from, true));
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to, true));
        msg.setSubject(subject);
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        msg.setContent(multipart);
        msg.setSentDate(new Date());
        msg.saveChanges();
        Transport transport = session.getTransport("smtp");
        transport.connect(host, port, null, null);
        transport.sendMessage(msg, msg.getAllRecipients());
        transport.close();
    }

    /**
     * Create a new MailHandler.
     *
     * @param to the email address that log messages should be sent to
     * @param subject the subject to use for log message emails
     * @param from the email address that log messages will be from
     * @param host the SMTP server to send through
     * @param port the port to use when sending via SMTP
     */
    public MailHandler(String to, String subject, String from, String host, int port) {
        this.to = to;
        this.subject = subject;
        this.from = from;
        this.host = host;
        this.port = port;
    }

    @Override
    public void close() throws SecurityException {
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(LogRecord lr) {
        Formatter formatter = new SimpleFormatter();
        String formatted = formatter.format(lr);
        try {
            sendEmail(to, from, subject, formatted, host, port);
        } catch (Exception e) {
            // FIXME: Use logger error reporter.
            System.err.println("exception sending email: " + e);
        }
    }
}
