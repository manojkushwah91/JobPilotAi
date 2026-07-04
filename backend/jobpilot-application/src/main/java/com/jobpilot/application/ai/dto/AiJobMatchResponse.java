package com.jobpilot.application.ai.dto;

import java.util.List;
import java.util.Map;

public record AiJobMatchResponse(
    double matchScore,
    Map<String, Object> matchBreakdown,
    List<String> matchedSkills,
    List<String> missingSkills,
    String recommendation
) {}
