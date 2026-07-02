package com.jobpilot.application.ai.ports;

import com.jobpilot.application.ai.dto.*;

public interface AiResumeScoringPort {
    AiResumeScoreResponse scoreResume(AiResumeScoreRequest request);
}
