package com.jobpilot.application.ai.dto;

import java.util.List;

public record SkillGapItem(
    String skill,
    String category,
    String importance,
    List<String> learningResources
) {}
