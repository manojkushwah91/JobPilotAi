package com.jobpilot.domain.identity;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.Objects;

public final class OAuthProvider extends BaseValueObject {

    private final Provider provider;
    private final String providerUserId;

    private OAuthProvider(Provider provider, String providerUserId) {
        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.providerUserId = Objects.requireNonNull(providerUserId, "providerUserId must not be null");
    }

    public static OAuthProvider from(Provider provider, String providerUserId) {
        return new OAuthProvider(provider, providerUserId);
    }

    public Provider provider() { return provider; }
    public String providerUserId() { return providerUserId; }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{provider, providerUserId};
    }

    public enum Provider {
        GOOGLE,
        GITHUB,
        LINKEDIN,
        MICROSOFT
    }
}
