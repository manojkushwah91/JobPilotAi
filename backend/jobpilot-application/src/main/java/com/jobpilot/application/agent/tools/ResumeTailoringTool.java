package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.application.resume.ports.ResumeVersionRepository;
import com.jobpilot.domain.resume.ResumeVersion;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class ResumeTailoringTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(ResumeTailoringTool.class);

    private final AiProviderPort aiProvider;
    private final ResumeVersionRepository versionRepository;

    public ResumeTailoringTool(AiProviderPort aiProvider, ResumeVersionRepository versionRepository) {
        this.aiProvider = aiProvider;
        this.versionRepository = versionRepository;
    }

    @Override
    public String name() {
        return "TAILOR_RESUME";
    }

    @Override
    public String description() {
        return "Tailors the resume for a specific job posting to maximize ATS score and saves as a new version";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        log.info("Executing resume tailoring tool");

        var resumeContent = (String) input.getOrDefault("resumeContent", "");
        var jobDescription = (String) input.getOrDefault("jobDescription", "");
        var companyName = (String) input.getOrDefault("companyName", "");
        var jobUrl = (String) input.getOrDefault("jobUrl", "");
        var jobTitle = (String) input.getOrDefault("jobTitle", "");
        var resumeIdStr = (String) input.getOrDefault("resumeId", "");
        var userIdStr = (String) input.getOrDefault("userId", "");

        var systemPrompt = "You are an expert resume tailor. Given a resume and job description, " +
            "optimize the resume to match the job requirements while maintaining authenticity. " +
            "Return a JSON object with: tailoredResume, changes, atsScoreImprovement, keywords.";

        var userPrompt = String.format(
            "Original Resume:\n%s\n\nJob Description:\n%s\n\nCompany: %s",
            resumeContent, jobDescription, companyName
        );

        var result = aiProvider.executePrompt(systemPrompt, userPrompt, null, 0.3, 3000);

        // Save as a new resume version if resumeId and userId are provided
        if (!resumeIdStr.isEmpty() && !userIdStr.isEmpty()) {
            try {
                var resumeId = UUID.fromString(resumeIdStr);
                var userId = UUID.fromString(userIdStr);

                var existing = versionRepository.findByResumeIdAndJobUrl(resumeId, jobUrl);
                if (existing.isEmpty()) {
                    var version = ResumeVersion.create(resumeId, userId, result, jobUrl, jobTitle, companyName);
                    versionRepository.save(version);
                    log.info("Saved tailored resume version for job: {} at {}", jobTitle, companyName);
                } else {
                    log.info("Resume version already exists for job URL: {}", jobUrl);
                }
            } catch (Exception e) {
                log.error("Failed to save resume version: {}", e.getMessage());
            }
        }

        return Map.of(
            "status", "success",
            "tailoredContent", result,
            "companyName", companyName
        );
    }

    @Override
    public int timeoutSeconds() {
        return 90;
    }
}
