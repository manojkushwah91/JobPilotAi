package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.BrowserAutomationPort;
import com.jobpilot.application.agent.ports.CandidateProfileRepository;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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
        return "Submits a job application using browser automation with candidate profile data";
    }

    @Override
    public boolean requiresApproval() {
        return true;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing application submission tool");

        var url = (String) input.getOrDefault("url", "");
        var resumePath = (String) input.getOrDefault("resumePath", "");
        var userId = input.get("userId") instanceof UUID uid ? uid : null;

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
            });
        }

        try {
            browserAutomation.launchBrowser();
            browserAutomation.navigateTo(url);

            var formFields = browserAutomation.getApplicationFormFields(url);

            if (profileData.containsKey("email")) {
                autoFillForm(formFields, profileData);
            }

            var screenshot = browserAutomation.takeScreenshot();

            var result = new LinkedHashMap<String, Object>();
            result.put("status", "awaiting_approval");
            result.put("url", url);
            result.put("screenshot", screenshot != null ? "captured" : "failed");
            result.put("profileLoaded", !profileData.isEmpty());
            result.put("formFieldsFound", formFields != null ? formFields.size() : 0);
            result.put("message", "Application prepared with profile data. Awaiting user approval.");
            return result;
        } catch (Exception e) {
            log.error("Application submission failed: {}", e.getMessage());
            return Map.of(
                "status", "error",
                "error", e.getMessage()
            );
        } finally {
            try {
                browserAutomation.closeBrowser();
            } catch (Exception e) {
                log.warn("Failed to close browser: {}", e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void autoFillForm(Map<String, Object> formFields, Map<String, Object> profile) {
        if (formFields == null) return;

        for (var entry : formFields.entrySet()) {
            var fieldName = entry.getKey().toLowerCase();
            var selector = (String) entry.getValue();

            try {
                if (fieldName.contains("name") && !fieldName.contains("company") && profile.containsKey("fullName")) {
                    browserAutomation.fillField(selector, (String) profile.get("fullName"));
                } else if (fieldName.contains("email") && profile.containsKey("email")) {
                    browserAutomation.fillField(selector, (String) profile.get("email"));
                } else if (fieldName.contains("phone") && profile.containsKey("phone")) {
                    browserAutomation.fillField(selector, (String) profile.get("phone"));
                } else if (fieldName.contains("location") || fieldName.contains("city")) {
                    if (profile.containsKey("location")) {
                        browserAutomation.fillField(selector, (String) profile.get("location"));
                    }
                } else if (fieldName.contains("linkedin") && profile.containsKey("linkedinUrl")) {
                    browserAutomation.fillField(selector, (String) profile.get("linkedinUrl"));
                }
            } catch (Exception e) {
                log.warn("Failed to fill field {}: {}", fieldName, e.getMessage());
            }
        }
    }

    @Override
    public int timeoutSeconds() {
        return 120;
    }
}
