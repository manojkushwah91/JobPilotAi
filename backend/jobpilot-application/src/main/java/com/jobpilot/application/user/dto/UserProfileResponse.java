package com.jobpilot.application.user.dto;

import com.jobpilot.domain.identity.User;

import java.time.Instant;

public record UserProfileResponse(
    String id,
    String email,
    String name,
    String role,
    String tier,
    String avatarUrl,
    String locale,
    Instant createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
            user.userId().value().toString(),
            user.email().value(),
            user.name(),
            user.role().name(),
            user.role().name(),
            null,
            null,
            user.createdAt()
        );
    }
}
