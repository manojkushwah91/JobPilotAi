package com.jobpilot.domain.resume;

import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class ResumeVersion extends BaseAggregateRoot {

    private UUID versionId;
    private UUID resumeId;
    private UUID userId;
    private String jobUrl;
    private String jobTitle;
    private String companyName;
    private String tailoredContent;
    private Map<String, Object> metadata;
    private Instant createdAt;

    private ResumeVersion() {
        super(UUID.randomUUID());
        this.createdAt = Instant.now();
    }

    public static ResumeVersion create(UUID resumeId, UUID userId, String tailoredContent,
                                        String jobUrl, String jobTitle, String companyName) {
        var version = new ResumeVersion();
        version.resumeId = resumeId;
        version.userId = userId;
        version.tailoredContent = tailoredContent;
        version.jobUrl = jobUrl;
        version.jobTitle = jobTitle;
        version.companyName = companyName;
        return version;
    }

    public static ResumeVersion reconstitute(UUID versionId, UUID resumeId, UUID userId,
                                              String tailoredContent, String jobUrl,
                                              String jobTitle, String companyName,
                                              Map<String, Object> metadata, Instant createdAt) {
        var version = new ResumeVersion();
        version.versionId = versionId;
        version.resumeId = resumeId;
        version.userId = userId;
        version.tailoredContent = tailoredContent;
        version.jobUrl = jobUrl;
        version.jobTitle = jobTitle;
        version.companyName = companyName;
        version.metadata = metadata;
        version.createdAt = createdAt;
        return version;
    }

    public UUID versionId() { return versionId; }
    public UUID resumeId() { return resumeId; }
    public UUID userId() { return userId; }
    public String jobUrl() { return jobUrl; }
    public String jobTitle() { return jobTitle; }
    public String companyName() { return companyName; }
    public String tailoredContent() { return tailoredContent; }
    public Map<String, Object> metadata() { return metadata; }
    public Instant createdAt() { return createdAt; }
}
