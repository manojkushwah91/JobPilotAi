package com.jobpilot.domain.agent;

import com.jobpilot.domain.shared.BaseValueObject;

import java.util.Objects;
import java.util.UUID;

public class CandidateProfileId extends BaseValueObject {

    private final UUID value;

    private CandidateProfileId(UUID value) {
        this.value = value;
    }

    public static CandidateProfileId generate() {
        return new CandidateProfileId(UUID.randomUUID());
    }

    public static CandidateProfileId from(UUID value) {
        return new CandidateProfileId(Objects.requireNonNull(value));
    }

    public static CandidateProfileId fromString(String value) {
        return new CandidateProfileId(UUID.fromString(value));
    }

    public UUID value() {
        return value;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[]{value};
    }
}
