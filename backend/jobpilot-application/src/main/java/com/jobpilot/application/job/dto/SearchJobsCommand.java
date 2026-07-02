package com.jobpilot.application.job.dto;

import java.util.List;

public record SearchJobsCommand(
    String query,
    List<String> skills,
    String employmentType,
    String experienceLevel,
    String industry,
    String location,
    int page,
    int size
) {
    public SearchJobsCommand {
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 20;
    }
}
