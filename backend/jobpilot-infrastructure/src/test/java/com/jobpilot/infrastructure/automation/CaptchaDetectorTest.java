package com.jobpilot.infrastructure.automation;

import com.jobpilot.application.automation.ports.CaptchaDetectorPort;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaptchaDetectorTest {

    @Mock
    private PlaywrightBrowserManager browserManager;

    @InjectMocks
    private PlaywrightCaptchaDetector captchaDetector;

    private Page page;

    @BeforeEach
    void setUp() {
        page = mock(Page.class);
        when(browserManager.getPage()).thenReturn(page);
    }

    @Test
    void shouldReturnNoCaptchaWhenNonePresent() {
        when(page.querySelector(anyString())).thenReturn(null);

        assertFalse(captchaDetector.detectCaptcha());
    }

    @Test
    void shouldDetectRecaptcha() {
        var recaptchaElement = mock(ElementHandle.class);
        when(page.querySelector(".g-recaptcha")).thenReturn(recaptchaElement);

        assertTrue(captchaDetector.detectCaptcha());
    }

    @Test
    void shouldDetectHcaptcha() {
        var hcaptchaElement = mock(ElementHandle.class);
        when(page.querySelector("iframe[src*='hcaptcha']")).thenReturn(hcaptchaElement);

        assertTrue(captchaDetector.detectCaptcha());
    }

    @Test
    void shouldReturnCaptchaTypeNoneWhenNoCaptcha() {
        when(page.querySelector(anyString())).thenReturn(null);

        assertEquals(CaptchaDetectorPort.CaptchaType.NONE, captchaDetector.detectCaptchaType());
    }

    @Test
    void shouldReturnRecaptchaV2Type() {
        var recaptchaElement = mock(ElementHandle.class);
        when(page.querySelector(".g-recaptcha")).thenReturn(recaptchaElement);

        assertEquals(CaptchaDetectorPort.CaptchaType.RECAPTCHA_V2, captchaDetector.detectCaptchaType());
    }

    @Test
    void shouldReturnHcaptchaType() {
        var hcaptchaElement = mock(ElementHandle.class);
        when(page.querySelector("iframe[src*='hcaptcha']")).thenReturn(hcaptchaElement);

        assertEquals(CaptchaDetectorPort.CaptchaType.HCAPTCHA, captchaDetector.detectCaptchaType());
    }

    @Test
    void shouldRequireManualInterventionForRecaptcha() {
        var recaptchaElement = mock(ElementHandle.class);
        when(page.querySelector(".g-recaptcha")).thenReturn(recaptchaElement);

        assertTrue(captchaDetector.requiresManualIntervention());
    }

    @Test
    void shouldNotRequireManualInterventionForRecaptchaV3() {
        var recaptchaV3Element = mock(ElementHandle.class);
        when(page.querySelector("[data-callback='onRecaptchaSuccess']")).thenReturn(recaptchaV3Element);

        assertFalse(captchaDetector.requiresManualIntervention());
    }

    @Test
    void shouldCheckIfCaptchaPresentAtSelector() {
        var element = mock(ElementHandle.class);
        when(page.querySelector("#captcha")).thenReturn(element);

        assertTrue(captchaDetector.isCaptchaPresent("#captcha"));
    }
}
