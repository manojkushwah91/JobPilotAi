package com.jobpilot.infrastructure.persistence.job;

import com.jobpilot.domain.job.EmploymentType;
import com.jobpilot.domain.job.ExperienceLevel;
import com.jobpilot.domain.job.JobId;
import com.jobpilot.domain.job.JobListing;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "job_listings")
public class JobListingEntity extends BaseJpaEntity {

    @Id private UUID id;
    @Column(name = "source", nullable = false) private String source;
    @Column(name = "source_id") private String sourceId;
    @Column(name = "title", nullable = false) private String title;
    @Column(name = "company_name", nullable = false) private String companyName;
    @Column(name = "company_logo_url") private String companyLogoUrl;
    @Column(name = "company_id") private UUID companyId;
    @Column(name = "location", columnDefinition = "jsonb") private String location;
    @Column(name = "salary", columnDefinition = "jsonb") private String salary;
    @Column(name = "description", nullable = false, columnDefinition = "text") private String description;
    @Column(name = "requirements", columnDefinition = "jsonb") private String requirements;
    @Column(name = "responsibilities", columnDefinition = "jsonb") private String responsibilities;
    @Column(name = "benefits", columnDefinition = "jsonb") private String benefits;
    @Enumerated(EnumType.STRING) @Column(name = "employment_type") private EmploymentType employmentType;
    @Enumerated(EnumType.STRING) @Column(name = "experience_level") private ExperienceLevel experienceLevel;
    @Column(name = "industry") private String industry;
    @Column(name = "skills", columnDefinition = "jsonb") private String skills;
    @Column(name = "application_url") private String applicationUrl;
    @Column(name = "posted_at") private Instant postedAt;
    @Column(name = "is_active", nullable = false) private boolean isActive;

    protected JobListingEntity() {}

    public static JobListingEntity fromDomain(JobListing job) {
        var e = new JobListingEntity();
        e.id = job.jobId().value();
        e.source = job.source();
        e.sourceId = job.sourceId();
        e.title = job.title();
        e.companyName = job.companyName();
        e.companyLogoUrl = job.companyLogoUrl();
        e.companyId = job.companyId() != null ? job.companyId().value() : null;
        e.location = toJson(job.location());
        e.salary = toJson(job.salary());
        e.description = job.description();
        e.requirements = toJson(job.requirements());
        e.responsibilities = toJson(job.responsibilities());
        e.benefits = toJson(job.benefits());
        e.employmentType = job.employmentType();
        e.experienceLevel = job.experienceLevel();
        e.industry = job.industry();
        e.skills = toJson(job.skills());
        e.applicationUrl = job.applicationUrl();
        e.postedAt = job.postedAt();
        e.isActive = job.isActive();
        return e;
    }

    public JobListing toDomain() {
        return JobListing.reconstitute(
            JobId.from(id), source, sourceId, title, companyName, companyLogoUrl,
            companyId != null ? JobId.from(companyId) : null,
            fromJson(location), fromJson(salary), description,
            fromJsonList(requirements), fromJsonList(responsibilities), fromJsonList(benefits),
            employmentType, experienceLevel, industry, fromJsonList(skills),
            applicationUrl, postedAt, isActive, createdAt, updatedAt
        );
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    private static String toJson(Object obj) {
        try { return obj != null ? MAPPER.writeValueAsString(obj) : null; }
        catch (Exception e) { throw new RuntimeException("JSON serialization error", e); }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> fromJson(String json) {
        try { return json != null ? MAPPER.readValue(json, Map.class) : null; }
        catch (Exception e) { throw new RuntimeException("JSON parse error", e); }
    }

    @SuppressWarnings("unchecked")
    private static List<String> fromJsonList(String json) {
        try { return json != null ? MAPPER.readValue(json, List.class) : List.of(); }
        catch (Exception e) { throw new RuntimeException("JSON parse error", e); }
    }
}
