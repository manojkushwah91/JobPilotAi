package com.jobpilot.domain.identity;

public enum Role {
    FREE,
    PREMIUM,
    PRO,
    ADMIN;

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isProOrAbove() {
        return this == PRO || this == ADMIN;
    }

    public boolean canAccess(Role required) {
        return this.ordinal() >= required.ordinal();
    }
}
