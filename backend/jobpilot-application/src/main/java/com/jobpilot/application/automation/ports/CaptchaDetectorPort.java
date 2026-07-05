package com.jobpilot.application.automation.ports;

import java.util.List;

public interface CaptchaDetectorPort {

    boolean detectCaptcha();

    CaptchaType detectCaptchaType();

    List<String> getCaptchaSelectors();

    boolean isCaptchaPresent(String selector);

    default boolean requiresManualIntervention() {
        return true;
    }

    enum CaptchaType {
        RECAPTCHA_V2,
        RECAPTCHA_V3,
        HCAPTCHA,
        FUNCAPTCHA,
        IMAGE_CAPTCHA,
        TEXT_CAPTCHA,
        UNKNOWN,
        NONE
    }
}
