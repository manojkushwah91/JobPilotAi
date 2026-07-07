package com.jobpilot.infrastructure.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public final class JsonConfig {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonConfig() {}

    public static String toJson(Map<String, Object> map) {
        try {
            return MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize map", e);
        }
    }

    public static Map<String, Object> fromJsonMap(String json) {
        try {
            if (json == null || json.isBlank()) return null;
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize map: " + json, e);
        }
    }
}
