package com.jobpilot.application.user.service;

import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.identity.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteUserService {

    private final UserRepository userRepository;

    public DeleteUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(UUID userId) {
        var domainId = UserId.from(userId);
        var user = userRepository.findById(domainId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        user.softDelete();
        userRepository.save(user);
    }
}
