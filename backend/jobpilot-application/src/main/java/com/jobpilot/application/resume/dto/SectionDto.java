package com.jobpilot.application.resume.dto;

import java.util.Map;

public record SectionDto(
    String id,
    String type,
    String title,
    Map<String, Object> content,
    int sortOrder
) {}
