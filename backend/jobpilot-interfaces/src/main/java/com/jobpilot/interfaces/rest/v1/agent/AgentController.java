package com.jobpilot.interfaces.rest.v1.agent;

import com.jobpilot.application.agent.service.AgentTaskService;
import com.jobpilot.application.agent.service.MissionService;
import com.jobpilot.domain.agent.Mission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final MissionService missionService;
    private final AgentTaskService taskService;

    public AgentController(MissionService missionService, AgentTaskService taskService) {
        this.missionService = missionService;
        this.taskService = taskService;
    }

    @PostMapping("/missions")
    public ResponseEntity<MissionResponse> createMission(@RequestBody CreateMissionRequest request) {
        var mission = missionService.createMission(
            UUID.fromString(request.userId()),
            request.title(),
            request.targetRole(),
            request.targetLocation(),
            request.salaryMin(),
            request.salaryMax(),
            request.preferredCompanies(),
            request.avoidCompanies(),
            request.preferredSkills(),
            request.experienceLevel(),
            request.employmentType(),
            request.dailyLimit(),
            request.deadlineDays()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(MissionResponse.from(mission));
    }

    @GetMapping("/missions/{missionId}")
    public ResponseEntity<MissionResponse> getMission(@PathVariable UUID missionId) {
        var mission = missionService.getMission(missionId);
        return ResponseEntity.ok(MissionResponse.from(mission));
    }

    @GetMapping("/missions/user/{userId}")
    public ResponseEntity<?> getUserMissions(@PathVariable UUID userId) {
        var missions = missionService.getUserMissions(userId);
        return ResponseEntity.ok(missions.stream().map(MissionResponse::from).toList());
    }

    @PostMapping("/missions/{missionId}/start")
    public ResponseEntity<MissionResponse> startMission(@PathVariable UUID missionId) {
        var mission = missionService.startMission(missionId);
        return ResponseEntity.ok(MissionResponse.from(mission));
    }

    @PostMapping("/missions/{missionId}/pause")
    public ResponseEntity<MissionResponse> pauseMission(@PathVariable UUID missionId) {
        var mission = missionService.pauseMission(missionId);
        return ResponseEntity.ok(MissionResponse.from(mission));
    }

    @PostMapping("/missions/{missionId}/resume")
    public ResponseEntity<MissionResponse> resumeMission(@PathVariable UUID missionId) {
        var mission = missionService.resumeMission(missionId);
        return ResponseEntity.ok(MissionResponse.from(mission));
    }

    @PostMapping("/missions/{missionId}/cancel")
    public ResponseEntity<MissionResponse> cancelMission(@PathVariable UUID missionId) {
        var mission = missionService.cancelMission(missionId);
        return ResponseEntity.ok(MissionResponse.from(mission));
    }

    @GetMapping("/tasks/pending")
    public ResponseEntity<?> getPendingTasks() {
        var tasks = taskService.getPendingTasks();
        return ResponseEntity.ok(tasks.stream().map(TaskResponse::from).toList());
    }

    @PostMapping("/tasks/{taskId}/start")
    public ResponseEntity<TaskResponse> startTask(@PathVariable UUID taskId) {
        var task = taskService.startTask(taskId);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable UUID taskId,
                                                      @RequestBody Map<String, Object> output) {
        var task = taskService.completeTask(taskId, output);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PostMapping("/tasks/{taskId}/fail")
    public ResponseEntity<TaskResponse> failTask(@PathVariable UUID taskId,
                                                  @RequestBody Map<String, String> request) {
        var task = taskService.failTask(taskId, request.get("error"));
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAgentStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "running",
            "version", "2.0.0"
        ));
    }
}
