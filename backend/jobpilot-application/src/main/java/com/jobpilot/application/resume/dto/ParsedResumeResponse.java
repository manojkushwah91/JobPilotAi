package com.jobpilot.application.resume.dto;

import java.util.List;
import java.util.Map;

public record ParsedResumeResponse(
    String fullText,
    String email,
    String phone,
    String linkedinUrl,
    String githubUrl,
    List<String> skills,
    List<Map<String, String>> sections,
    int yearsExperience
) {}
