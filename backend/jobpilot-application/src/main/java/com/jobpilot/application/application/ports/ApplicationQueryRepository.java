package com.jobpilot.application.application.ports;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ApplicationQueryRepository {
    List<Map<String, Object>> findNotesByApplicationId(UUID applicationId);
    List<Map<String, Object>> findAttachmentsByApplicationId(UUID applicationId);
    List<Map<String, Object>> findFollowUpsByApplicationId(UUID applicationId);
    List<Map<String, Object>> findTimelineByApplicationId(UUID applicationId);
    Map<String, Object> addNote(UUID applicationId, UUID userId, String content);
    Map<String, Object> addNote(UUID applicationId, UUID userId, String content, String category);
    void deleteNote(UUID applicationId, UUID noteId);
}
