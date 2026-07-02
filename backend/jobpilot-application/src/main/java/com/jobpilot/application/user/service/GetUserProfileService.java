package com.jobpilot.application.user.service;

import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.user.dto.UserProfileResponse;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.identity.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetUserProfileService {

    private final UserRepository userRepository;

    public GetUserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse execute(UUID userId) {
        var domainId = UserId.from(userId);
        var user = userRepository.findById(domainId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        return UserProfileResponse.from(user);
    }
}
