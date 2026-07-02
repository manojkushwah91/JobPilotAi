package com.jobpilot.domain.identity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void adminShouldBeAdmin() {
        assertThat(Role.ADMIN.isAdmin()).isTrue();
    }

    @Test
    void freeShouldNotBeAdmin() {
        assertThat(Role.FREE.isAdmin()).isFalse();
    }

    @Test
    void proShouldBeProOrAbove() {
        assertThat(Role.PRO.isProOrAbove()).isTrue();
    }

    @Test
    void freeShouldNotBeProOrAbove() {
        assertThat(Role.FREE.isProOrAbove()).isFalse();
    }

    @Test
    void adminShouldAccessAll() {
        assertThat(Role.ADMIN.canAccess(Role.FREE)).isTrue();
        assertThat(Role.ADMIN.canAccess(Role.ADMIN)).isTrue();
    }

    @Test
    void freeShouldNotAccessPremium() {
        assertThat(Role.FREE.canAccess(Role.PREMIUM)).isFalse();
    }
}
