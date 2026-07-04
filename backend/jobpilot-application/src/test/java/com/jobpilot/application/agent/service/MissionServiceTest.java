package com.jobpilot.application.agent.service;

import com.jobpilot.application.agent.ports.MissionRepository;
import com.jobpilot.domain.agent.Mission;
import com.jobpilot.domain.agent.MissionId;
import com.jobpilot.domain.agent.MissionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

    @Mock
    private MissionRepository missionRepository;

    private MissionService missionService;

    private UUID testUserId;
    private Mission testMission;

    @BeforeEach
    void setUp() {
        missionService = new MissionService(missionRepository);
        testUserId = UUID.randomUUID();
        testMission = Mission.create(testUserId, "Test Mission", "Java Developer");
    }

    @Test
    void createMission_shouldCreateMissionWithAllParameters() {
        when(missionRepository.save(any(Mission.class))).thenReturn(testMission);

        var result = missionService.createMission(
            testUserId,
            "Find Remote Java Jobs",
            "Java Backend Developer",
            "Remote",
            15, 25,
            List.of("Google", "Microsoft"),
            List.of("TCS", "Infosys"),
            List.of("Java", "Spring Boot"),
            "MID",
            "FULL_TIME",
            20, 90
        );

        assertNotNull(result);
        assertEquals("Find Remote Java Jobs", result.title());
        assertEquals("Java Backend Developer", result.targetRole());
        assertEquals("Remote", result.targetLocation());
        assertEquals(15, result.salaryMin());
        assertEquals(25, result.salaryMax());
        assertEquals(List.of("Google", "Microsoft"), result.preferredCompanies());
        assertEquals(List.of("TCS", "Infosys"), result.avoidCompanies());
        assertEquals(List.of("Java", "Spring Boot"), result.preferredSkills());
        assertEquals("MID", result.experienceLevel());
        assertEquals("FULL_TIME", result.employmentType());
        assertEquals(20, result.dailyApplicationLimit());
        assertEquals(90, result.deadlineDays());
        assertEquals(MissionStatus.CREATED, result.status());

        verify(missionRepository).save(any(Mission.class));
    }

    @Test
    void getMission_shouldReturnMissionWhenFound() {
        when(missionRepository.findById(any(MissionId.class))).thenReturn(Optional.of(testMission));

        var result = missionService.getMission(testMission.missionId().value());

        assertNotNull(result);
        assertEquals(testMission.missionId(), result.missionId());
        verify(missionRepository).findById(any(MissionId.class));
    }

    @Test
    void getMission_shouldThrowExceptionWhenNotFound() {
        when(missionRepository.findById(any(MissionId.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> missionService.getMission(UUID.randomUUID()));
    }

    @Test
    void getUserMissions_shouldReturnListOfMissions() {
        when(missionRepository.findByUserId(testUserId)).thenReturn(List.of(testMission));

        var result = missionService.getUserMissions(testUserId);

        assertEquals(1, result.size());
        assertEquals(testMission.missionId(), result.get(0).missionId());
    }

    @Test
    void startMission_shouldStartMissionFromCreatedStatus() {
        when(missionRepository.findById(any(MissionId.class))).thenReturn(Optional.of(testMission));
        when(missionRepository.save(any(Mission.class))).thenReturn(testMission);

        var result = missionService.startMission(testMission.missionId().value());

        assertEquals(MissionStatus.ACTIVE, result.status());
        assertNotNull(result.startedAt());
        verify(missionRepository).save(any(Mission.class));
    }

    @Test
    void startMission_shouldThrowExceptionWhenNotCreated() {
        testMission.start();
        when(missionRepository.findById(any(MissionId.class))).thenReturn(Optional.of(testMission));

        assertThrows(IllegalStateException.class,
            () -> missionService.startMission(testMission.missionId().value()));
    }

    @Test
    void pauseMission_shouldPauseActiveMission() {
        testMission.start();
        when(missionRepository.findById(any(MissionId.class))).thenReturn(Optional.of(testMission));
        when(missionRepository.save(any(Mission.class))).thenReturn(testMission);

        var result = missionService.pauseMission(testMission.missionId().value());

        assertEquals(MissionStatus.PAUSED, result.status());
    }

    @Test
    void pauseMission_shouldThrowExceptionWhenNotActive() {
        when(missionRepository.findById(any(MissionId.class))).thenReturn(Optional.of(testMission));

        assertThrows(IllegalStateException.class,
            () -> missionService.pauseMission(testMission.missionId().value()));
    }

    @Test
    void resumeMission_shouldResumePausedMission() {
        testMission.start();
        testMission.pause();
        when(missionRepository.findById(any(MissionId.class))).thenReturn(Optional.of(testMission));
        when(missionRepository.save(any(Mission.class))).thenReturn(testMission);

        var result = missionService.resumeMission(testMission.missionId().value());

        assertEquals(MissionStatus.ACTIVE, result.status());
    }

    @Test
    void cancelMission_shouldCancelMission() {
        when(missionRepository.findById(any(MissionId.class))).thenReturn(Optional.of(testMission));
        when(missionRepository.save(any(Mission.class))).thenReturn(testMission);

        var result = missionService.cancelMission(testMission.missionId().value());

        assertEquals(MissionStatus.CANCELLED, result.status());
        assertNotNull(result.completedAt());
    }
}
