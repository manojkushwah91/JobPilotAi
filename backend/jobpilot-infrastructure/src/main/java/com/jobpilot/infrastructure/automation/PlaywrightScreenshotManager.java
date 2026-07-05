package com.jobpilot.infrastructure.automation;

import com.jobpilot.application.automation.ports.ScreenshotManagerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlaywrightScreenshotManager implements ScreenshotManagerPort {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightScreenshotManager.class);

    private final PlaywrightBrowserManager browserManager;
    private final Map<String, List<String>> screenshotHistory = new ConcurrentHashMap<>();

    @Value("${jobpilot.screenshots.dir:./screenshots}")
    private String screenshotsDir;

    public PlaywrightScreenshotManager(PlaywrightBrowserManager browserManager) {
        this.browserManager = browserManager;
    }

    @Override
    public byte[] takeScreenshot() {
        return browserManager.takeScreenshot();
    }

    @Override
    public byte[] takeElementScreenshot(String selector) {
        return browserManager.takeElementScreenshot(selector);
    }

    @Override
    public byte[] takeFullPageScreenshot() {
        return browserManager.takeScreenshot();
    }

    @Override
    public String saveScreenshot(byte[] data, String name) {
        var timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        var filename = String.format("%s_%s_%s.png", timestamp, name, UUID.randomUUID().toString().substring(0, 8));
        var path = Paths.get(screenshotsDir, filename);

        try {
            createDirectoryIfNeeded(path.getParent());
            Files.write(path, data);
            log.debug("Saved screenshot: {}", path);
            return path.toString();
        } catch (IOException e) {
            log.error("Failed to save screenshot: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<String> getScreenshotHistory(String sessionId) {
        return screenshotHistory.getOrDefault(sessionId, new ArrayList<>());
    }

    public String captureFullPage(String context) {
        var screenshot = takeScreenshot();
        return saveScreenshot(screenshot, context);
    }

    public String captureElement(String selector, String context) {
        var screenshot = takeElementScreenshot(selector);
        return saveScreenshot(screenshot, context);
    }

    private void createDirectoryIfNeeded(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }
}
