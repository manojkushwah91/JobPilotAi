package com.jobpilot.infrastructure.automation;

import com.jobpilot.application.automation.ports.CaptchaDetectorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlaywrightCaptchaDetector implements CaptchaDetectorPort {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightCaptchaDetector.class);

    private final PlaywrightBrowserManager browserManager;

    private static final List<String> CAPTCHA_SELECTORS = List.of(
        "iframe[src*='recaptcha']",
        "iframe[src*='hcaptcha']",
        ".g-recaptcha",
        "#captcha",
        "[data-testid='captcha']",
        ".captcha-container",
        "iframe[src*='funcaptcha']",
        ".h-captcha",
        "[data-callback='onRecaptchaSuccess']"
    );

    public PlaywrightCaptchaDetector(PlaywrightBrowserManager browserManager) {
        this.browserManager = browserManager;
    }

    @Override
    public boolean detectCaptcha() {
        return !getCaptchaSelectors().isEmpty();
    }

    @Override
    public CaptchaType detectCaptchaType() {
        var page = browserManager.getPage();

        if (page.querySelector(".g-recaptcha") != null ||
            page.querySelector("iframe[src*='recaptcha']") != null) {
            return CaptchaType.RECAPTCHA_V2;
        }

        if (page.querySelector("iframe[src*='hcaptcha']") != null ||
            page.querySelector(".h-captcha") != null) {
            return CaptchaType.HCAPTCHA;
        }

        if (page.querySelector("iframe[src*='funcaptcha']") != null) {
            return CaptchaType.FUNCAPTCHA;
        }

        if (page.querySelector("[data-callback='onRecaptchaSuccess']") != null) {
            return CaptchaType.RECAPTCHA_V3;
        }

        if (page.querySelector("#captcha") != null ||
            page.querySelector("[data-testid='captcha']") != null) {
            return CaptchaType.IMAGE_CAPTCHA;
        }

        return CaptchaType.NONE;
    }

    @Override
    public List<String> getCaptchaSelectors() {
        var page = browserManager.getPage();
        return CAPTCHA_SELECTORS.stream()
            .filter(selector -> page.querySelector(selector) != null)
            .toList();
    }

    @Override
    public boolean isCaptchaPresent(String selector) {
        return browserManager.getPage().querySelector(selector) != null;
    }

    @Override
    public boolean requiresManualIntervention() {
        var captchaType = detectCaptchaType();
        return switch (captchaType) {
            case RECAPTCHA_V2, HCAPTCHA, FUNCAPTCHA -> true;
            case RECAPTCHA_V3 -> false;
            case IMAGE_CAPTCHA, TEXT_CAPTCHA -> true;
            case UNKNOWN -> true;
            case NONE -> false;
        };
    }
}
