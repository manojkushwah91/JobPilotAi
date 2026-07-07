package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.BrowserAutomationPort;
import com.jobpilot.application.agent.ports.CandidateProfileRepository;
import com.jobpilot.application.automation.ports.PortalDetectorPort;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ApplicationSubmissionTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(ApplicationSubmissionTool.class);

    private final BrowserAutomationPort browserAutomation;
    private final CandidateProfileRepository profileRepository;
    @Autowired(required = false)
    private PortalDetectorPort portalDetector;

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
                profileData.put("fullName", profile.fullName() != null ? profile.fullName() : "");
                profileData.put("email", profile.email() != null ? profile.email() : "");
                profileData.put("phone", profile.phone() != null ? profile.phone() : "");
                profileData.put("location", profile.location() != null ? profile.location() : "");
                profileData.put("headline", profile.headline() != null ? profile.headline() : "");
                profileData.put("summary", profile.summary() != null ? profile.summary() : "");
                profileData.put("skills", profile.skills());
                profileData.put("experience", profile.experience());
                profileData.put("education", profile.education());
                profileData.put("certifications", profile.certifications());
                profileData.put("resumeText", profile.resumeText() != null ? profile.resumeText() : "");
                profileData.put("resumeFileUrl", profile.resumeFileUrl() != null ? profile.resumeFileUrl() : "");
                profileData.put("linkedinUrl", profile.linkedinUrl() != null ? profile.linkedinUrl() : "");
                profileData.put("portfolioUrl", profile.portfolioUrl() != null ? profile.portfolioUrl() : "");
                profileData.put("yearsExperience", profile.yearsExperience() != null ? String.valueOf(profile.yearsExperience()) : "");
                profileData.put("desiredRole", profile.desiredRole() != null ? profile.desiredRole() : "");
                profileData.put("desiredLocation", profile.desiredLocation() != null ? profile.desiredLocation() : "");
                profileData.put("salaryExpectationMin", profile.salaryExpectationMin() != null ? String.valueOf(profile.salaryExpectationMin()) : "");
                profileData.put("salaryExpectationMax", profile.salaryExpectationMax() != null ? String.valueOf(profile.salaryExpectationMax()) : "");
                profileData.put("currency", profile.currency() != null ? profile.currency() : "");
                profileData.put("employmentType", profile.employmentType() != null ? profile.employmentType() : "");
                profileData.put("workPreference", profile.workPreference() != null ? profile.workPreference() : "");
            });
        }

        try {
            browserAutomation.launchBrowser();
            browserAutomation.navigateTo(url);

            Thread.sleep(2000);

            String currentUrl = url;
            String portalType = "unknown";
            if (portalDetector != null) {
                portalType = portalDetector.detectPortal(currentUrl);
            }

            if ("unknown".equals(portalType)) {
                var applyUrl = findAndClickApplyButton();
                if (applyUrl != null) {
                    currentUrl = applyUrl;
                    Thread.sleep(3000);
                    if (portalDetector != null) {
                        portalType = portalDetector.detectPortal(currentUrl);
                    }
                    log.info("Redirected to: {} (portal: {})", currentUrl, portalType);
                }
            }

            var formAnalysis = browserAutomation.getApplicationFormFields(currentUrl);
            var pageType = (String) formAnalysis.getOrDefault("pageType", "UNKNOWN");
            var hasCaptcha = (Boolean) formAnalysis.getOrDefault("hasCaptcha", false);
            var fields = (List<Map<String, Object>>) formAnalysis.getOrDefault("fields", List.of());

            log.info("Page type: {}, Portal: {}, Fields found: {}, Has CAPTCHA: {}",
                pageType, portalType, fields.size(), hasCaptcha);

            int filledCount = 0;
            var filledFields = new ArrayList<String>();
            var skippedFields = new ArrayList<String>();

            var portalSelectors = Map.<String, String>of();
            if (portalDetector != null && !"unknown".equals(portalType)) {
                portalSelectors = portalDetector.getPortalSelectors(portalType);
            }

            if (!portalSelectors.isEmpty()) {
                var mapping = buildPortalFieldMapping(portalType, profileData);
                for (var entry : mapping.entrySet()) {
                    var selector = portalSelectors.get(entry.getKey());
                    var value = entry.getValue();
                    if (selector != null && value != null && !value.isBlank()) {
                        try {
                            fillFieldByType(selector, "text", value);
                            filledCount++;
                            filledFields.add(entry.getKey());
                            log.info("Portal fill: {} = {} via {}", entry.getKey(), selector, portalType);
                        } catch (Exception e) {
                            log.warn("Portal fill failed for {}: {}", entry.getKey(), e.getMessage());
                            skippedFields.add(entry.getKey());
                        }
                    }
                }

                var resumeSelector = portalSelectors.get("resume");
                var fileUrl = (String) profileData.get("resumeFileUrl");
                if (resumeSelector != null && fileUrl != null && !fileUrl.isBlank()) {
                    try {
                        fillFieldByType(resumeSelector, "file", fileUrl);
                        filledCount++;
                        filledFields.add("resume");
                    } catch (Exception e) {
                        log.warn("Resume upload failed: {}", e.getMessage());
                    }
                }

                var submitSelector = portalSelectors.get("submit");
                if (submitSelector != null) {
                    log.info("Portal submit button: {} for {}", submitSelector, portalType);
                }
            } else {
                for (var field : fields) {
                    var selector = (String) field.get("selector");
                    var fieldType = (String) field.get("type");
                    var label = (String) field.getOrDefault("label", "");
                    var name = (String) field.getOrDefault("name", "");

                    var matchResult = matchFieldToProfile(label, name, fieldType, profileData);
                    if (matchResult != null) {
                        try {
                            fillFieldByType(selector, fieldType, matchResult);
                            filledCount++;
                            filledFields.add(label.isEmpty() ? name : label);
                        } catch (Exception e) {
                            log.warn("Failed to fill field {}: {}", selector, e.getMessage());
                            skippedFields.add(label.isEmpty() ? name : label);
                        }
                    } else if ((Boolean) field.getOrDefault("required", false)) {
                        skippedFields.add(label.isEmpty() ? name : label);
                    }
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
            result.put("portalType", portalType);
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

    private Map<String, String> buildPortalFieldMapping(String portalType, Map<String, Object> profile) {
        var mapping = new LinkedHashMap<String, String>();
        if (profile.isEmpty()) return mapping;

        var fullName = (String) profile.getOrDefault("fullName", "");
        var parts = fullName.split(" ", 2);
        var firstName = parts.length > 0 ? parts[0] : "";
        var lastName = parts.length > 1 ? parts[1] : "";

        var email = (String) profile.getOrDefault("email", "");
        var phone = (String) profile.getOrDefault("phone", "");
        var location = (String) profile.getOrDefault("location", "");
        var linkedin = (String) profile.getOrDefault("linkedinUrl", "");
        var portfolio = (String) profile.getOrDefault("portfolioUrl", "");
        var summary = (String) profile.getOrDefault("summary", "");
        var headline = (String) profile.getOrDefault("headline", "");

        switch (portalType) {
            case "greenhouse", "greenhouse-form" -> {
                mapping.put("firstName", firstName);
                mapping.put("lastName", lastName);
                mapping.put("email", email);
                mapping.put("phone", phone);
                if (summary != null && !summary.isBlank()) mapping.put("coverLetter", summary);
            }
            case "lever", "lever-form" -> {
                mapping.put("name", fullName);
                mapping.put("email", email);
                mapping.put("phone", phone);
                if (linkedin != null && !linkedin.isBlank()) mapping.put("urls", linkedin);
            }
            case "workday" -> {
                mapping.put("firstName", firstName);
                mapping.put("lastName", lastName);
                mapping.put("email", email);
                mapping.put("phone", phone);
            }
            case "ashby" -> {
                mapping.put("name", fullName);
                mapping.put("email", email);
                mapping.put("phone", phone);
            }
            case "smartrecruiters" -> {
                mapping.put("firstName", firstName);
                mapping.put("lastName", lastName);
                mapping.put("email", email);
                mapping.put("phone", phone);
            }
            default -> {
                mapping.put("firstName", firstName);
                mapping.put("lastName", lastName);
                mapping.put("email", email);
                mapping.put("phone", phone);
                if (linkedin != null && !linkedin.isBlank()) mapping.put("linkedin", linkedin);
                if (portfolio != null && !portfolio.isBlank()) mapping.put("portfolio", portfolio);
            }
        }
        return mapping;
    }

    private String findAndClickApplyButton() {
        var applySelectors = List.of(
            "a[href*='apply']",
            "button:has-text('Apply')",
            "a:has-text('Apply')",
            "[data-apply-url]",
            ".apply-button",
            "#apply-button",
            "a[title*='Apply']",
            "button[title*='Apply']"
        );

        for (var selector : applySelectors) {
            try {
                browserAutomation.clickElement(selector);
                Thread.sleep(2000);
                log.info("Clicked apply button with selector: {}", selector);
                return null;
            } catch (Exception e) {
                // continue to next selector
            }
        }
        return null;
    }

    private void fillFieldByType(String selector, String type, String value) {
        switch (type.toLowerCase()) {
            case "checkbox" -> {
                if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)) {
                    browserAutomation.clickElement(selector);
                }
            }
            case "radio" -> browserAutomation.clickElement(selector);
            case "select" -> browserAutomation.fillField(selector, value);
            case "file" -> {
                if (value != null && !value.isBlank()) {
                    browserAutomation.fillField(selector, value);
                }
            }
            default -> browserAutomation.fillField(selector, value);
        }
    }

    @SuppressWarnings("unchecked")
    private String matchFieldToProfile(String label, String name, String type, Map<String, Object> profile) {
        var fieldText = (label + " " + name).toLowerCase().trim();

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
        if (fieldText.contains("full") && fieldText.contains("name") || fieldText.equals("name") || fieldText.equals("your name")) {
            return (String) profile.get("fullName");
        }
        if (fieldText.contains("email") || fieldText.contains("e-mail")) {
            return (String) profile.get("email");
        }
        if (fieldText.contains("phone") || fieldText.contains("tel") || fieldText.contains("mobile") || fieldText.contains("number")) {
            return (String) profile.get("phone");
        }
        if (fieldText.contains("location") || fieldText.contains("city") || fieldText.contains("address") || fieldText.contains("state") || fieldText.contains("country")) {
            return (String) profile.get("location");
        }
        if (fieldText.contains("linkedin")) {
            return (String) profile.get("linkedinUrl");
        }
        if (fieldText.contains("portfolio") || fieldText.contains("website") || fieldText.contains("github") || fieldText.contains("personal url")) {
            return (String) profile.get("portfolioUrl");
        }
        if (fieldText.contains("summary") || fieldText.contains("about") || fieldText.contains("bio") || fieldText.contains("objective")) {
            var summary = (String) profile.get("summary");
            if (summary != null && !summary.isBlank()) return summary;
            var resumeText = (String) profile.get("resumeText");
            if (resumeText != null && !resumeText.isBlank()) {
                return resumeText.length() > 500 ? resumeText.substring(0, 500) : resumeText;
            }
            return null;
        }
        if (fieldText.contains("headline") || fieldText.contains("title") || fieldText.contains("current role")) {
            return (String) profile.get("headline");
        }
        if (fieldText.contains("year") && fieldText.contains("experience")) {
            return (String) profile.get("yearsExperience");
        }
        if (fieldText.contains("desired") && fieldText.contains("role")) {
            return (String) profile.get("desiredRole");
        }
        if (fieldText.contains("desired") && fieldText.contains("location")) {
            return (String) profile.get("desiredLocation");
        }
        if (fieldText.contains("salary") || fieldText.contains("compensation") || fieldText.contains("expected pay")) {
            var min = (String) profile.get("salaryExpectationMin");
            var max = (String) profile.get("salaryExpectationMax");
            if (min != null && max != null) return min + " - " + max;
            if (min != null) return min;
            return null;
        }
        if (fieldText.contains("employment") && fieldText.contains("type") || fieldText.contains("work type")) {
            return (String) profile.get("employmentType");
        }
        if (fieldText.contains("work preference") || fieldText.contains("remote") || fieldText.contains("work style")) {
            return (String) profile.get("workPreference");
        }
        if (fieldText.contains("skill") || fieldText.contains("technology") || fieldText.contains("tech")) {
            var skills = profile.get("skills");
            if (skills instanceof List<?> skillList && !skillList.isEmpty()) {
                return String.join(", ", skillList.stream().map(Object::toString).toList());
            }
            return null;
        }
        if (fieldText.contains("experience") && !fieldText.contains("year")) {
            var experience = profile.get("experience");
            if (experience instanceof List<?> expList && !expList.isEmpty()) {
                return expList.get(0).toString();
            }
            return null;
        }
        if (fieldText.contains("education") || fieldText.contains("degree") || fieldText.contains("university")) {
            var education = profile.get("education");
            if (education instanceof List<?> eduList && !eduList.isEmpty()) {
                return eduList.get(0).toString();
            }
            return null;
        }
        if (fieldText.contains("certification") || fieldText.contains("license")) {
            var certs = profile.get("certifications");
            if (certs instanceof List<?> certList && !certList.isEmpty()) {
                return String.join(", ", certList.stream().map(Object::toString).toList());
            }
            return null;
        }

        if ("file".equals(type) && (fieldText.contains("resume") || fieldText.contains("cv"))) {
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
