package com.jobpilot.infrastructure.security;

import java.util.List;

public record JwtPrincipal(String userId, String email, String tier, List<String> roles) {

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isPro() {
        return hasRole("PRO") || isAdmin();
    }

    public boolean isPremium() {
        return hasRole("PREMIUM") || isPro();
    }
}
