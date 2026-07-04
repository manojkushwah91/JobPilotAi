package com.jobpilot.application.resume.dto;

import java.util.List;

public record ScoreResumeResponse(
    String resumeId,
    int score,
    String feedback,
    List<String> strengths,
    List<String> weaknesses,
    List<String> improvements
) {}
