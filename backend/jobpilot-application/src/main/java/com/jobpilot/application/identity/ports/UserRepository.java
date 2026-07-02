package com.jobpilot.application.identity.ports;

import com.jobpilot.domain.identity.Email;
import com.jobpilot.domain.identity.User;
import com.jobpilot.domain.identity.UserId;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(Email email);
    boolean existsByEmail(Email email);
}
