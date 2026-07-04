package com.jobpilot.application.agent.ports;

import com.jobpilot.domain.agent.Mission;
import com.jobpilot.domain.agent.MissionId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissionRepository {

    Mission save(Mission mission);

    Optional<Mission> findById(MissionId missionId);

    List<Mission> findByUserId(UUID userId);

    List<Mission> findByStatus(com.jobpilot.domain.agent.MissionStatus status);

    void delete(MissionId missionId);
}
