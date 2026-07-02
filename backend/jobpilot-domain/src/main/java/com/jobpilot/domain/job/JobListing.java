package com.jobpilot.domain.job;

import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class JobListing extends BaseAggregateRoot {

    private JobId jobId;
    private String source;
    private String sourceId;
    private String title;
    private String companyName;
    private String companyLogoUrl;
    private JobId companyId;
    private Map<String, Object> location;
    private Map<String, Object> salary;
    private String description;
    private List<String> requirements;
    private List<String> responsibilities;
    private List<String> benefits;
    private EmploymentType employmentType;
    private ExperienceLevel experienceLevel;
    private String industry;
    private List<String> skills;
    private String applicationUrl;
    private Instant postedAt;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private JobListing(JobId jobId, String source, String title, String companyName, String description) {
        super(jobId.value());
        this.jobId = jobId;
        this.source = source;
        this.title = title;
        this.companyName = companyName;
        this.description = description;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static JobListing create(JobId jobId, String source, String title, String companyName, String description) {
        return new JobListing(jobId, source, title, companyName, description);
    }

    public static JobListing reconstitute(JobId jobId, String source, String sourceId, String title,
                                           String companyName, String companyLogoUrl, JobId companyId,
                                           Map<String, Object> location, Map<String, Object> salary,
                                           String description, List<String> requirements,
                                           List<String> responsibilities, List<String> benefits,
                                           EmploymentType employmentType, ExperienceLevel experienceLevel,
                                           String industry, List<String> skills, String applicationUrl,
                                           Instant postedAt, boolean active, Instant createdAt, Instant updatedAt) {
        var jl = new JobListing(jobId, source, title, companyName, description);
        jl.sourceId = sourceId;
        jl.companyLogoUrl = companyLogoUrl;
        jl.companyId = companyId;
        jl.location = location;
        jl.salary = salary;
        jl.requirements = requirements;
        jl.responsibilities = responsibilities;
        jl.benefits = benefits;
        jl.employmentType = employmentType;
        jl.experienceLevel = experienceLevel;
        jl.industry = industry;
        jl.skills = skills;
        jl.applicationUrl = applicationUrl;
        jl.postedAt = postedAt;
        jl.active = active;
        return jl;
    }

    public void updateDetails(String title, String description, String companyName,
                               Map<String, Object> location, Map<String, Object> salary,
                               EmploymentType employmentType, ExperienceLevel experienceLevel,
                               String industry, List<String> skills, String applicationUrl) {
        this.title = title;
        this.description = description;
        this.companyName = companyName;
        this.location = location;
        this.salary = salary;
        this.employmentType = employmentType;
        this.experienceLevel = experienceLevel;
        this.industry = industry;
        this.skills = skills;
        this.applicationUrl = applicationUrl;
        this.updatedAt = Instant.now();
    }

    public void deactivate() { this.active = false; this.updatedAt = Instant.now(); }
    public void activate() { this.active = true; this.updatedAt = Instant.now(); }

    public JobId jobId() { return jobId; }
    public String source() { return source; }
    public String sourceId() { return sourceId; }
    public String title() { return title; }
    public String companyName() { return companyName; }
    public String companyLogoUrl() { return companyLogoUrl; }
    public JobId companyId() { return companyId; }
    public Map<String, Object> location() { return location; }
    public Map<String, Object> salary() { return salary; }
    public String description() { return description; }
    public List<String> requirements() { return requirements; }
    public List<String> responsibilities() { return responsibilities; }
    public List<String> benefits() { return benefits; }
    public EmploymentType employmentType() { return employmentType; }
    public ExperienceLevel experienceLevel() { return experienceLevel; }
    public String industry() { return industry; }
    public List<String> skills() { return skills; }
    public String applicationUrl() { return applicationUrl; }
    public Instant postedAt() { return postedAt; }
    public boolean isActive() { return active; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
