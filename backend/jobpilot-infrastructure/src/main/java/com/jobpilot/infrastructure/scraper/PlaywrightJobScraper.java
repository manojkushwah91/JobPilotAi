package com.jobpilot.infrastructure.scraper;

import com.jobpilot.application.scraper.dto.ScrapedJobDTO;
import com.jobpilot.application.scraper.ports.JobScraper;
import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "jobpilot.scraper.enabled", havingValue = "true", matchIfMissing = true)
public class PlaywrightJobScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightJobScraper.class);

    private static final List<SiteConfig> SEARCH_SITES = List.of(
        new SiteConfig(
            "https://remoteok.com/remote-%s-jobs",
            "https://remoteok.com",
            "remoteok"
        ),
        new SiteConfig(
            "https://weworkremotely.com/remote-jobs/search?term=%s",
            "https://weworkremotely.com",
            "weworkremotely"
        )
    );

    private record SiteConfig(String urlTemplate, String baseUrl, String name) {}

    @Override
    public String sourceName() {
        return "playwright-scraper";
    }

    @Override
    public List<ScrapedJobDTO> scrape(String query, String location, int maxResults) {
        var results = new ArrayList<ScrapedJobDTO>();

        try (var playwright = Playwright.create()) {
            var browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of("--no-sandbox", "--disable-setuid-sandbox")));
            var context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"));
            var page = context.newPage();

            for (var site : SEARCH_SITES) {
                if (results.size() >= maxResults) break;
                try {
                    var url = String.format(site.urlTemplate(), query.replace(" ", "-").toLowerCase());
                    log.info("[{}] Scraping: {}", site.name(), url);
                    page.navigate(url, new Page.NavigateOptions().setTimeout(30000));
                    page.waitForTimeout(5000);

                    var js = buildScraperJs(site.name(), maxResults);
                    var jobs = page.evaluate(js);

                    if (jobs instanceof List<?> jobList) {
                        int beforeSize = results.size();
                        for (var item : jobList) {
                            if (item instanceof Map<?, ?> job) {
                                var title = toString(job.get("title")).trim();
                                var company = toString(job.get("company")).trim();
                                var appUrl = toString(job.get("url")).trim();
                                var desc = toString(job.get("description")).trim();
                                var loc = toString(job.get("location")).trim();

                                if (title.isBlank() || appUrl.isBlank()) continue;
                                if (appUrl.startsWith("/")) {
                                    appUrl = site.baseUrl() + appUrl;
                                }

                                title = truncate(title, 255);
                                company = truncate(company, 255);
                                loc = truncate(loc, 255);
                                desc = truncate(desc, 2000);

                                var sourceId = site.name() + "-" + Integer.toHexString(appUrl.hashCode());

                                results.add(new ScrapedJobDTO(
                                    sourceId,
                                    title, company, null,
                                    loc.isBlank() ? null : Map.of("text", loc),
                                    null, desc, List.of(), List.of(), List.of(),
                                    "FULL_TIME", "MID", null, List.of(),
                                    appUrl, Instant.now()
                                ));
                                if (results.size() >= maxResults) break;
                            }
                        }
                        log.info("[{}] Found {} jobs", site.name(), results.size() - beforeSize);
                    }
                } catch (Exception e) {
                    log.warn("[{}] Failed to scrape: {}", site.name(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Scraper failed: {}", e.getMessage());
        }

        log.info("Total scraped {} jobs for query '{}'", results.size(), query);
        return results;
    }

    private String buildScraperJs(String siteName, int maxResults) {
        return switch (siteName) {
            case "remoteok" -> """
                () => Array.from(document.querySelectorAll('tr.job')).slice(0, %d).map(el => ({
                    title: (el.querySelector('h2')?.textContent || '').trim(),
                    company: (el.querySelector('h3')?.textContent || '').trim(),
                    url: (el.querySelector('a[href*="/remote-jobs/"]')?.href || el.querySelector('a')?.href || ''),
                    description: (el.querySelector('[class*="description"]')?.textContent || '').trim().substring(0, 500),
                    location: (el.querySelector('[class*="location"]')?.textContent || '').trim()
                }))
                """.formatted(maxResults);
            default -> """
                () => Array.from(document.querySelectorAll('section.job, [class*="job-listing"]')).slice(0, %d).map(el => ({
                    title: (el.querySelector('h2, h3, [class*="title"]')?.textContent || '').trim(),
                    company: (el.querySelector('.company, [class*="company"]')?.textContent || '').trim(),
                    url: (el.querySelector('a')?.href || ''),
                    description: (el.querySelector('[class*="description"], p')?.textContent || '').trim().substring(0, 500),
                    location: (el.querySelector('[class*="location"]')?.textContent || '').trim()
                }))
                """.formatted(maxResults);
        };
    }

    private static String toString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
