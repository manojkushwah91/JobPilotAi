package com.jobpilot.application.agent.service;

import com.jobpilot.application.agent.ports.AgentTaskRepository;
import com.jobpilot.domain.agent.AgentTask;
import com.jobpilot.domain.agent.AgentTaskId;
import com.jobpilot.domain.agent.TaskStatus;
import com.jobpilot.domain.agent.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentTaskServiceTest {

    @Mock
    private AgentTaskRepository taskRepository;

    private AgentTaskService taskService;

    private UUID testMissionId;
    private UUID testUserId;
    private AgentTask testTask;

    @BeforeEach
    void setUp() {
        taskService = new AgentTaskService(taskRepository);
        testMissionId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testTask = AgentTask.create(testMissionId, testUserId, TaskType.DISCOVER_JOBS, "Discover jobs");
    }

    @Test
    void createTask_shouldCreateTaskWithDefaults() {
        when(taskRepository.save(any(AgentTask.class))).thenReturn(testTask);

        var result = taskService.createTask(testMissionId, testUserId, TaskType.DISCOVER_JOBS, "Discover jobs");

        assertNotNull(result);
        assertEquals(TaskType.DISCOVER_JOBS, result.taskType());
        assertEquals(TaskStatus.PENDING, result.status());
        assertEquals(5, result.priority());
        assertEquals(0, result.retryCount());
        assertEquals(3, result.maxRetries());
        verify(taskRepository).save(any(AgentTask.class));
    }

    @Test
    void createTaskWithPriority_shouldCreateTaskWithCustomPriority() {
        when(taskRepository.save(any(AgentTask.class))).thenReturn(testTask);

        var result = taskService.createTaskWithPriority(
            testMissionId, testUserId, TaskType.DISCOVER_JOBS, "Discover jobs", 10);

        assertEquals(10, result.priority());
    }

    @Test
    void getTask_shouldReturnTaskWhenFound() {
        when(taskRepository.findById(any(AgentTaskId.class))).thenReturn(Optional.of(testTask));

        var result = taskService.getTask(testTask.taskId().value());

        assertNotNull(result);
        assertEquals(testTask.taskId(), result.taskId());
    }

    @Test
    void getTask_shouldThrowExceptionWhenNotFound() {
        when(taskRepository.findById(any(AgentTaskId.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> taskService.getTask(UUID.randomUUID()));
    }

    @Test
    void getPendingTasks_shouldReturnPendingTasks() {
        when(taskRepository.findPendingTasks(100)).thenReturn(List.of(testTask));

        var result = taskService.getPendingTasks();

        assertEquals(1, result.size());
    }

    @Test
    void startTask_shouldMarkTaskAsRunning() {
        when(taskRepository.findById(any(AgentTaskId.class))).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(AgentTask.class))).thenReturn(testTask);

        var result = taskService.startTask(testTask.taskId().value());

        assertEquals(TaskStatus.RUNNING, result.status());
        assertNotNull(result.startedAt());
    }

    @Test
    void completeTask_shouldMarkTaskAsCompleted() {
        when(taskRepository.findById(any(AgentTaskId.class))).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(AgentTask.class))).thenReturn(testTask);

        var result = taskService.completeTask(testTask.taskId().value(), Map.of("result", "success"));

        assertEquals(TaskStatus.COMPLETED, result.status());
        assertNotNull(result.completedAt());
    }

    @Test
    void failTask_shouldMarkTaskAsFailed() {
        when(taskRepository.findById(any(AgentTaskId.class))).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(AgentTask.class))).thenReturn(testTask);

        var result = taskService.failTask(testTask.taskId().value(), "Network error");

        assertEquals(TaskStatus.FAILED, result.status());
        assertEquals("Network error", result.errorMessage());
    }

    @Test
    void retryTask_shouldIncrementRetryCount() {
        when(taskRepository.findById(any(AgentTaskId.class))).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(AgentTask.class))).thenReturn(testTask);

        var result = taskService.retryTask(testTask.taskId().value());

        assertEquals(1, result.retryCount());
        assertEquals(TaskStatus.PENDING, result.status());
    }

    @Test
    void retryTask_shouldNotRetryWhenMaxRetriesReached() {
        testTask.incrementRetry();
        testTask.incrementRetry();
        when(taskRepository.findById(any(AgentTaskId.class))).thenReturn(Optional.of(testTask));

        var result = taskService.retryTask(testTask.taskId().value());

        assertEquals(3, result.retryCount());
    }
}
