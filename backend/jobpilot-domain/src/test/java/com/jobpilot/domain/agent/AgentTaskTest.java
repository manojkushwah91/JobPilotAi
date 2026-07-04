package com.jobpilot.domain.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AgentTaskTest {

    private UUID testMissionId;
    private UUID testUserId;
    private AgentTask testTask;

    @BeforeEach
    void setUp() {
        testMissionId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testTask = AgentTask.create(testMissionId, testUserId, TaskType.DISCOVER_JOBS, "Discover jobs");
    }

    @Test
    void create_shouldCreateTaskWithDefaults() {
        assertNotNull(testTask.taskId());
        assertEquals(testMissionId, testTask.missionId());
        assertEquals(testUserId, testTask.userId());
        assertEquals(TaskType.DISCOVER_JOBS, testTask.taskType());
        assertEquals(TaskStatus.PENDING, testTask.status());
        assertEquals(5, testTask.priority());
        assertEquals("Discover jobs", testTask.description());
        assertEquals(0, testTask.retryCount());
        assertEquals(3, testTask.maxRetries());
    }

    @Test
    void createWithPriority_shouldCreateTaskWithCustomPriority() {
        var task = AgentTask.createWithPriority(testMissionId, testUserId, TaskType.DISCOVER_JOBS, "High priority", 10);

        assertEquals(10, task.priority());
    }

    @Test
    void start_shouldTransitionToRunningStatus() {
        testTask.start();

        assertEquals(TaskStatus.RUNNING, testTask.status());
        assertNotNull(testTask.startedAt());
    }

    @Test
    void complete_shouldTransitionToCompletedStatus() {
        testTask.start();
        java.util.Map<String, Object> output = Map.of("result", "success", "jobsFound", 10);
        testTask.complete(output);

        assertEquals(TaskStatus.COMPLETED, testTask.status());
        assertEquals(output, testTask.output());
        assertNotNull(testTask.completedAt());
    }

    @Test
    void fail_shouldTransitionToFailedStatus() {
        testTask.start();
        testTask.fail("Network error");

        assertEquals(TaskStatus.FAILED, testTask.status());
        assertEquals("Network error", testTask.errorMessage());
        assertNotNull(testTask.completedAt());
    }

    @Test
    void canRetry_shouldReturnTrueWhenUnderMaxRetries() {
        assertTrue(testTask.canRetry());
    }

    @Test
    void canRetry_shouldReturnFalseWhenMaxRetriesReached() {
        testTask.incrementRetry();
        testTask.incrementRetry();
        testTask.incrementRetry();

        assertFalse(testTask.canRetry());
    }

    @Test
    void incrementRetry_shouldIncrementRetryCountAndResetStatus() {
        testTask.start();
        testTask.fail("Error");
        testTask.incrementRetry();

        assertEquals(1, testTask.retryCount());
        assertEquals(TaskStatus.PENDING, testTask.status());
    }

    @Test
    void reconstitute_shouldRecreateTaskFromData() {
        var task = AgentTask.reconstitute(
            testTask.taskId(),
            testMissionId,
            testUserId,
            TaskType.DISCOVER_JOBS,
            TaskStatus.RUNNING,
            10,
            "High priority task",
            Map.of("query", "Java"),
            Map.of("result", "success"),
            null,
            1, 3,
            Instant.now(), null, null,
            Instant.now(), Instant.now()
        );

        assertEquals(testTask.taskId(), task.taskId());
        assertEquals(10, task.priority());
        assertEquals(TaskStatus.RUNNING, task.status());
        assertEquals(1, task.retryCount());
    }
}
