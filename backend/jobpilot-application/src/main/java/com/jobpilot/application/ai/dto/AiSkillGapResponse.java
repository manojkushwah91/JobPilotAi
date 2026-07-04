package com.jobpilot.application.ai.dto;

import java.util.List;

public record AiSkillGapResponse(
    List<String> existingSkills,
    List<String> missingSkills,
    List<SkillGapItem> skillGaps,
    String summary
) {}
