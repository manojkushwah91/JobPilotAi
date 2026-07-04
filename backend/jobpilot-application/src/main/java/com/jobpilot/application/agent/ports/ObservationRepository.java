package com.jobpilot.application.agent.ports;

import com.jobpilot.domain.agent.AgentObservation;
import com.jobpilot.domain.agent.ObservationId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ObservationRepository {

    AgentObservation save(AgentObservation observation);

    Optional<AgentObservation> findById(ObservationId observationId);

    List<AgentObservation> findByMissionId(UUID missionId);

    List<AgentObservation> findRecentByMissionId(UUID missionId, int limit);
}
