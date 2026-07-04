package com.jobpilot.application.resume.dto;

import java.util.UUID;

public record UploadResumeResponse(
    String resumeId,
    String title,
    String filePath,
    long fileSize,
    int sectionCount
) {
    public static UploadResumeResponse from(UUID resumeId, String title, String filePath, long fileSize, int sectionCount) {
        return new UploadResumeResponse(resumeId.toString(), title, filePath, fileSize, sectionCount);
    }
}
