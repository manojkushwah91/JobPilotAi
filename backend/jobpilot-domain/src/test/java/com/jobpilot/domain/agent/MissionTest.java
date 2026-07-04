package com.jobpilot.domain.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MissionTest {

    private UUID testUserId;
    private Mission testMission;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testMission = Mission.create(testUserId, "Find Remote Java Jobs", "Java Backend Developer");
    }

    @Test
    void create_shouldCreateMissionWithDefaults() {
        assertNotNull(testMission.missionId());
        assertEquals(testUserId, testMission.userId());
        assertEquals("Find Remote Java Jobs", testMission.title());
        assertEquals("Java Backend Developer", testMission.targetRole());
        assertEquals(MissionStatus.CREATED, testMission.status());
        assertEquals(0, testMission.totalJobsFound());
        assertEquals(0, testMission.totalApplicationsSubmitted());
        assertEquals(20, testMission.dailyApplicationLimit());
        assertEquals(90, testMission.deadlineDays());
    }

    @Test
    void builderMethods_shouldSetProperties() {
        var mission = Mission.create(testUserId, "Test", "Developer")
            .withTargetLocation("Remote")
            .withSalaryRange(15, 25, "INR")
            .withPreferredCompanies(List.of("Google", "Microsoft"))
            .withAvoidCompanies(List.of("TCS"))
            .withPreferredSkills(List.of("Java", "Spring Boot"))
            .withExperienceLevel("MID")
            .withEmploymentType("FULL_TIME")
            .withDailyApplicationLimit(10)
            .withDeadlineDays(60);

        assertEquals("Remote", mission.targetLocation());
        assertEquals(15, mission.salaryMin());
        assertEquals(25, mission.salaryMax());
        assertEquals("INR", mission.currency());
        assertEquals(List.of("Google", "Microsoft"), mission.preferredCompanies());
        assertEquals(List.of("TCS"), mission.avoidCompanies());
        assertEquals(List.of("Java", "Spring Boot"), mission.preferredSkills());
        assertEquals("MID", mission.experienceLevel());
        assertEquals("FULL_TIME", mission.employmentType());
        assertEquals(10, mission.dailyApplicationLimit());
        assertEquals(60, mission.deadlineDays());
    }

    @Test
    void start_shouldTransitionToActiveStatus() {
        testMission.start();

        assertEquals(MissionStatus.ACTIVE, testMission.status());
        assertNotNull(testMission.startedAt());
        assertNotNull(testMission.deadlineAt());
    }

    @Test
    void start_shouldThrowExceptionWhenNotCreated() {
        testMission.start();

        assertThrows(IllegalStateException.class, () -> testMission.start());
    }

    @Test
    void pause_shouldTransitionToPausedStatus() {
        testMission.start();
        testMission.pause();

        assertEquals(MissionStatus.PAUSED, testMission.status());
    }

    @Test
    void pause_shouldThrowExceptionWhenNotActive() {
        assertThrows(IllegalStateException.class, () -> testMission.pause());
    }

    @Test
    void resume_shouldTransitionToActiveStatus() {
        testMission.start();
        testMission.pause();
        testMission.resume();

        assertEquals(MissionStatus.ACTIVE, testMission.status());
    }

    @Test
    void resume_shouldThrowExceptionWhenNotPaused() {
        assertThrows(IllegalStateException.class, () -> testMission.resume());
    }

    @Test
    void complete_shouldTransitionToCompletedStatus() {
        testMission.start();
        testMission.complete();

        assertEquals(MissionStatus.COMPLETED, testMission.status());
        assertNotNull(testMission.completedAt());
    }

    @Test
    void cancel_shouldTransitionToCancelledStatus() {
        testMission.start();
        testMission.cancel();

        assertEquals(MissionStatus.CANCELLED, testMission.status());
        assertNotNull(testMission.completedAt());
    }

    @Test
    void incrementJobsFound_shouldIncrementCounter() {
        testMission.incrementJobsFound();
        testMission.incrementJobsFound();

        assertEquals(2, testMission.totalJobsFound());
    }

    @Test
    void incrementApplicationsSubmitted_shouldIncrementCounter() {
        testMission.incrementApplicationsSubmitted();

        assertEquals(1, testMission.totalApplicationsSubmitted());
    }

    @Test
    void incrementRejected_shouldIncrementCounter() {
        testMission.incrementRejected();

        assertEquals(1, testMission.totalRejected());
    }

    @Test
    void incrementPending_shouldIncrementCounter() {
        testMission.incrementPending();

        assertEquals(1, testMission.totalPending());
    }

    @Test
    void hasReachedDailyLimit_shouldReturnTrueWhenLimitReached() {
        testMission.withDailyApplicationLimit(5);
        for (int i = 0; i < 5; i++) {
            testMission.incrementApplicationsSubmitted();
        }

        assertTrue(testMission.hasReachedDailyLimit());
    }

    @Test
    void hasReachedDailyLimit_shouldReturnFalseWhenUnderLimit() {
        testMission.withDailyApplicationLimit(20);
        testMission.incrementApplicationsSubmitted();

        assertFalse(testMission.hasReachedDailyLimit());
    }

    @Test
    void isExpired_shouldReturnFalseWhenDeadlineNotPassed() {
        testMission.withDeadlineDays(90);
        testMission.start();

        assertFalse(testMission.isExpired());
    }

    @Test
    void shouldStop_shouldReturnTrueWhenDailyLimitReached() {
        testMission.start();
        testMission.withDailyApplicationLimit(1);
        testMission.incrementApplicationsSubmitted();

        assertTrue(testMission.shouldStop());
    }

    @Test
    void shouldStop_shouldReturnFalseWhenActiveAndUnderLimits() {
        testMission.start();

        assertFalse(testMission.shouldStop());
    }

    @Test
    void reconstitute_shouldRecreateMissionFromData() {
        var mission = Mission.reconstitute(
            testMission.missionId(),
            testUserId,
            "Test Mission",
            "Developer",
            "Remote",
            15, 25, "INR",
            List.of("Google"),
            List.of("TCS"),
            List.of("Java"),
            "MID",
            "FULL_TIME",
            20, 90,
            MissionStatus.ACTIVE,
            Instant.now(), null, Instant.now().plus(90, ChronoUnit.DAYS),
            5, 3, 1, 1,
            null,
            Instant.now(), Instant.now()
        );

        assertEquals(testMission.missionId(), mission.missionId());
        assertEquals("Remote", mission.targetLocation());
        assertEquals(15, mission.salaryMin());
        assertEquals(MissionStatus.ACTIVE, mission.status());
    }
}
