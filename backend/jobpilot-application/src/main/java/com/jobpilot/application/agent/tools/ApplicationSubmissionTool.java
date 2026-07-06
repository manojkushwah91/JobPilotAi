package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.BrowserAutomationPort;
import com.jobpilot.application.agent.ports.CandidateProfileRepository;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ApplicationSubmissionTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(ApplicationSubmissionTool.class);

    private final BrowserAutomationPort browserAutomation;
    private final CandidateProfileRepository profileRepository;

    public ApplicationSubmissionTool(BrowserAutomationPort browserAutomation,
                                      CandidateProfileRepository profileRepository) {
        this.browserAutomation = browserAutomation;
        this.profileRepository = profileRepository;
    }

    @Override
    public String name() {
        return "SUBMIT_APPLICATION";
    }

    @Override
    public String description() {
        return "Navigates to a job URL, analyzes the form, fills it with candidate profile data, and submits";
    }

    @Override
    public boolean requiresApproval() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing application submission tool");

        var url = (String) input.getOrDefault("url", "");
        var jobTitle = (String) input.getOrDefault("title", "");
        var company = (String) input.getOrDefault("company", "");
        var userId = input.get("userId") instanceof UUID uid ? uid : null;

        if (url.isBlank()) {
            return Map.of("status", "error", "error", "No URL provided");
        }

        var profileData = new LinkedHashMap<String, Object>();
        if (userId != null) {
            profileRepository.findByUserId(userId).ifPresent(profile -> {
                profileData.put("fullName", profile.fullName());
                profileData.put("email", profile.email());
                profileData.put("phone", profile.phone());
                profileData.put("location", profile.location());
                profileData.put("skills", profile.skills());
                profileData.put("resumeText", profile.resumeText());
                profileData.put("resumeFileUrl", profile.resumeFileUrl());
                profileData.put("linkedinUrl", profile.linkedinUrl());
                profileData.put("portfolioUrl", profile.portfolioUrl());
            });
        }

        try {
            browserAutomation.launchBrowser();
            browserAutomation.navigateTo(url);

            Thread.sleep(2000);

            var formAnalysis = browserAutomation.getApplicationFormFields(url);
            var pageType = (String) formAnalysis.getOrDefault("pageType", "UNKNOWN");
            var hasCaptcha = (Boolean) formAnalysis.getOrDefault("hasCaptcha", false);
            var fields = (List<Map<String, Object>>) formAnalysis.getOrDefault("fields", List.of());

            log.info("Page type: {}, Fields found: {}, Has CAPTCHA: {}", pageType, fields.size(), hasCaptcha);

            int filledCount = 0;
            var filledFields = new ArrayList<String>();
            var skippedFields = new ArrayList<String>();

            for (var field : fields) {
                var selector = (String) field.get("selector");
                var fieldType = (String) field.get("type");
                var label = (String) field.getOrDefault("label", "");
                var name = (String) field.getOrDefault("name", "");
                var required = (Boolean) field.getOrDefault("required", false);

                var matchResult = matchFieldToProfile(label, name, fieldType, profileData);
                if (matchResult != null) {
                    try {
                        browserAutomation.fillField(selector, matchResult);
                        filledCount++;
                        filledFields.add(label.isEmpty() ? name : label);
                        log.info("Filled field '{}' with profile data", label.isEmpty() ? name : label);
                    } catch (Exception e) {
                        log.warn("Failed to fill field {}: {}", selector, e.getMessage());
                        skippedFields.add(label.isEmpty() ? name : label);
                    }
                } else if (required) {
                    skippedFields.add(label.isEmpty() ? name : label);
                }
            }

            var screenshot = browserAutomation.takeScreenshot();

            var result = new LinkedHashMap<String, Object>();
            result.put("status", "awaiting_approval");
            result.put("url", url);
            result.put("jobTitle", jobTitle);
            result.put("company", company);
            result.put("pageType", pageType);
            result.put("totalFields", fields.size());
            result.put("filledFields", filledCount);
            result.put("filledFieldNames", filledFields);
            result.put("unfilledRequiredFields", skippedFields);
            result.put("hasCaptcha", hasCaptcha);
            result.put("profileLoaded", !profileData.isEmpty());
            result.put("screenshot", screenshot != null && screenshot.length > 0 ? "captured" : "failed");
            result.put("message", String.format("Found %d fields, filled %d with profile data. Awaiting approval.",
                fields.size(), filledCount));
            return result;

        } catch (Exception e) {
            log.error("Application submission failed: {}", e.getMessage());
            return Map.of(
                "status", "error",
                "error", e.getMessage(),
                "url", url
            );
        } finally {
            try {
                browserAutomation.closeBrowser();
            } catch (Exception e) {
                log.warn("Failed to close browser: {}", e.getMessage());
            }
        }
    }

    private String matchFieldToProfile(String label, String name, String type, Map<String, Object> profile) {
        var fieldText = (label + " " + name).toLowerCase();

        if (profile.isEmpty()) return null;

        if (fieldText.contains("first") && fieldText.contains("name")) {
            var fullName = (String) profile.get("fullName");
            if (fullName != null) {
                var parts = fullName.split(" ", 2);
                return parts[0];
            }
        }
        if (fieldText.contains("last") && fieldText.contains("name")) {
            var fullName = (String) profile.get("fullName");
            if (fullName != null) {
                var parts = fullName.split(" ", 2);
                return parts.length > 1 ? parts[1] : "";
            }
        }
        if (fieldText.contains("full") && fieldText.contains("name") || fieldText.equals("name")) {
            return (String) profile.get("fullName");
        }
        if (fieldText.contains("email")) {
            return (String) profile.get("email");
        }
        if (fieldText.contains("phone") || fieldText.contains("tel")) {
            return (String) profile.get("phone");
        }
        if (fieldText.contains("location") || fieldText.contains("city") || fieldText.contains("address")) {
            return (String) profile.get("location");
        }
        if (fieldText.contains("linkedin")) {
            return (String) profile.get("linkedinUrl");
        }
        if (fieldText.contains("portfolio") || fieldText.contains("website") || fieldText.contains("github")) {
            return (String) profile.get("portfolioUrl");
        }
        if (fieldText.contains("summary") || fieldText.contains("about") || fieldText.contains("bio")) {
            return (String) profile.get("summary");
        }

        if ("file".equals(type) && fieldText.contains("resume")) {
            var fileUrl = (String) profile.get("resumeFileUrl");
            if (fileUrl != null && !fileUrl.isBlank()) {
                return fileUrl;
            }
        }

        return null;
    }

    @Override
    public int timeoutSeconds() {
        return 180;
    }
}
