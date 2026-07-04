package com.jobpilot.application.agent.service;

import com.jobpilot.application.agent.ports.MissionRepository;
import com.jobpilot.domain.agent.Mission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MissionService {

    private static final Logger log = LoggerFactory.getLogger(MissionService.class);

    private final MissionRepository missionRepository;

    public MissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    public Mission createMission(UUID userId, String title, String targetRole,
                                  String targetLocation, Integer salaryMin, Integer salaryMax,
                                  List<String> preferredCompanies, List<String> avoidCompanies,
                                  List<String> preferredSkills, String experienceLevel,
                                  String employmentType, Integer dailyLimit, Integer deadlineDays) {
        var mission = Mission.create(userId, title, targetRole);
        missionRepository.save(mission);
        log.info("Created mission {} for user {}", mission.missionId(), userId);
        return mission;
    }

    public Mission getMission(UUID missionId) {
        var missionIdObj = com.jobpilot.domain.agent.MissionId.from(missionId);
        return missionRepository.findById(missionIdObj)
            .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
    }

    public List<Mission> getUserMissions(UUID userId) {
        return missionRepository.findByUserId(userId);
    }

    public Mission startMission(UUID missionId) {
        var mission = getMission(missionId);
        mission.start();
        missionRepository.save(mission);
        log.info("Started mission {}", missionId);
        return mission;
    }

    public Mission pauseMission(UUID missionId) {
        var mission = getMission(missionId);
        mission.pause();
        missionRepository.save(mission);
        log.info("Paused mission {}", missionId);
        return mission;
    }

    public Mission resumeMission(UUID missionId) {
        var mission = getMission(missionId);
        mission.resume();
        missionRepository.save(mission);
        log.info("Resumed mission {}", missionId);
        return mission;
    }

    public Mission cancelMission(UUID missionId) {
        var mission = getMission(missionId);
        mission.cancel();
        missionRepository.save(mission);
        log.info("Cancelled mission {}", missionId);
        return mission;
    }
}
