package com.jobpilot.infrastructure.notification;

import com.jobpilot.application.notification.ports.EmailMonitorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.SubjectTerm;

@Component
public class ImapEmailMonitorAdapter implements EmailMonitorPort {

    private static final Logger log = LoggerFactory.getLogger(ImapEmailMonitorAdapter.class);

    @Value("${jobpilot.email.imap.host:}")
    private String imapHost;

    @Value("${jobpilot.email.imap.port:993}")
    private int imapPort;

    @Value("${jobpilot.email.imap.username:}")
    private String username;

    @Value("${jobpilot.email.imap.password:}")
    private String password;

    @Value("${jobpilot.email.imap.folder:INBOX}")
    private String defaultFolder;

    private volatile boolean authenticated = false;
    private volatile Store store;

    private Store getStore() {
        if (store != null && store.isConnected()) return store;
        try {
            var props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imaps.host", imapHost);
            props.put("mail.imaps.port", String.valueOf(imapPort));
            props.put("mail.imaps.ssl.enable", "true");

            var session = Session.getInstance(props);
            store = session.getStore("imaps");
            store.connect(imapHost, username, password);
            authenticated = true;
            log.info("Connected to IMAP server: {}", imapHost);
        } catch (Exception e) {
            log.error("IMAP connection failed: {}", e.getMessage());
            authenticated = false;
        }
        return store;
    }

    @Override
    public List<Map<String, Object>> fetchRecentEmails(String folder, int maxCount) {
        var emails = new ArrayList<Map<String, Object>>();
        var store = getStore();
        if (store == null || !store.isConnected()) return emails;

        try {
            var inbox = store.getFolder(folder != null ? folder : defaultFolder);
            inbox.open(Folder.READ_ONLY);

            var messages = inbox.getMessages();
            var start = Math.max(0, messages.length - maxCount);

            for (int i = messages.length - 1; i >= start; i--) {
                try {
                    var msg = (MimeMessage) messages[i];
                    var email = new LinkedHashMap<String, Object>();
                    email.put("id", msg.getMessageID());
                    email.put("subject", msg.getSubject());
                    email.put("from", getFromAddress(msg));
                    email.put("date", msg.getSentDate() != null ? msg.getSentDate().toString() : null);
                    email.put("body", getPlainTextContent(msg));
                    email.put("folder", folder);
                    emails.add(email);
                } catch (Exception e) {
                    log.warn("Failed to read email: {}", e.getMessage());
                }
            }

            inbox.close(false);
        } catch (Exception e) {
            log.error("Failed to fetch emails: {}", e.getMessage());
        }

        return emails;
    }

    @Override
    public List<Map<String, Object>> searchEmails(String query, int maxCount) {
        var emails = new ArrayList<Map<String, Object>>();
        var store = getStore();
        if (store == null || !store.isConnected()) return emails;

        try {
            var inbox = store.getFolder(defaultFolder);
            inbox.open(Folder.READ_ONLY);

            var searchTerm = new SubjectTerm(query);
            var messages = inbox.search(searchTerm);

            var count = Math.min(messages.length, maxCount);
            for (int i = messages.length - 1; i >= messages.length - count && i >= 0; i--) {
                try {
                    var msg = (MimeMessage) messages[i];
                    var email = new LinkedHashMap<String, Object>();
                    email.put("id", msg.getMessageID());
                    email.put("subject", msg.getSubject());
                    email.put("from", getFromAddress(msg));
                    email.put("date", msg.getSentDate() != null ? msg.getSentDate().toString() : null);
                    email.put("body", getPlainTextContent(msg));
                    emails.add(email);
                } catch (Exception e) {
                    log.warn("Failed to read searched email: {}", e.getMessage());
                }
            }

            inbox.close(false);
        } catch (Exception e) {
            log.error("Failed to search emails: {}", e.getMessage());
        }

        return emails;
    }

    @Override
    public Map<String, Object> getEmail(String messageId) {
        var store = getStore();
        if (store == null || !store.isConnected()) return Map.of();

        try {
            var inbox = store.getFolder(defaultFolder);
            inbox.open(Folder.READ_ONLY);

            var messages = inbox.getMessages();
            for (var msg : messages) {
                var mimeMsg = (MimeMessage) msg;
                if (messageId.equals(mimeMsg.getMessageID())) {
                    var email = new LinkedHashMap<String, Object>();
                    email.put("id", mimeMsg.getMessageID());
                    email.put("subject", mimeMsg.getSubject());
                    email.put("from", getFromAddress(mimeMsg));
                    email.put("date", mimeMsg.getSentDate() != null ? mimeMsg.getSentDate().toString() : null);
                    email.put("body", getPlainTextContent(mimeMsg));
                    inbox.close(false);
                    return email;
                }
            }

            inbox.close(false);
        } catch (Exception e) {
            log.error("Failed to get email {}: {}", messageId, e.getMessage());
        }

        return Map.of();
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated && store != null && store.isConnected();
    }

    private String getFromAddress(MimeMessage msg) {
        try {
            var from = msg.getFrom();
            if (from != null && from.length > 0) {
                return from[0].toString();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getPlainTextContent(MimeMessage msg) {
        try {
            if (msg.isMimeType("text/plain")) {
                return (String) msg.getContent();
            }
            if (msg.isMimeType("text/html")) {
                var html = (String) msg.getContent();
                return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
            }
            if (msg.isMimeType("multipart/*")) {
                var multipart = (Multipart) msg.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    var part = multipart.getBodyPart(i);
                    if (part.isMimeType("text/plain")) {
                        return (String) part.getContent();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract email content: {}", e.getMessage());
        }
        return null;
    }
}
