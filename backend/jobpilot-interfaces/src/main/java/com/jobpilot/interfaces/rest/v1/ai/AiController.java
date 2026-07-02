package com.jobpilot.interfaces.rest.v1.ai;

import com.jobpilot.application.ai.dto.*;
import com.jobpilot.application.ai.ports.*;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiResumeScoringPort aiResumeScoringPort;
    private final AiSkillGapPort aiSkillGapPort;
    private final AiJobMatchPort aiJobMatchPort;

    public AiController(AiResumeScoringPort aiResumeScoringPort, AiSkillGapPort aiSkillGapPort, AiJobMatchPort aiJobMatchPort) {
        this.aiResumeScoringPort = aiResumeScoringPort;
        this.aiSkillGapPort = aiSkillGapPort;
        this.aiJobMatchPort = aiJobMatchPort;
    }

    @RateLimited(capacity = 5)
    @PostMapping("/resume/score")
    public ResponseEntity<ApiResponse<AiResumeScoreResponse>> scoreResume(@RequestBody AiResumeScoreRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(aiResumeScoringPort.scoreResume(request)));
    }

    @RateLimited(capacity = 5)
    @PostMapping("/resume/skill-gap")
    public ResponseEntity<ApiResponse<AiSkillGapResponse>> skillGap(@RequestBody AiSkillGapRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(aiSkillGapPort.analyzeSkillGap(request)));
    }

    @RateLimited(capacity = 5)
    @PostMapping("/job/match")
    public ResponseEntity<ApiResponse<AiJobMatchResponse>> matchJob(@RequestBody AiJobMatchRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(aiJobMatchPort.matchJob(request)));
    }
}
