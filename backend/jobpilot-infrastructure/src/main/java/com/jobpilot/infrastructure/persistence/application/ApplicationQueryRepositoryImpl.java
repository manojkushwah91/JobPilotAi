package com.jobpilot.infrastructure.persistence.application;

import com.jobpilot.application.application.ports.ApplicationQueryRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
public class ApplicationQueryRepositoryImpl implements ApplicationQueryRepository {

    private final EntityManager em;

    public ApplicationQueryRepositoryImpl(EntityManager em) { this.em = em; }

    @Override
    public List<Map<String, Object>> findNotesByApplicationId(UUID applicationId) {
        var list = new ArrayList<Map<String, Object>>();
        var rows = em.createNativeQuery(
            "SELECT id, content, category, created_at FROM application_notes WHERE application_id = ?1 ORDER BY created_at DESC")
            .setParameter(1, applicationId).getResultList();
        for (var row : rows) {
            var o = (Object[]) row;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o[0].toString());
            m.put("content", o[1]);
            m.put("category", o[2]);
            m.put("createdAt", o[3] != null ? o[3].toString() : null);
            list.add(m);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> findAttachmentsByApplicationId(UUID applicationId) {
        var list = new ArrayList<Map<String, Object>>();
        var rows = em.createNativeQuery(
            "SELECT id, file_name, file_type, file_url, file_size, created_at FROM application_attachments WHERE application_id = ?1 ORDER BY created_at DESC")
            .setParameter(1, applicationId).getResultList();
        for (var row : rows) {
            var o = (Object[]) row;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o[0].toString());
            m.put("fileName", o[1]);
            m.put("fileType", o[2]);
            m.put("fileUrl", o[3]);
            m.put("fileSize", o[4]);
            m.put("createdAt", o[5] != null ? o[5].toString() : null);
            list.add(m);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> findFollowUpsByApplicationId(UUID applicationId) {
        var list = new ArrayList<Map<String, Object>>();
        var rows = em.createNativeQuery(
            "SELECT id, type, subject, content, status, scheduled_at, sent_at, created_at FROM application_followups WHERE application_id = ?1 ORDER BY created_at DESC")
            .setParameter(1, applicationId).getResultList();
        for (var row : rows) {
            var o = (Object[]) row;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o[0].toString());
            m.put("type", o[1]);
            m.put("subject", o[2]);
            m.put("content", o[3]);
            m.put("status", o[4]);
            m.put("scheduledAt", o[5] != null ? o[5].toString() : null);
            m.put("sentAt", o[6] != null ? o[6].toString() : null);
            m.put("createdAt", o[7] != null ? o[7].toString() : null);
            list.add(m);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> findTimelineByApplicationId(UUID applicationId) {
        var list = new ArrayList<Map<String, Object>>();
        var rows = em.createNativeQuery(
            "SELECT status_history FROM applications WHERE id = ?1")
            .setParameter(1, applicationId).getResultList();
        if (!rows.isEmpty()) {
            var history = (String) rows.get(0);
            list.add(Map.of("statusHistory", history != null ? history : "[]"));
        }
        if (list.isEmpty()) {
            var createdAt = em.createNativeQuery("SELECT created_at FROM applications WHERE id = ?1")
                .setParameter(1, applicationId).getResultList();
            if (!createdAt.isEmpty()) {
                list.add(Map.of("event", "Application created", "timestamp", createdAt.get(0).toString()));
            }
        }
        return list;
    }

    @Override
    public Map<String, Object> addNote(UUID applicationId, UUID userId, String content) {
        return addNote(applicationId, userId, content, "GENERAL");
    }

    @Override
    public Map<String, Object> addNote(UUID applicationId, UUID userId, String content, String category) {
        var id = java.util.UUID.randomUUID();
        em.createNativeQuery(
            "INSERT INTO application_notes (id, application_id, user_id, content, category, created_at, updated_at) VALUES (?1, ?2, ?3, ?4, ?5, now(), now())")
            .setParameter(1, id)
            .setParameter(2, applicationId)
            .setParameter(3, userId)
            .setParameter(4, content)
            .setParameter(5, category != null ? category : "GENERAL")
            .executeUpdate();
        return Map.of("id", id.toString(), "content", content, "category", category != null ? category : "GENERAL", "createdAt", Instant.now().toString());
    }

    @Override
    public void deleteNote(UUID applicationId, UUID noteId) {
        em.createNativeQuery("DELETE FROM application_notes WHERE id = ?1 AND application_id = ?2")
            .setParameter(1, noteId)
            .setParameter(2, applicationId)
            .executeUpdate();
    }
}
