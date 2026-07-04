package com.jobpilot.domain.agent;

import java.util.Map;
import java.util.UUID;

public interface AgentRuntime {

    void startMission(UUID missionId);

    void pauseMission(UUID missionId);

    void resumeMission(UUID missionId);

    void stopMission(UUID missionId);

    AgentTask executeTask(AgentTask task);

    AgentDecision makeDecision(AgentDecision decision);

    AgentObservation observe(AgentObservation observation);

    void storeMemory(AgentMemory memory);

    AgentMemory recallMemory(UUID userId, MemoryType type, String key);

    Map<String, Object> getMissionStatus(UUID missionId);

    Map<String, Object> getAgentStatus();
}
