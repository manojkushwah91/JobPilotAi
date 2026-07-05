package com.jobpilot.infrastructure.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AutomationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AutomationWebSocketHandler.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArrayList<String>> sessionSubscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket connected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        var payload = message.getPayload();
        log.debug("Received message from {}: {}", session.getId(), payload);

        if (payload.startsWith("subscribe:")) {
            var topic = payload.substring("subscribe:".length());
            sessionSubscriptions.computeIfAbsent(session.getId(), k -> new CopyOnWriteArrayList<>()).add(topic);
            session.sendMessage(new TextMessage("{\"type\":\"subscribed\",\"topic\":\"" + topic + "\"}"));
        } else if (payload.startsWith("unsubscribe:")) {
            var topic = payload.substring("unsubscribe:".length());
            var subs = sessionSubscriptions.get(session.getId());
            if (subs != null) subs.remove(topic);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        sessionSubscriptions.remove(session.getId());
        log.info("WebSocket disconnected: {}", session.getId());
    }

    public void broadcastToTopic(String topic, Map<String, Object> data) {
        var message = "{\"type\":\"" + topic + "\",\"data\":" + toJson(data) + "}";
        sessions.values().forEach(session -> {
            var subs = sessionSubscriptions.get(session.getId());
            if (subs != null && subs.contains(topic)) {
                try {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(message));
                    }
                } catch (IOException e) {
                    log.error("Failed to send message to {}: {}", session.getId(), e.getMessage());
                }
            }
        });
    }

    public void sendToSession(String sessionId, Map<String, Object> data) {
        var session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                var message = "{\"type\":\"update\",\"data\":" + toJson(data) + "}";
                synchronized (session) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                log.error("Failed to send message to {}: {}", sessionId, e.getMessage());
            }
        }
    }

    public void broadcastToAll(Map<String, Object> data) {
        var message = "{\"type\":\"broadcast\",\"data\":" + toJson(data) + "}";
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(message));
                    }
                } catch (IOException e) {
                    log.error("Failed to broadcast: {}", e.getMessage());
                }
            }
        });
    }

    public int getActiveConnections() {
        return (int) sessions.values().stream().filter(WebSocketSession::isOpen).count();
    }

    private String toJson(Map<String, Object> data) {
        var sb = new StringBuilder();
        sb.append("{");
        var first = true;
        for (var entry : data.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            if (entry.getValue() instanceof String s) {
                sb.append("\"").append(escapeJson(s)).append("\"");
            } else if (entry.getValue() instanceof Number n) {
                sb.append(n);
            } else if (entry.getValue() instanceof Boolean b) {
                sb.append(b);
            } else {
                sb.append("\"").append(escapeJson(String.valueOf(entry.getValue()))).append("\"");
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
