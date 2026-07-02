package com.jobpilot.infrastructure.persistence.user;

import com.jobpilot.application.user.dto.UserSettingsResponse;
import com.jobpilot.application.user.ports.UserSettingsRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserSettingsRepositoryImpl implements UserSettingsRepository {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<UserSettingsResponse> findByUserId(UUID userId) {
        var sql = """
            SELECT job_preferences, notification_prefs, privacy_settings, ai_preferences, appearance
            FROM user_settings WHERE user_id = ?
            """;
        var query = em.createNativeQuery(sql);
        query.setParameter(1, userId);
        var rows = query.getResultList();
        if (rows.isEmpty()) return Optional.empty();

        var row = (Object[]) rows.get(0);
        return Optional.of(new UserSettingsResponse(
            parseJson((String) row[0]),
            parseJson((String) row[1]),
            parseJson((String) row[2]),
            parseJson((String) row[3]),
            parseJson((String) row[4])
        ));
    }

    @Override
    public void save(UUID userId, Map<String, Object> jobPrefs, Map<String, Object> notifPrefs,
                     Map<String, Object> privacy, Map<String, Object> aiPrefs, Map<String, Object> appearance) {
        var sql = """
            INSERT INTO user_settings (user_id, job_preferences, notification_prefs, privacy_settings, ai_preferences, appearance)
            VALUES (?, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb)
            ON CONFLICT (user_id)
            DO UPDATE SET job_preferences = EXCLUDED.job_preferences,
                          notification_prefs = EXCLUDED.notification_prefs,
                          privacy_settings = EXCLUDED.privacy_settings,
                          ai_preferences = EXCLUDED.ai_preferences,
                          appearance = EXCLUDED.appearance
            """;
        var query = em.createNativeQuery(sql);
        query.setParameter(1, userId);
        query.setParameter(2, toJson(jobPrefs));
        query.setParameter(3, toJson(notifPrefs));
        query.setParameter(4, toJson(privacy));
        query.setParameter(5, toJson(aiPrefs));
        query.setParameter(6, toJson(appearance));
        query.executeUpdate();
    }

    private static Map<String, Object> parseJson(String json) {
        try {
            return MAPPER.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static String toJson(Map<String, Object> map) {
        try {
            return MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }
}
