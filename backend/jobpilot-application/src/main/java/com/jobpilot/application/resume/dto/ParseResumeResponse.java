package com.jobpilot.application.resume.dto;

import java.util.List;

public record ParseResumeResponse(
    List<String> skills,
    List<String> experience,
    List<String> education,
    String summary,
    String contact,
    List<SectionDto> sections
) {}
