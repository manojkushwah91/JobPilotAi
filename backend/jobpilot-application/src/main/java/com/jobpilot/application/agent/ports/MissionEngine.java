package com.jobpilot.application.agent.ports;

import com.jobpilot.domain.agent.Mission;

import java.util.Map;
import java.util.UUID;

public interface MissionEngine {

    void startMission(Mission mission);

    void pauseMission(UUID missionId);

    void resumeMission(UUID missionId);

    void stopMission(UUID missionId);

    Map<String, Object> getMissionStatus(UUID missionId);

    Map<String, Object> getAgentStatus();
}
