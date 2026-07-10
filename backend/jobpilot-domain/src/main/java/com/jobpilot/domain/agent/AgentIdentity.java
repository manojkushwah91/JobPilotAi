package com.jobpilot.domain.agent;

import java.time.Instant;
import java.util.*;

public class AgentIdentity {
    private final UUID userId;
    private final String fullName;
    private final String email;
    private String headline;
    private String summary;
    private List<String> skills;
    private List<ExperienceEntry> experience;
    private List<EducationEntry> education;
    private List<String> certifications;
    private List<String> projects;
    private String preferredLocation;
    private int salaryMin;
    private int salaryMax;
    private List<String> preferredCompanies;
    private List<String> avoidCompanies;
    private String employmentType;
    private String workAuthorization;
    private String careerGoal;
    private Instant lastUpdated;

    public AgentIdentity(UUID userId, String fullName, String email) {
        this.userId = Objects.requireNonNull(userId);
        this.fullName = Objects.requireNonNull(fullName);
        this.email = Objects.requireNonNull(email);
        this.skills = new ArrayList<>();
        this.experience = new ArrayList<>();
        this.education = new ArrayList<>();
        this.certifications = new ArrayList<>();
        this.projects = new ArrayList<>();
        this.preferredCompanies = new ArrayList<>();
        this.avoidCompanies = new ArrayList<>();
        this.lastUpdated = Instant.now();
    }

    public UUID userId() { return userId; }
    public String fullName() { return fullName; }
    public String email() { return email; }
    public String headline() { return headline; }
    public void headline(String h) { this.headline = h; }
    public String summary() { return summary; }
    public void summary(String s) { this.summary = s; }
    public List<String> skills() { return Collections.unmodifiableList(skills); }
    public void skills(List<String> s) { this.skills = new ArrayList<>(s); }
    public List<ExperienceEntry> experience() { return Collections.unmodifiableList(experience); }
    public void experience(List<ExperienceEntry> e) { this.experience = new ArrayList<>(e); }
    public List<EducationEntry> education() { return Collections.unmodifiableList(education); }
    public void education(List<EducationEntry> e) { this.education = new ArrayList<>(e); }
    public List<String> certifications() { return Collections.unmodifiableList(certifications); }
    public void certifications(List<String> c) { this.certifications = new ArrayList<>(c); }
    public List<String> projects() { return Collections.unmodifiableList(projects); }
    public void projects(List<String> p) { this.projects = new ArrayList<>(p); }
    public String preferredLocation() { return preferredLocation; }
    public void preferredLocation(String l) { this.preferredLocation = l; }
    public int salaryMin() { return salaryMin; }
    public void salaryMin(int s) { this.salaryMin = s; }
    public int salaryMax() { return salaryMax; }
    public void salaryMax(int s) { this.salaryMax = s; }
    public List<String> preferredCompanies() { return Collections.unmodifiableList(preferredCompanies); }
    public void preferredCompanies(List<String> c) { this.preferredCompanies = new ArrayList<>(c); }
    public List<String> avoidCompanies() { return Collections.unmodifiableList(avoidCompanies); }
    public void avoidCompanies(List<String> c) { this.avoidCompanies = new ArrayList<>(c); }
    public String employmentType() { return employmentType; }
    public void employmentType(String t) { this.employmentType = t; }
    public String workAuthorization() { return workAuthorization; }
    public void workAuthorization(String w) { this.workAuthorization = w; }
    public String careerGoal() { return careerGoal; }
    public void careerGoal(String g) { this.careerGoal = g; }
    public Instant lastUpdated() { return lastUpdated; }

    public String toContextBlock() {
        var sb = new StringBuilder();
        sb.append("User: ").append(fullName).append("\n");
        if (headline != null) sb.append("Headline: ").append(headline).append("\n");
        if (summary != null) sb.append("Summary: ").append(summary).append("\n");
        sb.append("Skills: ").append(String.join(", ", skills)).append("\n");
        sb.append("Experience: ").append(experience.size()).append(" positions\n");
        for (var e : experience) {
            sb.append("  - ").append(e.title()).append(" at ").append(e.company())
              .append(" (").append(e.startDate()).append(" - ").append(e.endDate().orElse("present")).append(")\n");
        }
        if (preferredLocation != null) sb.append("Location: ").append(preferredLocation).append("\n");
        sb.append("Salary: $").append(salaryMin).append(" - $").append(salaryMax).append("\n");
        if (careerGoal != null) sb.append("Goal: ").append(careerGoal).append("\n");
        if (!preferredCompanies.isEmpty()) sb.append("Target companies: ").append(String.join(", ", preferredCompanies)).append("\n");
        return sb.toString();
    }

    public static class ExperienceEntry {
        private final String title;
        private final String company;
        private final String startDate;
        private final String endDate;
        private final String description;

        public ExperienceEntry(String title, String company, String startDate, String endDate, String description) {
            this.title = title;
            this.company = company;
            this.startDate = startDate;
            this.endDate = endDate;
            this.description = description;
        }

        public String title() { return title; }
        public String company() { return company; }
        public String startDate() { return startDate; }
        public Optional<String> endDate() { return Optional.ofNullable(endDate); }
        public String description() { return description; }
    }

    public static class EducationEntry {
        private final String degree;
        private final String institution;
        private final String field;
        private final String graduationYear;

        public EducationEntry(String degree, String institution, String field, String graduationYear) {
            this.degree = degree;
            this.institution = institution;
            this.field = field;
            this.graduationYear = graduationYear;
        }

        public String degree() { return degree; }
        public String institution() { return institution; }
        public String field() { return field; }
        public String graduationYear() { return graduationYear; }
    }
}
