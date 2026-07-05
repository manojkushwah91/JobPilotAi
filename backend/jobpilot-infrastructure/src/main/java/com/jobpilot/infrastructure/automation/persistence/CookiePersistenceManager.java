package com.jobpilot.infrastructure.automation.persistence;

import com.jobpilot.infrastructure.automation.PlaywrightBrowserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Component
public class CookiePersistenceManager {

    private static final Logger log = LoggerFactory.getLogger(CookiePersistenceManager.class);

    @Value("${jobpilot.cookies.dir:./cookies}")
    private String cookiesDir;

    public void saveCookies(PlaywrightBrowserManager browserManager, String sessionId) {
        try {
            var context = browserManager.getContext();
            if (context == null) return;

            var cookies = context.cookies();
            var path = Paths.get(cookiesDir, sessionId + ".json");
            createDirectoryIfNeeded(path.getParent());

            var sb = new StringBuilder("[");
            for (int i = 0; i < cookies.size(); i++) {
                var cookie = cookies.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"name\":\"").append(escapeJson(cookie.name)).append("\",");
                sb.append("\"value\":\"").append(escapeJson(cookie.value)).append("\",");
                sb.append("\"domain\":\"").append(escapeJson(cookie.domain)).append("\",");
                sb.append("\"path\":\"").append(escapeJson(cookie.path)).append("\",");
                sb.append("\"httpOnly\":").append(cookie.httpOnly).append(",");
                sb.append("\"secure\":").append(cookie.secure);
                sb.append("}");
            }
            sb.append("]");

            Files.writeString(path, sb.toString());
            log.debug("Saved {} cookies for session {}", cookies.size(), sessionId);
        } catch (Exception e) {
            log.error("Failed to save cookies: {}", e.getMessage());
        }
    }

    public void loadCookies(PlaywrightBrowserManager browserManager, String sessionId) {
        try {
            var path = Paths.get(cookiesDir, sessionId + ".json");
            if (!Files.exists(path)) return;

            var json = Files.readString(path);
            var context = browserManager.getContext();
            if (context == null) return;

            var cookieList = parseCookiesJson(json);
            if (!cookieList.isEmpty()) {
                var page = browserManager.getPage();
                if (page != null) {
                    for (var cookie : cookieList) {
                        var domain = cookie.get("domain");
                        var name = cookie.get("name");
                        var value = cookie.get("value");
                        var script = String.format(
                            "document.cookie = '%s=%s; path=%s; domain=%s'",
                            name, value, cookie.get("path"), domain
                        );
                        page.evaluate(script);
                    }
                    log.debug("Loaded {} cookies for session {}", cookieList.size(), sessionId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to load cookies: {}", e.getMessage());
        }
    }

    public boolean hasCookies(String sessionId) {
        return Files.exists(Paths.get(cookiesDir, sessionId + ".json"));
    }

    public void deleteCookies(String sessionId) {
        try {
            var path = Paths.get(cookiesDir, sessionId + ".json");
            Files.deleteIfExists(path);
            log.debug("Deleted cookies for session {}", sessionId);
        } catch (Exception e) {
            log.error("Failed to delete cookies: {}", e.getMessage());
        }
    }

    private List<Map<String, String>> parseCookiesJson(String json) {
        var cookies = new ArrayList<Map<String, String>>();
        try {
            json = json.trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]")) json = json.substring(0, json.length() - 1);

            var entries = json.split("\\},\\s*\\{");
            for (var entry : entries) {
                entry = entry.replace("{", "").replace("}", "").trim();
                if (entry.isEmpty()) continue;

                var fields = parseFields(entry);
                cookies.add(fields);
            }
        } catch (Exception e) {
            log.error("Failed to parse cookies: {}", e.getMessage());
        }
        return cookies;
    }

    private Map<String, String> parseFields(String json) {
        var fields = new HashMap<String, String>();
        var parts = json.split(",");
        for (var part : parts) {
            var colonIdx = part.indexOf(':');
            if (colonIdx > 0) {
                var key = part.substring(0, colonIdx).trim().replace("\"", "");
                var value = part.substring(colonIdx + 1).trim().replace("\"", "");
                fields.put(key, value);
            }
        }
        return fields;
    }

    private void createDirectoryIfNeeded(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
