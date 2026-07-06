package com.jobpilot.application.agent.ports;

import com.jobpilot.domain.agent.CandidateProfile;
import com.jobpilot.domain.agent.CandidateProfileId;

import java.util.Optional;
import java.util.UUID;

public interface CandidateProfileRepository {

    CandidateProfile save(CandidateProfile profile);

    Optional<CandidateProfile> findById(CandidateProfileId profileId);

    Optional<CandidateProfile> findByUserId(UUID userId);

    void delete(CandidateProfileId profileId);
}
