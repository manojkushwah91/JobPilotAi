package com.jobpilot.application.interview.dto;

public record ScoreInterviewAnswerResponse(
    int score,
    String feedback,
    String strengths,
    String improvements
) {}
