package com.jobpilot.application.automation.ports;

import com.jobpilot.domain.automation.AutomationSession;
import com.jobpilot.domain.automation.AutomationSessionId;
import com.jobpilot.domain.automation.AutomationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AutomationRepository {
    AutomationSession save(AutomationSession session);
    Optional<AutomationSession> findById(AutomationSessionId id);
    Page<AutomationSession> findByUserId(UUID userId, Pageable pageable);
    List<AutomationSession> findByStatus(AutomationStatus status, Pageable pageable);
}
