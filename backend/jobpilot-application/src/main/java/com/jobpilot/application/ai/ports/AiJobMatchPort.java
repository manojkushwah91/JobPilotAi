package com.jobpilot.application.ai.ports;

import com.jobpilot.application.ai.dto.*;

public interface AiJobMatchPort {
    AiJobMatchResponse matchJob(AiJobMatchRequest request);
}
