package com.jobpilot.application.ai.ports;

import com.jobpilot.application.ai.dto.*;

public interface AiSkillGapPort {
    AiSkillGapResponse analyzeSkillGap(AiSkillGapRequest request);
}
