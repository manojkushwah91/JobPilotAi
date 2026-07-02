package com.jobpilot.application.application.service;

import com.jobpilot.application.application.dto.ApplicationResponse;
import com.jobpilot.application.application.dto.ApplyCommand;
import com.jobpilot.application.application.ports.ApplicationRepository;
import com.jobpilot.application.application.usecase.ApplyUseCase;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.application.resume.ports.ResumeRepository;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.application.Application;
import com.jobpilot.domain.application.ApplicationId;
import com.jobpilot.domain.identity.UserId;
import com.jobpilot.domain.job.JobId;
import com.jobpilot.domain.resume.ResumeId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ApplyService implements ApplyUseCase {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;

    public ApplyService(ApplicationRepository applicationRepository, JobRepository jobRepository,
                         ResumeRepository resumeRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
    }

    @Override
    public ApplicationResponse execute(ApplyCommand command) {
        var jobId = JobId.from(UUID.fromString(command.jobListingId()));
        var job = jobRepository.findById(jobId)
            .orElseThrow(() -> new NotFoundException("JobListing", command.jobListingId()));

        var resumeId = ResumeId.from(UUID.fromString(command.resumeId()));
        var resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new NotFoundException("Resume", command.resumeId()));

        var appId = ApplicationId.generate();
        var userId = UserId.from(UUID.fromString(command.userId()));
        var application = Application.create(appId, userId, jobId);
        application.submit(resumeId);

        applicationRepository.save(application);
        return toResponse(application);
    }

    public static ApplicationResponse toResponse(Application app) {
        return new ApplicationResponse(
            app.applicationId().value().toString(), app.userId().value().toString(),
            app.jobListingId().value().toString(),
            app.resumeId() != null ? app.resumeId().value().toString() : null,
            app.coverLetterId() != null ? app.coverLetterId().value().toString() : null,
            app.status().name(), app.statusHistory(),
            app.automationInfo(), app.salaryOffered(),
            app.appliedAt(), app.createdAt(), app.updatedAt()
        );
    }
}
