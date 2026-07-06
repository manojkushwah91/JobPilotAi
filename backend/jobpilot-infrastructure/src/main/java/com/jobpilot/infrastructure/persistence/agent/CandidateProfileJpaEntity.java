package com.jobpilot.infrastructure.persistence.agent;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "candidate_profiles")
public class CandidateProfileJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String phone;

    private String location;

    private String headline;

    @Column(columnDefinition = "text")
    private String summary;

    @Column(columnDefinition = "text")
    private String skills;

    @Column(columnDefinition = "text")
    private String experience;

    @Column(columnDefinition = "text")
    private String education;

    @Column(columnDefinition = "text")
    private String certifications;

    @Column(name = "resume_text", columnDefinition = "text")
    private String resumeText;

    @Column(name = "resume_file_url")
    private String resumeFileUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "desired_role")
    private String desiredRole;

    @Column(name = "desired_location")
    private String desiredLocation;

    @Column(name = "salary_expectation_min")
    private Integer salaryExpectationMin;

    @Column(name = "salary_expectation_max")
    private Integer salaryExpectationMax;

    private String currency;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "work_preference")
    private String workPreference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CandidateProfileJpaEntity() {}

    public static CandidateProfileJpaEntity fromDomain(com.jobpilot.domain.agent.CandidateProfile profile) {
        var entity = new CandidateProfileJpaEntity();
        entity.id = profile.profileId().value();
        entity.userId = profile.userId();
        entity.fullName = profile.fullName();
        entity.email = profile.email();
        entity.phone = profile.phone();
        entity.location = profile.location();
        entity.headline = profile.headline();
        entity.summary = profile.summary();
        entity.skills = profile.skills().toString();
        entity.experience = profile.experience().toString();
        entity.education = profile.education().toString();
        entity.certifications = profile.certifications().toString();
        entity.resumeText = profile.resumeText();
        entity.resumeFileUrl = profile.resumeFileUrl();
        entity.linkedinUrl = profile.linkedinUrl();
        entity.portfolioUrl = profile.portfolioUrl();
        entity.yearsExperience = profile.yearsExperience();
        entity.desiredRole = profile.desiredRole();
        entity.desiredLocation = profile.desiredLocation();
        entity.salaryExpectationMin = profile.salaryExpectationMin();
        entity.salaryExpectationMax = profile.salaryExpectationMax();
        entity.currency = profile.currency();
        entity.employmentType = profile.employmentType();
        entity.workPreference = profile.workPreference();
        entity.createdAt = profile.createdAt();
        entity.updatedAt = profile.updatedAt();
        return entity;
    }

    public com.jobpilot.domain.agent.CandidateProfile toDomain() {
        return com.jobpilot.domain.agent.CandidateProfile.reconstitute(
            com.jobpilot.domain.agent.CandidateProfileId.from(id),
            userId, fullName, email, phone, location, headline, summary,
            parseList(skills), parseList(experience), parseList(education), parseList(certifications),
            resumeText, resumeFileUrl, linkedinUrl, portfolioUrl,
            yearsExperience, desiredRole, desiredLocation,
            salaryExpectationMin, salaryExpectationMax, currency,
            employmentType, workPreference,
            null, createdAt, updatedAt
        );
    }

    private java.util.List<String> parseList(String value) {
        if (value == null || value.isBlank()) return java.util.List.of();
        return java.util.Arrays.stream(value.replace("[", "").replace("]", "").split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }
    public String getResumeText() { return resumeText; }
    public void setResumeText(String resumeText) { this.resumeText = resumeText; }
    public String getResumeFileUrl() { return resumeFileUrl; }
    public void setResumeFileUrl(String resumeFileUrl) { this.resumeFileUrl = resumeFileUrl; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }
    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }
    public String getDesiredRole() { return desiredRole; }
    public void setDesiredRole(String desiredRole) { this.desiredRole = desiredRole; }
    public String getDesiredLocation() { return desiredLocation; }
    public void setDesiredLocation(String desiredLocation) { this.desiredLocation = desiredLocation; }
    public Integer getSalaryExpectationMin() { return salaryExpectationMin; }
    public void setSalaryExpectationMin(Integer salaryExpectationMin) { this.salaryExpectationMin = salaryExpectationMin; }
    public Integer getSalaryExpectationMax() { return salaryExpectationMax; }
    public void setSalaryExpectationMax(Integer salaryExpectationMax) { this.salaryExpectationMax = salaryExpectationMax; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public String getWorkPreference() { return workPreference; }
    public void setWorkPreference(String workPreference) { this.workPreference = workPreference; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
