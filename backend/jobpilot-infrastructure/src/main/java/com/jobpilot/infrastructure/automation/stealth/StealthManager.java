package com.jobpilot.infrastructure.automation.stealth;

import com.jobpilot.infrastructure.automation.PlaywrightBrowserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class StealthManager {

    private static final Logger log = LoggerFactory.getLogger(StealthManager.class);

    private static final List<String> USER_AGENTS = List.of(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
    );

    private static final List<int[]> VIEWPORTS = List.of(
        new int[]{1920, 1080},
        new int[]{1366, 768},
        new int[]{1536, 864},
        new int[]{1440, 900},
        new int[]{1280, 720},
        new int[]{1600, 900}
    );

    private static final List<String> LOCALES = List.of(
        "en-US", "en-GB", "en-CA", "en-AU"
    );

    private static final List<String> TIMEZONES = List.of(
        "America/New_York", "America/Chicago", "America/Denver",
        "America/Los_Angeles", "Europe/London", "Europe/Berlin"
    );

    private final Random random = new Random();

    @Value("${jobpilot.stealth.enabled:true}")
    private boolean stealthEnabled;

    @Value("${jobpilot.stealth.min-delay-ms:1000}")
    private int minDelayMs;

    @Value("${jobpilot.stealth.max-delay-ms:5000}")
    private int maxDelayMs;

    public void applyStealthSettings(PlaywrightBrowserManager browserManager) {
        if (!stealthEnabled) return;

        var page = browserManager.getPage();
        if (page == null) return;

        page.evaluate("""
            () => {
                Object.defineProperty(navigator, 'webdriver', { get: () => false });
                Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en'] });
                Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] });
                
                const originalQuery = window.navigator.permissions.query;
                window.navigator.permissions.query = (parameters) => (
                    parameters.name === 'notifications' ?
                        Promise.resolve({ state: Notification.permission }) :
                        originalQuery(parameters)
                );
                
                window.chrome = { runtime: {} };
                
                Object.defineProperty(navigator, 'maxTouchPoints', { get: () => 1 });
            }
        """);

        log.debug("Applied stealth settings");
    }

    public String getRandomUserAgent() {
        return USER_AGENTS.get(random.nextInt(USER_AGENTS.size()));
    }

    public int[] getRandomViewport() {
        return VIEWPORTS.get(random.nextInt(VIEWPORTS.size()));
    }

    public String getRandomLocale() {
        return LOCALES.get(random.nextInt(LOCALES.size()));
    }

    public String getRandomTimezone() {
        return TIMEZONES.get(random.nextInt(TIMEZONES.size()));
    }

    public void randomDelay() {
        if (!stealthEnabled) return;
        var delay = minDelayMs + random.nextInt(maxDelayMs - minDelayMs);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void randomDelay(int minMs, int maxMs) {
        var delay = minMs + random.nextInt(maxMs - minMs);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Map<String, Object> getStealthLaunchOptions() {
        return Map.of(
            "headless", true,
            "userAgent", getRandomUserAgent(),
            "viewport", getRandomViewport(),
            "locale", getRandomLocale(),
            "timezoneId", getRandomTimezone(),
            "args", List.of(
                "--no-sandbox",
                "--disable-setuid-sandbox",
                "--disable-blink-features=AutomationControlled",
                "--disable-features=IsolateOrigins,site-per-process",
                "--disable-site-isolation-trials"
            )
        );
    }
}
