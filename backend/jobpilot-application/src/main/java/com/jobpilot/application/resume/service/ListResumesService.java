package com.jobpilot.application.resume.service;

import com.jobpilot.application.resume.dto.ListResumesCommand;
import com.jobpilot.application.resume.dto.ResumeResponse;
import com.jobpilot.application.resume.ports.ResumeRepository;
import com.jobpilot.application.resume.usecase.ListResumesUseCase;
import com.jobpilot.domain.identity.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListResumesService implements ListResumesUseCase {

    private final ResumeRepository resumeRepository;

    public ListResumesService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    @Override
    public List<ResumeResponse> execute(ListResumesCommand command) {
        var userId = UserId.from(UUID.fromString(command.userId()));
        return resumeRepository.findByUserId(userId).stream()
            .filter(r -> !r.isDeleted())
            .map(CreateResumeService::toResponse)
            .toList();
    }
}
