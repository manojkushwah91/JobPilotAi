package com.jobpilot.application.agent.ports;

import com.jobpilot.domain.agent.AgentDecision;
import com.jobpilot.domain.agent.DecisionId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DecisionRepository {

    AgentDecision save(AgentDecision decision);

    Optional<AgentDecision> findById(DecisionId decisionId);

    List<AgentDecision> findByMissionId(UUID missionId);

    List<AgentDecision> findUnexecutedByMissionId(UUID missionId);
}
