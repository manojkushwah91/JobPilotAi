package com.jobpilot.application.application.ports;

import com.jobpilot.domain.application.Application;
import com.jobpilot.domain.application.ApplicationId;
import com.jobpilot.domain.identity.UserId;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository {
    Application save(Application application);
    Optional<Application> findById(ApplicationId id);
    List<Application> findByUserId(UserId userId);
    List<Application> findByUserIdAndStatus(UserId userId, String status);
    void delete(Application application);
}
