package com.jobpilot.infrastructure.automation;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Requires Playwright browsers installed. Run with -Dtest.includeDisabled=true")
class PlaywrightIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightIntegrationTest.class);

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(true)
            .setArgs(java.util.List.of("--no-sandbox", "--disable-setuid-sandbox")));
        context = browser.newContext(new Browser.NewContextOptions()
            .setViewportSize(1280, 720)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"));
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        if (page != null) page.close();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @Test
    void shouldLaunchBrowser() {
        assertNotNull(browser);
        assertNotNull(context);
        assertNotNull(page);
        log.info("Browser launched successfully");
    }

    @Test
    void shouldNavigateToUrl() {
        page.navigate("https://example.com");
        assertEquals("https://example.com/", page.url());
        log.info("Navigation successful");
    }

    @Test
    void shouldTakeScreenshot() {
        page.navigate("https://example.com");
        var screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(false));
        assertNotNull(screenshot);
        assertTrue(screenshot.length > 0);
        log.info("Screenshot taken: {} bytes", screenshot.length);
    }

    @Test
    void shouldGetPageContent() {
        page.navigate("https://example.com");
        var content = page.content();
        assertNotNull(content);
        assertTrue(content.contains("Example Domain"));
        log.info("Page content retrieved: {} chars", content.length());
    }

    @Test
    void shouldWaitForSelector() {
        page.navigate("https://example.com");
        page.waitForSelector("h1");
        var heading = page.textContent("h1");
        assertEquals("Example Domain", heading);
        log.info("Selector found: {}", heading);
    }

    @Test
    void shouldEvaluateJavaScript() {
        page.navigate("https://example.com");
        var title = page.evaluate("document.title");
        assertEquals("Example Domain", title.toString());
        log.info("JavaScript evaluation successful: {}", title);
    }

    @Test
    void shouldHandleMultiplePages() {
        var page2 = context.newPage();
        page.navigate("https://example.com");
        page2.navigate("https://www.iana.org/domains/example");

        assertEquals(2, context.pages().size());
        log.info("Multiple pages handled: {} pages", context.pages().size());

        page2.close();
    }

    @Test
    void shouldManageCookies() {
        page.navigate("https://example.com");
        var script = "document.cookie = 'test_cookie=test_value; path=/; domain=example.com'";
        page.evaluate(script);

        var cookieValue = page.evaluate("document.cookie");
        assertNotNull(cookieValue);
        log.info("Cookie set via JavaScript: {}", cookieValue);
    }

    @Test
    void shouldHandleFormFilling() {
        page.setContent("""
            <form>
                <input type="text" id="name" name="name">
                <input type="email" id="email" name="email">
                <select id="country" name="country">
                    <option value="us">United States</option>
                    <option value="uk">United Kingdom</option>
                </select>
                <button type="submit">Submit</button>
            </form>
            """);

        page.fill("#name", "John Doe");
        page.fill("#email", "john@example.com");
        page.selectOption("#country", "uk");

        assertEquals("John Doe", page.inputValue("#name"));
        assertEquals("john@example.com", page.inputValue("#email"));
        assertEquals("uk", page.inputValue("#country"));
        log.info("Form filling successful");
    }

    @Test
    void shouldHandleFileUpload() {
        page.setContent("""
            <form>
                <input type="file" id="file-upload">
            </form>
            """);

        var fileInput = page.querySelector("#file-upload");
        assertNotNull(fileInput);
        log.info("File upload input found");
    }

    @Test
    void shouldHandleElementInteraction() {
        page.setContent("""
            <div id="clickable" style="padding: 20px; background: blue; color: white;">Click me</div>
            <div id="result"></div>
            <script>
                document.getElementById('clickable').addEventListener('click', function() {
                    document.getElementById('result').textContent = 'Clicked!';
                });
            </script>
            """);

        page.click("#clickable");
        var result = page.textContent("#result");
        assertEquals("Clicked!", result);
        log.info("Element interaction successful");
    }
}
