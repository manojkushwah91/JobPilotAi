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
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "jobpilot.scraper.enabled", havingValue = "true", matchIfMissing = true)
public class PlaywrightJobScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightJobScraper.class);
    private static final List<String> SEARCH_SITES = List.of(
        "https://weworkremotely.com/remote-jobs/search?term=%s",
        "https://remoteok.com/remote-%s-jobs"
    );

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
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"));
            var page = context.newPage();

            for (var siteTemplate : SEARCH_SITES) {
                if (results.size() >= maxResults) break;
                try {
                    var url = String.format(siteTemplate, query.replace(" ", "-").toLowerCase());
                    log.info("Scraping: {}", url);
                    page.navigate(url, new Page.NavigateOptions().setTimeout(30000));
                    page.waitForTimeout(3000);

                    var jobs = page.evaluate(
                        "() => Array.from(document.querySelectorAll('article, .job, .job-listing, [class*=job]')).slice(0," + maxResults + ").map(el => ({" +
                        "  title: (el.querySelector('h2, h3, .title, [class*=title]')?.textContent || '').trim()," +
                        "  company: (el.querySelector('.company, [class*=company], .employer')?.textContent || '').trim()," +
                        "  url: el.querySelector('a')?.href || ''," +
                        "  description: (el.querySelector('.description, [class*=description], p')?.textContent || '').trim().substring(0, 1000)," +
                        "  location: (el.querySelector('.location, [class*=location], .region')?.textContent || '').trim()" +
                        "}))"
                    );

                    if (jobs instanceof List<?> jobList) {
                        for (var item : jobList) {
                            if (item instanceof Map<?, ?> job) {
                                var title = job.get("title") != null ? job.get("title").toString() : "";
                                var company = job.get("company") != null ? job.get("company").toString() : "";
                                var appUrl = job.get("url") != null ? job.get("url").toString() : "";
                                var desc = job.get("description") != null ? job.get("description").toString() : "";
                                var loc = job.get("location") != null ? job.get("location").toString() : "";

                                if (title.isBlank() || appUrl.isBlank()) continue;
                                if (appUrl.startsWith("/")) appUrl = "https://weworkremotely.com" + appUrl;

                                results.add(new ScrapedJobDTO(
                                    "src-" + results.size(),
                                    title, company, null,
                                    loc.isBlank() ? null : Map.of("text", loc),
                                    null, desc, List.of(), List.of(), List.of(),
                                    "FULL_TIME", "MID", null, List.of(),
                                    appUrl, Instant.now()
                                ));
                                if (results.size() >= maxResults) break;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to scrape {}: {}", siteTemplate, e.getMessage());
                }
            }
            browser.close();
        } catch (Exception e) {
            log.error("Scraper failed: {}", e.getMessage());
        }
        log.info("Scraped {} jobs for query '{}'", results.size(), query);
        return results;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}