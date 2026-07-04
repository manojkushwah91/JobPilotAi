package com.jobpilot.application.ai.ports;

import com.jobpilot.application.ai.dto.AiResumeScoreRequest;
import com.jobpilot.application.ai.dto.AiResumeScoreResponse;

public interface AiResumeScoringPort {
    AiResumeScoreResponse scoreResume(AiResumeScoreRequest request);
}
