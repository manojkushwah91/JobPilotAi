package com.jobpilot.domain.identity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class OAuthProviderTest {

    @Test
    void shouldCreateFromProviderAndUserId() {
        var provider = OAuthProvider.from(OAuthProvider.Provider.GOOGLE, "google-id-123");
        assertThat(provider.provider()).isEqualTo(OAuthProvider.Provider.GOOGLE);
        assertThat(provider.providerUserId()).isEqualTo("google-id-123");
    }

    @Test
    void shouldBeEqualForSameValues() {
        var p1 = OAuthProvider.from(OAuthProvider.Provider.GITHUB, "gh-123");
        var p2 = OAuthProvider.from(OAuthProvider.Provider.GITHUB, "gh-123");
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    void shouldNotBeEqualForDifferentProviders() {
        var p1 = OAuthProvider.from(OAuthProvider.Provider.GOOGLE, "id-123");
        var p2 = OAuthProvider.from(OAuthProvider.Provider.GITHUB, "id-123");
        assertThat(p1).isNotEqualTo(p2);
    }
}
