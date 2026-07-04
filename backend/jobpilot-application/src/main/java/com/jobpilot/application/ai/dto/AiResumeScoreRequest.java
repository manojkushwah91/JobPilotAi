package com.jobpilot.application.ai.dto;

public record AiResumeScoreRequest(
    String resumeId,
    String jobDescription
) {}
