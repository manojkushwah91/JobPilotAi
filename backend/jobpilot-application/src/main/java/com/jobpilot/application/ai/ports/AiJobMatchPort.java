package com.jobpilot.application.ai.ports;

import com.jobpilot.application.ai.dto.AiJobMatchRequest;
import com.jobpilot.application.ai.dto.AiJobMatchResponse;

public interface AiJobMatchPort {
    AiJobMatchResponse matchJob(AiJobMatchRequest request);
}
