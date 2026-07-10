package com.jobpilot.application.agent.service;

import com.jobpilot.application.agent.ports.*;
import com.jobpilot.domain.agent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class CareerAgentBrain {

    private static final Logger log = LoggerFactory.getLogger(CareerAgentBrain.class);

    private final AgentMemoryService memoryService;
    private final CandidateProfileRepository profileRepository;
    private final AiProviderPort aiProvider;
    private final ToolRegistry toolRegistry;
    private final ObservationRepository observationRepository;
    private final DecisionRepository decisionRepository;

    private final Map<UUID, CareerAgentState> agentStates = new HashMap<>();
    private final Map<UUID, List<AgentEpisode>> episodes = new HashMap<>();
    private final Map<UUID, List<AgentReflection>> reflections = new HashMap<>();
    private final Map<UUID, List<WeeklyReview>> weeklyReviews = new HashMap<>();

    private static final String REASONING_MODEL = "llama3.2";
    private static final double TEMPERATURE = 0.3;
    private static final int MAX_TOKENS = 2048;

    public CareerAgentBrain(AgentMemoryService memoryService,
                             CandidateProfileRepository profileRepository,
                             AiProviderPort aiProvider,
                             ToolRegistry toolRegistry,
                             ObservationRepository observationRepository,
                             DecisionRepository decisionRepository) {
        this.memoryService = memoryService;
        this.profileRepository = profileRepository;
        this.aiProvider = aiProvider;
        this.toolRegistry = toolRegistry;
        this.observationRepository = observationRepository;
        this.decisionRepository = decisionRepository;
    }

    // ────────────────────────────────────────────────
    //  PUBLIC API — called by DefaultAgentRuntime
    // ────────────────────────────────────────────────

    public CareerAgentState getOrCreateState(UUID userId) {
        return agentStates.computeIfAbsent(userId, id -> {
            var profile = profileRepository.findByUserId(id);
            var identity = buildIdentity(id, profile.orElse(null));
            return new CareerAgentState(id, identity);
        });
    }

    public ThinkingResult think(UUID userId, Mission mission, List<AgentObservation> observations) {
        var state = getOrCreateState(userId);
        var context = buildThinkingContext(state, mission, observations);
        var prompt = buildThinkPrompt(context);
        return executeThinking(prompt, state, mission);
    }

    public AgentPlan plan(UUID userId, Mission mission, ThinkingResult thinking) {
        var state = getOrCreateState(userId);
        var context = buildPlanningContext(state, mission, thinking);
        var prompt = buildPlanPrompt(context);
        return executePlanning(prompt, mission.missionId().value(), state);
    }

    public AgentReflection reflect(UUID userId, AgentTask task, Map<String, Object> result) {
        var state = getOrCreateState(userId);
        var context = buildReflectionContext(task, result);
        var prompt = buildReflectPrompt(context);
        var reflection = executeReflection(prompt, userId, task);
        storeReflection(userId, reflection);
        storeEpisode(userId, task, reflection);
        return reflection;
    }

    public void updateAgentSettings(UUID userId, String preferredLocation, Integer salaryMin, Integer salaryMax,
                                     List<String> preferredCompanies, List<String> avoidCompanies,
                                     String employmentType, String workAuthorization, String careerGoal,
                                     List<String> certifications, List<String> projects) {
        var state = getOrCreateState(userId);
        var identity = state.identity();
        if (preferredLocation != null) identity.preferredLocation(preferredLocation);
        if (salaryMin != null) identity.salaryMin(salaryMin);
        if (salaryMax != null) identity.salaryMax(salaryMax);
        if (preferredCompanies != null) identity.preferredCompanies(preferredCompanies);
        if (avoidCompanies != null) identity.avoidCompanies(avoidCompanies);
        if (employmentType != null) identity.employmentType(employmentType);
        if (workAuthorization != null) identity.workAuthorization(workAuthorization);
        if (careerGoal != null) identity.careerGoal(careerGoal);
        if (certifications != null) identity.certifications(certifications);
        if (projects != null) identity.projects(projects);
    }

    public Map<String, Object> getCareerHealth(UUID userId) {
        var state = agentStates.get(userId);
        if (state == null) return Map.of("status", "insufficient_data", "message", "No agent state available.");

        var totalApps = state.totalApplicationsSubmitted();
        var totalInterviews = state.totalInterviewsScheduled();
        var failures = state.consecutiveFailures();

        if (totalApps + totalInterviews + failures == 0) {
            return Map.of("status", "insufficient_data", "message", "Not enough data to calculate career health.");
        }

        var appScore = Math.min(40, totalApps * 4);
        var interviewScore = Math.min(30, totalInterviews * 10);
        var failurePenalty = Math.min(20, failures * 5);
        var baseScore = 30;
        var score = Math.max(0, Math.min(100, baseScore + appScore + interviewScore - failurePenalty));

        return Map.of(
            "status", "available",
            "score", score,
            "breakdown", Map.of(
                "applicationVolume", totalApps,
                "applicationScore", appScore,
                "interviewScore", interviewScore,
                "failurePenalty", failurePenalty,
                "baseScore", baseScore
            )
        );
    }

    public List<Map<String, Object>> getRecommendations(UUID userId) {
        var state = agentStates.get(userId);
        if (state == null) return List.of();

        var reviews = weeklyReviews.getOrDefault(userId, List.of());
        if (reviews.isEmpty()) return List.of();

        var latest = reviews.get(reviews.size() - 1);
        if (latest.recommendations().isEmpty()) return List.of();

        return latest.recommendations().stream()
            .map(r -> {
                var m = new java.util.LinkedHashMap<String, Object>();
                m.put("title", r.length() > 60 ? r.substring(0, 57) + "..." : r);
                m.put("description", r);
                m.put("reason", "Based on your weekly review and recent agent reflections.");
                m.put("confidence", latest.metrics().getOrDefault("avgMatchScore", 0.0).intValue());
                m.put("expectedImpact", latest.metrics().getOrDefault("successRate", 0.0) > 0.5 ? "High" : "Medium");
                return m;
            })
            .limit(5)
            .map(m -> (Map<String, Object>) m)
            .toList();
    }

    public List<Map<String, Object>> getRequiresAttention(UUID userId) {
        var items = new java.util.ArrayList<Map<String, Object>>();

        var recentEps = getRecentEpisodes(userId, 50);
        for (var ep : recentEps) {
            var ctxContainsCaptcha = ep.context().values().stream()
                .anyMatch(v -> v instanceof String s && s.toLowerCase().contains("captcha"));
            if (ctxContainsCaptcha) {
                items.add(Map.of(
                    "type", "captcha",
                    "message", "CAPTCHA detected — manual intervention required.",
                    "detail", ep.narrative(),
                    "timestamp", ep.occurredAt().toString()
                ));
            }
            if (!ep.success() && ep.lessons() != null && ep.lessons().stream().anyMatch(l -> l.contains("interview"))) {
                items.add(Map.of(
                    "type", "interview",
                    "message", "Interview invitation detected.",
                    "detail", ep.narrative(),
                    "timestamp", ep.occurredAt().toString()
                ));
            }
        }

        // Check for consecutive failures
        var state = agentStates.get(userId);
        if (state != null && state.consecutiveFailures() >= 3) {
            items.add(Map.of(
                "type", "failure_warning",
                "message", state.consecutiveFailures() + " consecutive application failures.",
                "detail", "The AI may need adjusted targeting or resume improvements.",
                "timestamp", Instant.now().toString()
            ));
        }

        return items;
    }

    public Optional<WeeklyReview> weeklyReview(UUID userId) {
        var state = agentStates.get(userId);
        if (state == null) return Optional.empty();

        var lastReview = state.lastWeeklyReviewAt();
        var daysSince = ChronoUnit.DAYS.between(lastReview, java.time.Instant.now());
        if (daysSince < 7) return Optional.empty();

        var context = buildWeeklyReviewContext(userId, state);
        var prompt = buildWeeklyReviewPrompt(context);
        var review = executeWeeklyReview(prompt, userId);
        storeWeeklyReview(userId, review);
        state.markWeeklyReviewDone();
        return Optional.of(review);
    }

    public String generateBriefing(UUID userId) {
        var state = agentStates.get(userId);
        if (state == null) return "No agent state available.";

        var recentEpisodes = getRecentEpisodes(userId, 20);
        var recentReflections = getRecentReflections(userId, 10);

        var context = new StringBuilder();
        context.append("=== USER IDENTITY ===\n");
        context.append(state.identity().toContextBlock()).append("\n\n");

        context.append("=== CURRENT PLAN ===\n");
        context.append(state.currentPlan().toPromptContext()).append("\n\n");

        context.append("=== RECENT ACTIVITY ===\n");
        for (var ep : recentEpisodes) {
            context.append(ep.toSummary()).append("\n");
        }

        context.append("=== RECENT REFLECTIONS ===\n");
        for (var ref : recentReflections) {
            context.append(ref.toPromptContext()).append("\n---\n");
        }

        var userPrompt = """
            You are an AI career agent writing a daily briefing for your user.

            Based on the following information, write a warm, personal briefing that sounds like it's from a dedicated AI employee.

            Include:
            - A greeting
            - What happened recently (applications submitted, interviews found)
            - What you recommend the user focus on today
            - Any insights from recent reflections

            Write 3-4 paragraphs. Be helpful and specific.

            Context:
            %s
            """.formatted(context.toString());

        try {
            return aiProvider.executePrompt(
                "You are the user's personal AI career agent. Write briefings that are warm, specific, and actionable.",
                userPrompt,
                REASONING_MODEL,
                0.4,
                1024
            );
        } catch (Exception e) {
            log.warn("Briefing generation failed: {}", e.getMessage());
            return "Good morning! Your agent has been working. Check your dashboard for the latest updates.";
        }
    }

    // ────────────────────────────────────────────────
    //  THINKING (Layer 3 + 4)
    // ────────────────────────────────────────────────

    private String buildThinkingContext(CareerAgentState state, Mission mission, List<AgentObservation> observations) {
        var sb = new StringBuilder();
        sb.append("=== USER IDENTITY ===\n");
        sb.append(state.identity().toContextBlock()).append("\n\n");

        sb.append("=== MISSION ===\n");
        sb.append("Target Role: ").append(mission.targetRole()).append("\n");
        sb.append("Target Location: ").append(mission.targetLocation()).append("\n");
        sb.append("Salary Range: $").append(mission.salaryMin()).append(" - $").append(mission.salaryMax()).append("\n");
        sb.append("Daily Application Limit: ").append(mission.dailyApplicationLimit()).append("\n");
        sb.append("Applications Submitted: ").append(mission.totalApplicationsSubmitted()).append("\n");
        sb.append("Status: ").append(mission.status()).append("\n\n");

        sb.append("=== OBSERVATIONS ===\n");
        for (var obs : observations) {
            sb.append("- [").append(obs.type()).append("] ").append(obs.description()).append("\n");
        }
        sb.append("\n");

        var memories = memoryService.getAllMemories(state.userId());
        if (!memories.isEmpty()) {
            sb.append("=== MEMORIES ===\n");
            for (var m : memories) {
                sb.append("- ").append(m.memoryType()).append(": ").append(m.memoryKey())
                  .append(" = ").append(m.value()).append("\n");
            }
            sb.append("\n");
        }

        var userEpisodes = episodes.getOrDefault(state.userId(), List.of());
        if (!userEpisodes.isEmpty()) {
            sb.append("=== RECENT EXPERIENCES (last 5) ===\n");
            var recent = userEpisodes.subList(Math.max(0, userEpisodes.size() - 5), userEpisodes.size());
            for (var ep : recent) {
                sb.append(ep.toSummary()).append("\n");
            }
        }

        return sb.toString();
    }

    private String buildThinkPrompt(String context) {
        return """
            You are a career agent AI — a dedicated employee managing a user's job search.

            Read the current state below and think carefully about what to do next.

            %s

            Think step by step:
            1. What is the current state of the mission?
            2. Are there pending tasks that need execution?
            3. Should we discover new jobs? Apply to existing ones? Tailor resumes?
            4. Are there any problems (failures, limits reached) we need to handle?
            5. Should we notify the user about anything?
            6. What is the single most important action to take right now?

            After thinking, output your decision as JSON only (no other text):
            {
              "shouldAct": true/false,
              "reasoning": "your step-by-step reasoning",
              "nextAction": "SEARCH_JOBS" | "APPLY_TO_JOBS" | "TAILOR_RESUME" | "NOTIFY_USER" | "WAIT" | "PREPARE_INTERVIEW" | "CHECK_EMAIL" | "REFLECT",
              "priority": 1-10,
              "description": "what specifically to do",
              "requiresNotification": true/false,
              "context": {}
            }
            """.formatted(context);
    }

    private ThinkingResult executeThinking(String prompt, CareerAgentState state, Mission mission) {
        try {
            var response = aiProvider.executePrompt(
                "You are a precise career agent AI. Output only valid JSON.",
                prompt,
                REASONING_MODEL,
                TEMPERATURE,
                MAX_TOKENS
            );

            var json = extractJson(response);
            var shouldAct = "true".equals(getJsonValue(json, "shouldAct"));
            var reasoning = getJsonValue(json, "reasoning");
            var nextAction = getJsonValue(json, "nextAction", "WAIT");
            var priority = parseInt(getJsonValue(json, "priority", "5"));
            var description = getJsonValue(json, "description", "");
            var requiresNotification = "true".equals(getJsonValue(json, "requiresNotification"));

            var decision = AgentDecision.create(
                mission.missionId().value(),
                mapToDecisionType(nextAction),
                reasoning
            );
            decisionRepository.save(decision);

            log.info("Agent thinking: {} (priority={}, action={}) - {}", reasoning, priority, nextAction, description);

            return new ThinkingResult(shouldAct, reasoning, nextAction, priority, description, requiresNotification, decision);

        } catch (Exception e) {
            log.warn("Thinking failed, falling back: {}", e.getMessage());
            return fallbackThinking(mission);
        }
    }

    private ThinkingResult fallbackThinking(Mission mission) {
        if (!mission.hasReachedDailyLimit()) {
            return new ThinkingResult(true, "Fallback: daily limit not reached, continue searching",
                "SEARCH_JOBS", 5, "Search for new job matches", false, null);
        }
        return new ThinkingResult(false, "Fallback: daily limit reached, waiting",
            "WAIT", 1, "Waiting for daily limit reset", false, null);
    }

    private DecisionType mapToDecisionType(String action) {
        return switch (action.toUpperCase()) {
            case "SEARCH_JOBS" -> DecisionType.UPDATE_STRATEGY;
            case "APPLY_TO_JOBS" -> DecisionType.APPLY_TO_JOB;
            case "TAILOR_RESUME" -> DecisionType.TAILOR_RESUME;
            case "NOTIFY_USER" -> DecisionType.UPDATE_STRATEGY;
            case "PREPARE_INTERVIEW" -> DecisionType.UPDATE_STRATEGY;
            case "CHECK_EMAIL" -> DecisionType.UPDATE_STRATEGY;
            case "REFLECT" -> DecisionType.UPDATE_STRATEGY;
            default -> DecisionType.UPDATE_STRATEGY;
        };
    }

    // ────────────────────────────────────────────────
    //  PLANNING (Layer 4)
    // ────────────────────────────────────────────────

    private String buildPlanningContext(CareerAgentState state, Mission mission, ThinkingResult thinking) {
        var sb = new StringBuilder();

        sb.append("Current plan:\n");
        sb.append(state.currentPlan().toPromptContext()).append("\n\n");

        sb.append("Latest thinking:\n");
        sb.append("  Decision: ").append(thinking.nextAction()).append("\n");
        sb.append("  Reasoning: ").append(thinking.reasoning()).append("\n");
        sb.append("  Priority: ").append(thinking.priority()).append("\n\n");

        var userEpisodes = episodes.getOrDefault(state.userId(), List.of());
        if (!userEpisodes.isEmpty()) {
            sb.append("Recent reflections (last 3):\n");
            var recent = userEpisodes.subList(Math.max(0, userEpisodes.size() - 3), userEpisodes.size());
            for (var ep : recent) {
                sb.append("- [").append(ep.success() ? "OK" : "FAIL").append("] ")
                  .append(ep.title()).append(": ").append(ep.narrative()).append("\n");
            }
        }

        return sb.toString();
    }

    private String buildPlanPrompt(String context) {
        return """
            You are a career agent planning the user's job search for the next few days.

            Given the current state and latest decision, create a concrete plan.

            %s

            Today is: %s

            Output a plan as JSON:
            {
              "strategy": "brief description of your overall strategy",
              "actions": [
                {
                  "actionType": "SEARCH_JOBS" | "APPLY_TO_JOBS" | "TAILOR_RESUME" | "NOTIFY_USER" | "PREPARE_INTERVIEW" | "CHECK_EMAIL",
                  "description": "what to do",
                  "reasoning": "why this action",
                  "priority": 1-10,
                  "scheduledDay": 0 (today), 1 (tomorrow), etc.
                }
              ]
            }

            Limit to 5 actions. Order by priority.
            """.formatted(context, LocalDate.now());
    }

    private AgentPlan executePlanning(String prompt, UUID missionId, CareerAgentState state) {
        try {
            var response = aiProvider.executePrompt(
                "You are a precise career agent planner. Output only valid JSON arrays and objects.",
                prompt,
                REASONING_MODEL,
                0.2,
                2048
            );

            var json = extractJson(response);
            var strategy = getJsonValue(json, "strategy", "Continue job search");
            var actionsJson = extractJsonArray(json, "actions");

            List<AgentPlan.PlannedAction> actions = new ArrayList<>();
            if (actionsJson != null) {
                for (var actionJson : actionsJson) {
                    var type = getJsonValue(actionJson, "actionType", "SEARCH_JOBS");
                    var desc = getJsonValue(actionJson, "description", "");
                    var reason = getJsonValue(actionJson, "reasoning", "");
                    var priority = parseInt(getJsonValue(actionJson, "priority", "5"));
                    var dayOffset = parseInt(getJsonValue(actionJson, "scheduledDay", "0"));
                    var scheduled = LocalDate.now().plusDays(dayOffset);
                    actions.add(new AgentPlan.PlannedAction(type, desc, reason, priority, scheduled));
                }
            }

            var horizon = LocalDate.now().plusDays(3);
            var plan = new AgentPlan(missionId, strategy, actions, horizon);
            state.updatePlan(plan);
            return plan;

        } catch (Exception e) {
            log.warn("Planning failed: {}", e.getMessage());
            var plan = AgentPlan.empty();
            state.updatePlan(plan);
            return plan;
        }
    }

    // ────────────────────────────────────────────────
    //  REFLECTION (Layer 6)
    // ────────────────────────────────────────────────

    private String buildReflectionContext(AgentTask task, Map<String, Object> result) {
        var sb = new StringBuilder();
        sb.append("Task type: ").append(task.taskType()).append("\n");
        sb.append("Description: ").append(task.description()).append("\n");
        sb.append("Status: ").append(task.status()).append("\n");
        sb.append("Input: ").append(task.input() != null ? task.input() : "none").append("\n");
        sb.append("Output: ").append(result).append("\n");
        if (task.errorMessage() != null) {
            sb.append("Error: ").append(task.errorMessage()).append("\n");
        }
        return sb.toString();
    }

    private String buildReflectPrompt(String context) {
        return """
            Reflect on this completed task.

            %s

            Output JSON only:
            {
              "narrative": "what happened in 1-2 sentences",
              "success": true/false,
              "why": "why it succeeded or failed",
              "lessons": ["lesson 1", "lesson 2"],
              "newKnowledge": "any new knowledge to remember (or null)"
            }
            """.formatted(context);
    }

    private AgentReflection executeReflection(String prompt, UUID userId, AgentTask task) {
        try {
            var response = aiProvider.executePrompt(
                "You are a reflective AI agent. Output only valid JSON.",
                prompt,
                REASONING_MODEL,
                0.2,
                1024
            );

            var json = extractJson(response);
            var narrative = getJsonValue(json, "narrative", "Task completed");
            var success = "true".equals(getJsonValue(json, "success", "true"));
            var why = getJsonValue(json, "why", "No specific reason identified");
            var newKnowledge = getJsonValue(json, "newKnowledge");

            var lessonsJson = extractJsonArray(json, "lessons");
            List<String> lessons = new ArrayList<>();
            if (lessonsJson != null) {
                for (var l : lessonsJson) lessons.add(l);
            }

            var reflection = new AgentReflection(
                userId, task.taskType().name(), task.description(),
                success, narrative, why, lessons, newKnowledge
            );

            if (!success) {
                var state = agentStates.get(userId);
                if (state != null) state.recordFailure();
            }

            log.info("Reflection: {} - {} (success={})", task.taskType(), narrative, success);
            return reflection;

        } catch (Exception e) {
            log.warn("Reflection failed: {}", e.getMessage());
            return new AgentReflection(
                userId, task.taskType().name(), task.description(),
                true, "Task completed without reflection", "Reflection generation failed",
                List.of(), null
            );
        }
    }

    // ────────────────────────────────────────────────
    //  WEEKLY REVIEW (Layer 7)
    // ────────────────────────────────────────────────

    private String buildWeeklyReviewContext(UUID userId, CareerAgentState state) {
        var sb = new StringBuilder();
        sb.append("=== AGENT STATE ===\n");
        sb.append("Total applications: ").append(state.totalApplicationsSubmitted()).append("\n");
        sb.append("Total interviews: ").append(state.totalInterviewsScheduled()).append("\n");
        sb.append("Consecutive failures: ").append(state.consecutiveFailures()).append("\n\n");

        var userEpisodes = episodes.getOrDefault(userId, List.of());
        if (!userEpisodes.isEmpty()) {
            sb.append("=== ALL EXPERIENCES THIS PERIOD ===\n");
            for (var ep : userEpisodes) {
                sb.append(ep.toSummary()).append("\n---\n");
            }
        }

        var userReflections = reflections.getOrDefault(userId, List.of());
        if (!userReflections.isEmpty()) {
            sb.append("=== ALL REFLECTIONS ===\n");
            for (var ref : userReflections) {
                sb.append(ref.toPromptContext()).append("\n---\n");
            }
        }

        var memories = memoryService.getAllMemories(userId);
        if (!memories.isEmpty()) {
            sb.append("=== MEMORIES ===\n");
            for (var m : memories) {
                sb.append("- ").append(m.memoryType()).append(": ").append(m.memoryKey())
                  .append(" = ").append(m.value()).append("\n");
            }
        }

        return sb.toString();
    }

    private String buildWeeklyReviewPrompt(String context) {
        return """
            You are a career agent conducting your weekly self-evaluation.

            Analyze the past week's performance and identify what's working and what's not.

            %s

            Today is: %s

            Output JSON only:
            {
              "metrics": {
                "applicationsSubmitted": number,
                "interviewsScheduled": number,
                "successRate": 0.0-1.0,
                "avgMatchScore": 0-100
              },
              "insights": [
                "insight about what's working",
                "insight about what's not working"
              ],
              "recommendations": [
                "specific recommendation to improve"
              ],
              "strategyChange": "how should the strategy change (or null)",
              "planNeedsUpdate": true/false
            }
            """.formatted(context, LocalDate.now());
    }

    private WeeklyReview executeWeeklyReview(String prompt, UUID userId) {
        try {
            var response = aiProvider.executePrompt(
                "You are a career agent conducting weekly review. Output only valid JSON.",
                prompt,
                REASONING_MODEL,
                0.3,
        2048
            );

            var json = extractJson(response);

            var metricsJson = extractJsonObject(json, "metrics");
            Map<String, Double> metrics = new HashMap<>();
            if (metricsJson != null) {
                metrics.put("applicationsSubmitted", parseDouble(getJsonValue(metricsJson, "applicationsSubmitted", "0")));
                metrics.put("interviewsScheduled", parseDouble(getJsonValue(metricsJson, "interviewsScheduled", "0")));
                metrics.put("successRate", parseDouble(getJsonValue(metricsJson, "successRate", "0")));
                metrics.put("avgMatchScore", parseDouble(getJsonValue(metricsJson, "avgMatchScore", "0")));
            }

            var insightsArray = extractJsonArray(json, "insights");
            List<String> insights = insightsArray != null ? new ArrayList<>(insightsArray) : Collections.emptyList();

            var recsArray = extractJsonArray(json, "recommendations");
            List<String> recommendations = recsArray != null ? new ArrayList<>(recsArray) : Collections.emptyList();

            var strategyChange = getJsonValue(json, "strategyChange");
            var planNeedsUpdate = "true".equals(getJsonValue(json, "planNeedsUpdate", "false"));

            var weekStart = LocalDate.now().minusDays(7);
            var review = new WeeklyReview(userId, weekStart, LocalDate.now(),
                metrics, insights, recommendations, strategyChange, planNeedsUpdate);

            log.info("Weekly review complete: {} insights, {} recommendations", insights.size(), recommendations.size());
            return review;

        } catch (Exception e) {
            log.warn("Weekly review failed: {}", e.getMessage());
            return new WeeklyReview(userId, LocalDate.now().minusDays(7), LocalDate.now(),
                Map.of(), List.of("Review generation failed: " + e.getMessage()),
                List.of(), null, false);
        }
    }

    // ────────────────────────────────────────────────
    //  MEMORY HELPERS
    // ────────────────────────────────────────────────

    private AgentIdentity buildIdentity(UUID userId, CandidateProfile candidateProfile) {
        if (candidateProfile == null) {
            return new AgentIdentity(userId, "User", "unknown@email.com");
        }

        var identity = new AgentIdentity(userId,
            candidateProfile.fullName() != null ? candidateProfile.fullName() : "User",
            candidateProfile.email() != null ? candidateProfile.email() : "unknown@email.com"
        );

        if (candidateProfile.headline() != null) identity.headline(candidateProfile.headline());
        if (candidateProfile.summary() != null) identity.summary(candidateProfile.summary());
        if (candidateProfile.skills() != null) identity.skills(candidateProfile.skills());
        if (candidateProfile.desiredLocation() != null) identity.preferredLocation(candidateProfile.desiredLocation());
        if (candidateProfile.salaryExpectationMin() != null) identity.salaryMin(candidateProfile.salaryExpectationMin());
        if (candidateProfile.salaryExpectationMax() != null) identity.salaryMax(candidateProfile.salaryExpectationMax());
        if (candidateProfile.certifications() != null) identity.certifications(candidateProfile.certifications());
        if (candidateProfile.employmentType() != null) identity.employmentType(candidateProfile.employmentType());
        if (candidateProfile.workPreference() != null) identity.workAuthorization(candidateProfile.workPreference());

        if (candidateProfile.experience() != null) {
            identity.experience(candidateProfile.experience().stream()
                .map(e -> {
                    var parts = e.split("\\|");
                    var title = parts.length > 0 ? parts[0].trim() : "";
                    var company = parts.length > 1 ? parts[1].trim() : "";
                    var startDate = parts.length > 2 ? parts[2].trim() : "";
                    var endDate = parts.length > 3 ? parts[3].trim() : null;
                    var desc = parts.length > 4 ? parts[4].trim() : "";
                    return new AgentIdentity.ExperienceEntry(title, company, startDate, endDate, desc);
                })
                .toList());
        }

        if (candidateProfile.education() != null) {
            identity.education(candidateProfile.education().stream()
                .map(e -> {
                    var parts = e.split("\\|");
                    var degree = parts.length > 0 ? parts[0].trim() : "";
                    var institution = parts.length > 1 ? parts[1].trim() : "";
                    var field = parts.length > 2 ? parts[2].trim() : "";
                    var year = parts.length > 3 ? parts[3].trim() : "";
                    return new AgentIdentity.EducationEntry(degree, institution, field, year);
                })
                .toList());
        }

        return identity;
    }

    private void storeReflection(UUID userId, AgentReflection reflection) {
        reflections.computeIfAbsent(userId, k -> new ArrayList<>()).add(reflection);
    }

    private void storeEpisode(UUID userId, AgentTask task, AgentReflection reflection) {
        var episode = new AgentEpisode(
            userId,
            task.taskType().name(),
            task.description(),
            reflection.narrative(),
            reflection.success(),
            reflection.lessons(),
            task.input() != null ? task.input() : Map.of(),
            reflection.success() ? 0.5 : 0.8
        );
        episodes.computeIfAbsent(userId, k -> new ArrayList<>()).add(episode);
    }

    private void storeWeeklyReview(UUID userId, WeeklyReview review) {
        weeklyReviews.computeIfAbsent(userId, k -> new ArrayList<>()).add(review);
    }

    private List<AgentEpisode> getRecentEpisodes(UUID userId, int limit) {
        var userEpisodes = episodes.getOrDefault(userId, List.of());
        if (userEpisodes.size() <= limit) return userEpisodes;
        return userEpisodes.subList(userEpisodes.size() - limit, userEpisodes.size());
    }

    private List<AgentReflection> getRecentReflections(UUID userId, int limit) {
        var userReflections = reflections.getOrDefault(userId, List.of());
        if (userReflections.size() <= limit) return userReflections;
        return userReflections.subList(userReflections.size() - limit, userReflections.size());
    }

    // ────────────────────────────────────────────────
    //  JSON PARSING (no dependency on JSON library)
    // ────────────────────────────────────────────────

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "{}";
    }

    private String getJsonValue(String json, String key) {
        return getJsonValue(json, key, null);
    }

    private String getJsonValue(String json, String key, String defaultValue) {
        var searchKey = "\"" + key + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return defaultValue;

        int colonIdx = json.indexOf(':', keyIdx + searchKey.length());
        if (colonIdx < 0) return defaultValue;

        var afterColon = json.substring(colonIdx + 1).trim();

        if (afterColon.startsWith("\"")) {
            int endQuote = findEndQuote(afterColon);
            if (endQuote > 0) {
                return afterColon.substring(1, endQuote);
            }
            return defaultValue;
        }

        if (afterColon.startsWith("true") || afterColon.startsWith("false")) {
            int end = Math.min(
                indexOfNonDigit(afterColon, 0),
                afterColon.length()
            );
            return afterColon.substring(0, end).trim();
        }

        int end = indexOfNonDigit(afterColon, 0);
        if (end > 0) {
            return afterColon.substring(0, end).trim();
        }

        return defaultValue;
    }

    private List<String> extractJsonArray(String json, String key) {
        var searchKey = "\"" + key + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return null;

        int colonIdx = json.indexOf(':', keyIdx + searchKey.length());
        if (colonIdx < 0) return null;

        var afterColon = json.substring(colonIdx + 1).trim();
        if (!afterColon.startsWith("[")) return null;

        int bracketDepth = 0;
        int arrayStart = colonIdx + 1 + afterColon.indexOf('[');
        int arrayEnd = -1;

        for (int i = arrayStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') bracketDepth++;
            else if (c == ']') {
                bracketDepth--;
                if (bracketDepth == 0) {
                    arrayEnd = i;
                    break;
                }
            }
        }

        if (arrayEnd < 0) return null;

        var arrayContent = json.substring(arrayStart + 1, arrayEnd);
        List<String> items = new ArrayList<>();
        int inString = 0;
        var current = new StringBuilder();

        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '"' && (i == 0 || arrayContent.charAt(i - 1) != '\\')) {
                inString = 1 - inString;
                if (inString == 0) {
                    items.add(current.toString());
                    current.setLength(0);
                }
            } else if (inString == 1) {
                current.append(c);
            }
        }

        return items;
    }

    private String extractJsonObject(String json, String key) {
        var searchKey = "\"" + key + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return null;

        int colonIdx = json.indexOf(':', keyIdx + searchKey.length());
        if (colonIdx < 0) return null;

        var afterColon = json.substring(colonIdx + 1).trim();
        if (!afterColon.startsWith("{")) return null;

        int braceDepth = 0;
        int objStart = colonIdx + 1 + afterColon.indexOf('{');
        int objEnd = -1;

        for (int i = objStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') braceDepth++;
            else if (c == '}') {
                braceDepth--;
                if (braceDepth == 0) { objEnd = i; break; }
            }
        }

        if (objEnd < 0) return null;
        return json.substring(objStart, objEnd + 1);
    }

    private int findEndQuote(String s) {
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == '"' && s.charAt(i - 1) != '\\') return i;
        }
        return -1;
    }

    private int indexOfNonDigit(String s, int start) {
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isDigit(c) && c != '.' && c != '-') return i;
        }
        return s.length();
    }

    private int parseInt(String s) {
        if (s == null) return 5;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 5; }
    }

    private double parseDouble(String s) {
        if (s == null) return 0.0;
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    // ────────────────────────────────────────────────
    //  RESULT VALUE OBJECT
    // ────────────────────────────────────────────────

    public record ThinkingResult(
        boolean shouldAct,
        String reasoning,
        String nextAction,
        int priority,
        String description,
        boolean requiresNotification,
        AgentDecision decision
    ) {}
}
