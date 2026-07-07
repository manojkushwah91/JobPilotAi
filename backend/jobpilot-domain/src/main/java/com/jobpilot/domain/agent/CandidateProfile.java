package com.jobpilot.domain.agent;

import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CandidateProfile extends BaseAggregateRoot {

    private CandidateProfileId profileId;
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private String location;
    private String headline;
    private String summary;
    private List<String> skills;
    private List<String> experience;
    private List<String> education;
    private List<String> certifications;
    private String resumeText;
    private String resumeFileUrl;
    private String linkedinUrl;
    private String portfolioUrl;
    private Integer yearsExperience;
    private String desiredRole;
    private String desiredLocation;
    private Integer salaryExpectationMin;
    private Integer salaryExpectationMax;
    private String currency;
    private String employmentType;
    private String workPreference;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;

    private CandidateProfile(CandidateProfileId profileId, UUID userId, String fullName, String email) {
        super(profileId.value());
        this.profileId = profileId;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.skills = new ArrayList<>();
        this.experience = new ArrayList<>();
        this.education = new ArrayList<>();
        this.certifications = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static CandidateProfile create(UUID userId, String fullName, String email) {
        return new CandidateProfile(CandidateProfileId.generate(), userId, fullName, email);
    }

    public static CandidateProfile reconstitute(CandidateProfileId profileId, UUID userId, String fullName,
                                                  String email, String phone, String location, String headline,
                                                  String summary, List<String> skills, List<String> experience,
                                                  List<String> education, List<String> certifications,
                                                  String resumeText, String resumeFileUrl,
                                                  String linkedinUrl, String portfolioUrl,
                                                  Integer yearsExperience, String desiredRole,
                                                  String desiredLocation, Integer salaryExpectationMin,
                                                  Integer salaryExpectationMax, String currency,
                                                  String employmentType, String workPreference,
                                                  Map<String, Object> metadata,
                                                  Instant createdAt, Instant updatedAt) {
        var p = new CandidateProfile(profileId, userId, fullName, email);
        p.phone = phone;
        p.location = location;
        p.headline = headline;
        p.summary = summary;
        p.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        p.experience = experience != null ? new ArrayList<>(experience) : new ArrayList<>();
        p.education = education != null ? new ArrayList<>(education) : new ArrayList<>();
        p.certifications = certifications != null ? new ArrayList<>(certifications) : new ArrayList<>();
        p.resumeText = resumeText;
        p.resumeFileUrl = resumeFileUrl;
        p.linkedinUrl = linkedinUrl;
        p.portfolioUrl = portfolioUrl;
        p.yearsExperience = yearsExperience;
        p.desiredRole = desiredRole;
        p.desiredLocation = desiredLocation;
        p.salaryExpectationMin = salaryExpectationMin;
        p.salaryExpectationMax = salaryExpectationMax;
        p.currency = currency;
        p.employmentType = employmentType;
        p.workPreference = workPreference;
        p.metadata = metadata;
        p.createdAt = createdAt;
        p.updatedAt = updatedAt;
        return p;
    }

    public void updateProfile(String fullName, String phone, String location, String headline, String summary) {
        if (fullName != null) this.fullName = fullName;
        if (phone != null) this.phone = phone;
        if (location != null) this.location = location;
        if (headline != null) this.headline = headline;
        if (summary != null) this.summary = summary;
        this.updatedAt = Instant.now();
    }

    public void updateSkills(List<String> skills) {
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    public void updateExperience(List<String> experience) {
        this.experience = experience != null ? new ArrayList<>(experience) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    public void updateEducation(List<String> education) {
        this.education = education != null ? new ArrayList<>(education) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    public void updateCertifications(List<String> certifications) {
        this.certifications = certifications != null ? new ArrayList<>(certifications) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    public void updateResume(String resumeText, String resumeFileUrl) {
        if (resumeText != null) this.resumeText = resumeText;
        if (resumeFileUrl != null) this.resumeFileUrl = resumeFileUrl;
        this.updatedAt = Instant.now();
    }

    public void updateLinks(String linkedinUrl, String portfolioUrl) {
        if (linkedinUrl != null) this.linkedinUrl = linkedinUrl;
        if (portfolioUrl != null) this.portfolioUrl = portfolioUrl;
        this.updatedAt = Instant.now();
    }

    public void updatePreferences(String desiredRole, String desiredLocation,
                                   Integer salaryMin, Integer salaryMax, String currency,
                                   String employmentType, String workPreference) {
        if (desiredRole != null) this.desiredRole = desiredRole;
        if (desiredLocation != null) this.desiredLocation = desiredLocation;
        if (salaryMin != null) this.salaryExpectationMin = salaryMin;
        if (salaryMax != null) this.salaryExpectationMax = salaryMax;
        if (currency != null) this.currency = currency;
        if (employmentType != null) this.employmentType = employmentType;
        if (workPreference != null) this.workPreference = workPreference;
        this.updatedAt = Instant.now();
    }

    public CandidateProfileId profileId() { return profileId; }
    public UUID userId() { return userId; }
    public String fullName() { return fullName; }
    public String email() { return email; }
    public String phone() { return phone; }
    public String location() { return location; }
    public String headline() { return headline; }
    public String summary() { return summary; }
    public List<String> skills() { return List.copyOf(skills); }
    public List<String> experience() { return List.copyOf(experience); }
    public List<String> education() { return List.copyOf(education); }
    public List<String> certifications() { return List.copyOf(certifications); }
    public String resumeText() { return resumeText; }
    public String resumeFileUrl() { return resumeFileUrl; }
    public String linkedinUrl() { return linkedinUrl; }
    public String portfolioUrl() { return portfolioUrl; }
    public Integer yearsExperience() { return yearsExperience; }
    public String desiredRole() { return desiredRole; }
    public String desiredLocation() { return desiredLocation; }
    public Integer salaryExpectationMin() { return salaryExpectationMin; }
    public Integer salaryExpectationMax() { return salaryExpectationMax; }
    public String currency() { return currency; }
    public String employmentType() { return employmentType; }
    public String workPreference() { return workPreference; }
    public Map<String, Object> metadata() { return metadata; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
