package com.jobpilot.interfaces.rest.v1.agent;

import com.jobpilot.application.agent.service.CandidateProfileService;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.infrastructure.security.JwtPrincipal;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
public class CandidateProfileController {

    private final CandidateProfileService profileService;

    public CandidateProfileController(CandidateProfileService profileService) {
        this.profileService = profileService;
    }

    @RateLimited(capacity = 100)
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var profile = profileService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(profile.<Map<String, Object>>map(p -> {
            var m = new java.util.HashMap<String, Object>();
            m.put("id", p.profileId().value().toString());
            m.put("fullName", p.fullName() != null ? p.fullName() : "");
            m.put("email", p.email() != null ? p.email() : "");
            m.put("phone", p.phone() != null ? p.phone() : "");
            m.put("location", p.location() != null ? p.location() : "");
            m.put("headline", p.headline() != null ? p.headline() : "");
            m.put("summary", p.summary() != null ? p.summary() : "");
            m.put("skills", p.skills());
            m.put("experience", p.experience());
            m.put("education", p.education());
            m.put("certifications", p.certifications());
            m.put("resumeText", p.resumeText() != null ? p.resumeText() : "");
            m.put("resumeFileUrl", p.resumeFileUrl() != null ? p.resumeFileUrl() : "");
            m.put("linkedinUrl", p.linkedinUrl() != null ? p.linkedinUrl() : "");
            m.put("portfolioUrl", p.portfolioUrl() != null ? p.portfolioUrl() : "");
            m.put("yearsExperience", p.yearsExperience() != null ? p.yearsExperience() : 0);
            m.put("desiredRole", p.desiredRole() != null ? p.desiredRole() : "");
            m.put("desiredLocation", p.desiredLocation() != null ? p.desiredLocation() : "");
            m.put("salaryExpectationMin", p.salaryExpectationMin() != null ? p.salaryExpectationMin() : 0);
            m.put("salaryExpectationMax", p.salaryExpectationMax() != null ? p.salaryExpectationMax() : 0);
            m.put("currency", p.currency() != null ? p.currency() : "USD");
            m.put("employmentType", p.employmentType() != null ? p.employmentType() : "");
            m.put("workPreference", p.workPreference() != null ? p.workPreference() : "");
            return m;
        }).orElse(Map.of())));
    }

    @RateLimited(capacity = 50)
    @PutMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var fullName = (String) body.getOrDefault("fullName", "");
        var phone = (String) body.getOrDefault("phone", "");
        var location = (String) body.getOrDefault("location", "");
        var headline = (String) body.getOrDefault("headline", "");
        var summary = (String) body.getOrDefault("summary", "");
        var profile = profileService.updateBasicInfo(userId, fullName, phone, location, headline, summary);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "id", profile.profileId().value().toString(),
            "fullName", profile.fullName(),
            "email", profile.email()
        )));
    }

    @RateLimited(capacity = 50)
    @PutMapping("/skills")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSkills(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        @SuppressWarnings("unchecked")
        var skills = (List<String>) body.getOrDefault("skills", List.of());
        var profile = profileService.updateSkills(userId, skills);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("skills", profile.skills())));
    }

    @RateLimited(capacity = 50)
    @PutMapping("/experience")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateExperience(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        @SuppressWarnings("unchecked")
        var experience = (List<String>) body.getOrDefault("experience", List.of());
        var profile = profileService.updateExperience(userId, experience);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("experience", profile.experience())));
    }

    @RateLimited(capacity = 50)
    @PutMapping("/education")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateEducation(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        @SuppressWarnings("unchecked")
        var education = (List<String>) body.getOrDefault("education", List.of());
        var profile = profileService.updateEducation(userId, education);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("education", profile.education())));
    }

    @RateLimited(capacity = 50)
    @PutMapping("/resume")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateResume(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var resumeText = (String) body.getOrDefault("resumeText", "");
        var resumeFileUrl = (String) body.getOrDefault("resumeFileUrl", "");
        var profile = profileService.updateResume(userId, resumeText, resumeFileUrl);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "resumeText", profile.resumeText() != null ? profile.resumeText() : "",
            "resumeFileUrl", profile.resumeFileUrl() != null ? profile.resumeFileUrl() : ""
        )));
    }

    @RateLimited(capacity = 50)
    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePreferences(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        var desiredRole = (String) body.getOrDefault("desiredRole", "");
        var desiredLocation = (String) body.getOrDefault("desiredLocation", "");
        var salaryMin = body.get("salaryExpectationMin") != null ? ((Number) body.get("salaryExpectationMin")).intValue() : null;
        var salaryMax = body.get("salaryExpectationMax") != null ? ((Number) body.get("salaryExpectationMax")).intValue() : null;
        var currency = (String) body.getOrDefault("currency", "USD");
        var employmentType = (String) body.getOrDefault("employmentType", "");
        var workPreference = (String) body.getOrDefault("workPreference", "");
        var profile = profileService.updatePreferences(userId, desiredRole, desiredLocation,
            salaryMin, salaryMax, currency, employmentType, workPreference);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "desiredRole", profile.desiredRole() != null ? profile.desiredRole() : "",
            "desiredLocation", profile.desiredLocation() != null ? profile.desiredLocation() : "",
            "salaryExpectationMin", profile.salaryExpectationMin() != null ? profile.salaryExpectationMin() : 0,
            "salaryExpectationMax", profile.salaryExpectationMax() != null ? profile.salaryExpectationMax() : 0
        )));
    }

    @RateLimited(capacity = 50)
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            @AuthenticationPrincipal JwtPrincipal principal) {
        var userId = UUID.fromString(principal.userId());
        profileService.delete(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
