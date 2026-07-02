package com.jobpilot.application.identity.service;

import com.jobpilot.application.identity.dto.ChangePasswordCommand;
import com.jobpilot.application.identity.ports.PasswordEncoder;
import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.application.identity.usecase.ChangePasswordUseCase;
import com.jobpilot.common.exception.UnauthorizedException;
import com.jobpilot.domain.identity.PasswordHash;
import com.jobpilot.domain.identity.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Void execute(ChangePasswordCommand command) {
        var userId = UserId.from(java.util.UUID.fromString(command.userId()));
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!passwordEncoder.matches(command.currentPassword(), user.passwordHash().value())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        var encodedPassword = passwordEncoder.encode(command.newPassword());
        var newPasswordHash = PasswordHash.from(encodedPassword);
        user.updatePassword(newPasswordHash);
        userRepository.save(user);

        return null;
    }
}
