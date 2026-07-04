package com.jobpilot.application.ai.ports;

import com.jobpilot.application.ai.dto.AiSkillGapRequest;
import com.jobpilot.application.ai.dto.AiSkillGapResponse;

public interface AiSkillGapPort {
    AiSkillGapResponse analyzeSkillGap(AiSkillGapRequest request);
}
