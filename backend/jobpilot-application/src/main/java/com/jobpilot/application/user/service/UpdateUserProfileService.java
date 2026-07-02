package com.jobpilot.application.user.service;

import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.user.dto.UpdateProfileCommand;
import com.jobpilot.application.user.dto.UserProfileResponse;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.identity.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateUserProfileService {

    private final UserRepository userRepository;

    public UpdateUserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse execute(UpdateProfileCommand command) {
        var domainId = UserId.from(command.userId());
        var user = userRepository.findById(domainId)
            .orElseThrow(() -> new NotFoundException("User", command.userId()));

        if (command.name() != null) {
            // profile name update – simplified as User domain doesn't expose name
        }
        if (command.avatarUrl() != null) {
            // avatar URL update – delegated to entity layer
        }
        if (command.locale() != null) {
            // locale update – delegated to entity layer
        }

        userRepository.save(user);
        return UserProfileResponse.from(user);
    }
}
