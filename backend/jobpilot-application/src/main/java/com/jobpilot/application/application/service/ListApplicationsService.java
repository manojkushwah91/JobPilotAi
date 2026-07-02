package com.jobpilot.application.application.service;

import com.jobpilot.application.application.dto.ApplicationResponse;
import com.jobpilot.application.application.dto.ListApplicationsCommand;
import com.jobpilot.application.application.ports.ApplicationRepository;
import com.jobpilot.application.application.usecase.ListApplicationsUseCase;
import com.jobpilot.domain.identity.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListApplicationsService implements ListApplicationsUseCase {

    private final ApplicationRepository applicationRepository;

    public ListApplicationsService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public List<ApplicationResponse> execute(ListApplicationsCommand command) {
        var userId = UserId.from(UUID.fromString(command.userId()));
        return applicationRepository.findByUserId(userId).stream()
            .filter(a -> !a.isDeleted())
            .map(ApplyService::toResponse)
            .toList();
    }
}
