package com.jobpilot.application.ai.dto;

import java.util.List;
import java.util.Map;

public record AiResumeScoreResponse(int atsScore, Map<String, Object> scoreBreakdown,
                                     List<String> missingKeywords, List<String> strengths,
                                     List<String> improvements) {}
