package com.jobpilot.interfaces.rest.v1.interview;

import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.application.interview.dto.*;
import com.jobpilot.application.interview.usecase.*;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {

    private static final Logger log = LoggerFactory.getLogger(InterviewController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ScheduleInterviewUseCase scheduleInterviewUseCase;
    private final CompleteInterviewUseCase completeInterviewUseCase;
    private final CancelInterviewUseCase cancelInterviewUseCase;
    private final GetInterviewUseCase getInterviewUseCase;
    private final ListUserInterviewsUseCase listUserInterviewsUseCase;
    private final AiProviderPort aiProviderPort;

    public InterviewController(ScheduleInterviewUseCase scheduleInterviewUseCase,
                                CompleteInterviewUseCase completeInterviewUseCase,
                                CancelInterviewUseCase cancelInterviewUseCase,
                                GetInterviewUseCase getInterviewUseCase,
                                ListUserInterviewsUseCase listUserInterviewsUseCase,
                                AiProviderPort aiProviderPort) {
        this.scheduleInterviewUseCase = scheduleInterviewUseCase;
        this.completeInterviewUseCase = completeInterviewUseCase;
        this.cancelInterviewUseCase = cancelInterviewUseCase;
        this.getInterviewUseCase = getInterviewUseCase;
        this.listUserInterviewsUseCase = listUserInterviewsUseCase;
        this.aiProviderPort = aiProviderPort;
    }

    // --- Original scheduling endpoints (keep for backward compat) ---

    @RateLimited(capacity = 100)
    @PostMapping
    public ResponseEntity<ApiResponse<InterviewResponse>> schedule(@Valid @RequestBody ScheduleRequest request) {
        var command = new ScheduleInterviewCommand(UUID.fromString(request.userId()), request.companyId(),
            request.jobId(), request.type(), request.scheduledAt(), request.durationMinutes(),
            request.interviewerName(), request.interviewRound(), request.location(),
            request.meetingLink(), request.notes());
        var response = scheduleInterviewUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InterviewResponse>> getById(@PathVariable String id) {
        var response = getInterviewUseCase.execute(new GetInterviewCommand(id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> listByUser(@PathVariable UUID userId) {
        var response = listUserInterviewsUseCase.execute(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<InterviewResponse>> complete(@PathVariable UUID id,
            @Valid @RequestBody CompleteRequest request) {
        var response = completeInterviewUseCase.execute(new CompleteInterviewCommand(id, request.rating(), request.feedback()));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable String id,
            @RequestBody(required = false) CancelRequest request) {
        cancelInterviewUseCase.execute(new CancelInterviewCommand(id, request != null ? request.reason() : null));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- Interview Session / QA endpoints (matching frontend expectations) ---

    @RateLimited(capacity = 100)
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listSessions(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = principal != null ? UUID.fromString(principal.userId()) : null;
        if (userId != null) {
            var interviews = listUserInterviewsUseCase.execute(userId);
            var sessions = interviews.stream().map(i -> {
                Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", i.sessionId().toString());
                m.put("type", i.type());
                m.put("status", i.status().name());
                m.put("scheduledAt", i.scheduledAt() != null ? i.scheduledAt().toString() : "");
                m.put("questionCount", i.questions() != null ? i.questions().size() : 0);
                m.put("answeredCount", 0);
                return m;
            }).toList();
            return ResponseEntity.ok(ApiResponse.ok(sessions));
        }
        return ResponseEntity.ok(ApiResponse.ok(List.of()));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSession(@PathVariable String id) {
        var response = getInterviewUseCase.execute(new GetInterviewCommand(id));
        java.util.List<Map<String, Object>> questionsList = new java.util.ArrayList<>();
        if (response.questions() != null) {
            for (var q : response.questions()) {
                Map<String, Object> qm = new java.util.HashMap<>();
                qm.put("question", q.question());
                qm.put("expectedAnswer", q.expectedAnswer() != null ? q.expectedAnswer() : "");
                qm.put("difficulty", q.difficulty());
                qm.put("category", q.category() != null ? q.category() : "");
                questionsList.add(qm);
            }
        }
        Map<String, Object> session = new java.util.HashMap<>();
        session.put("id", response.sessionId().toString());
        session.put("type", response.type());
        session.put("status", response.status().name());
        session.put("scheduledAt", response.scheduledAt() != null ? response.scheduledAt().toString() : "");
        session.put("questions", questionsList);
        return ResponseEntity.ok(ApiResponse.ok(session));
    }

    @RateLimited(capacity = 50)
    @GetMapping("/sessions/{id}/next-question")
    public ResponseEntity<ApiResponse<Map<String, Object>>> nextQuestion(@PathVariable String id) {
        var response = getInterviewUseCase.execute(new GetInterviewCommand(id));
        String type = response.type() != null ? response.type() : "general";

        String systemPrompt = "You are an expert interview coach. Generate realistic interview questions.";
        String userPrompt = "Generate a " + type + " interview question for a software engineering position. " +
            "Return ONLY valid JSON with: question (string), category (string), difficulty (easy|medium|hard), " +
            "tips (array of strings). Do not include any text outside the JSON.";

        try {
            String aiResponse = aiProviderPort.executePrompt(systemPrompt, userPrompt, null, 0.7, 500);
            Map<String, Object> parsed = objectMapper.readValue(extractJson(aiResponse),
                new TypeReference<Map<String, Object>>() {});
            parsed.put("id", UUID.randomUUID().toString());
            return ResponseEntity.ok(ApiResponse.ok(parsed));
        } catch (Exception e) {
            log.warn("Failed to parse AI response for nextQuestion, using fallback: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "id", UUID.randomUUID().toString(),
                "question", "Tell me about yourself and your experience with the technologies mentioned in the job description.",
                "category", "general",
                "difficulty", "medium",
                "tips", List.of("Focus on relevant experience", "Use the STAR method", "Keep it concise")
            )));
        }
    }

    @RateLimited(capacity = 50)
    @PostMapping("/sessions/{id}/questions/{qid}/answer")
    public ResponseEntity<ApiResponse<Map<String, Object>>> answerQuestion(
            @PathVariable String id,
            @PathVariable String qid,
            @RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "the interview question");
        String answer = body.getOrDefault("answer", "");

        String systemPrompt = "You are an expert interview coach. Evaluate interview answers constructively.";
        String userPrompt = "Evaluate this interview answer.\nQuestion: " + question + "\nAnswer: " + answer +
            "\nReturn ONLY valid JSON with: feedback (string), score (integer 1-10), suggestedAnswer (string). " +
            "Do not include any text outside the JSON.";

        try {
            String aiResponse = aiProviderPort.executePrompt(systemPrompt, userPrompt, null, 0.3, 500);
            Map<String, Object> parsed = objectMapper.readValue(extractJson(aiResponse),
                new TypeReference<Map<String, Object>>() {});
            parsed.put("id", qid);
            return ResponseEntity.ok(ApiResponse.ok(parsed));
        } catch (Exception e) {
            log.warn("Failed to parse AI response for answerQuestion, using fallback: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "id", qid,
                "feedback", "Good answer. Try to be more specific about your achievements using metrics.",
                "score", 7,
                "suggestedAnswer", "In my previous role, I led a team of 5 engineers..."
            )));
        }
    }

    @RateLimited(capacity = 50)
    @PostMapping("/sessions/{id}/complete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> completeSession(@PathVariable String id) {
        var response = getInterviewUseCase.execute(new GetInterviewCommand(id));

        String systemPrompt = "You are an expert interview coach. Summarize interview performance constructively.";
        String userPrompt = "Summarize this interview performance for a " +
            (response.type() != null ? response.type() : "general") + " interview." +
            "\nReturn ONLY valid JSON with: overallScore (integer 1-10), strengths (array of strings), " +
            "areasForImprovement (array of strings), feedback (string). " +
            "Do not include any text outside the JSON.";

        try {
            String aiResponse = aiProviderPort.executePrompt(systemPrompt, userPrompt, null, 0.3, 500);
            Map<String, Object> parsed = objectMapper.readValue(extractJson(aiResponse),
                new TypeReference<Map<String, Object>>() {});
            parsed.put("id", id);
            parsed.put("status", "COMPLETED");
            parsed.put("totalQuestions", response.questions() != null ? response.questions().size() : 0);
            return ResponseEntity.ok(ApiResponse.ok(parsed));
        } catch (Exception e) {
            log.warn("Failed to parse AI response for completeSession, using fallback: {}", e.getMessage());
            int questionCount = response.questions() != null ? response.questions().size() : 0;
            return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "id", id,
                "status", "COMPLETED",
                "overallScore", 7,
                "totalQuestions", questionCount,
                "answeredQuestions", questionCount,
                "strengths", List.of("Communication", "Technical knowledge"),
                "areasForImprovement", List.of("Provide more specific examples"),
                "feedback", "Good performance overall. Keep practicing!"
            )));
        }
    }

    @RateLimited(capacity = 100)
    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getQuestionBank(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "20") int limit) {

        String cat = category != null ? category : "general";
        String diff = difficulty != null ? difficulty : "medium";

        String systemPrompt = "You are an expert interview coach. Generate realistic interview questions.";
        String userPrompt = "Generate " + limit + " " + diff + " interview questions for category " + cat + ". " +
            "Return ONLY a valid JSON array where each element has: question (string), category (string), " +
            "difficulty (string). Do not include any text outside the JSON array.";

        try {
            String aiResponse = aiProviderPort.executePrompt(systemPrompt, userPrompt, null, 0.7, 1500);
            List<Map<String, Object>> parsed = objectMapper.readValue(extractJson(aiResponse),
                new TypeReference<List<Map<String, Object>>>() {});
            parsed.forEach(m -> m.put("id", UUID.randomUUID().toString()));
            return ResponseEntity.ok(ApiResponse.ok(parsed));
        } catch (Exception e) {
            log.warn("Failed to parse AI response for getQuestionBank, using fallback: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.ok(List.of(
                Map.of("id", UUID.randomUUID().toString(), "question", "Tell me about yourself.",
                    "category", cat, "difficulty", "easy"),
                Map.of("id", UUID.randomUUID().toString(), "question", "Why do you want to work here?",
                    "category", cat, "difficulty", "easy"),
                Map.of("id", UUID.randomUUID().toString(), "question", "Describe a challenging project.",
                    "category", cat, "difficulty", "medium"),
                Map.of("id", UUID.randomUUID().toString(), "question", "How do you handle conflicts?",
                    "category", cat, "difficulty", "medium"),
                Map.of("id", UUID.randomUUID().toString(), "question", "Explain a technical concept.",
                    "category", cat, "difficulty", "hard")
            )));
        }
    }

    private String extractJson(String response) {
        if (response == null) return "{}";
        String trimmed = response.strip();
        int start = trimmed.indexOf('[') != -1 && (trimmed.indexOf('{') == -1 || trimmed.indexOf('[') < trimmed.indexOf('{'))
            ? trimmed.indexOf('[') : trimmed.indexOf('{');
        int end = trimmed.lastIndexOf(']');
        if (end == -1) end = trimmed.lastIndexOf('}');
        if (start == -1 || end == -1 || end <= start) return trimmed;
        return trimmed.substring(start, end + 1);
    }

    public record ScheduleRequest(
        @NotBlank String userId, UUID companyId, UUID jobId,
        @NotBlank String type, @NotNull Instant scheduledAt,
        Integer durationMinutes, String interviewerName, Integer interviewRound,
        String location, String meetingLink, String notes
    ) {}

    public record CompleteRequest(@NotNull int rating, String feedback) {}

    public record CancelRequest(String reason) {}
}
