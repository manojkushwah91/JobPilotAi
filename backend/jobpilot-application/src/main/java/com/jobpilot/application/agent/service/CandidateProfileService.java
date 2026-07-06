package com.jobpilot.application.agent.service;

import com.jobpilot.application.agent.ports.CandidateProfileRepository;
import com.jobpilot.domain.agent.CandidateProfile;
import com.jobpilot.domain.agent.CandidateProfileId;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CandidateProfileService {

    private final CandidateProfileRepository repository;

    public CandidateProfileService(CandidateProfileRepository repository) {
        this.repository = repository;
    }

    public CandidateProfile getOrCreate(UUID userId, String fullName, String email) {
        return repository.findByUserId(userId)
            .orElseGet(() -> repository.save(CandidateProfile.create(userId, fullName, email)));
    }

    public Optional<CandidateProfile> getByUserId(UUID userId) {
        return repository.findByUserId(userId);
    }

    public CandidateProfile save(CandidateProfile profile) {
        return repository.save(profile);
    }

    public CandidateProfile updateBasicInfo(UUID userId, String fullName, String phone,
                                              String location, String headline, String summary) {
        var profile = repository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
        profile.updateProfile(fullName, phone, location, headline, summary);
        return repository.save(profile);
    }

    public CandidateProfile updateSkills(UUID userId, java.util.List<String> skills) {
        var profile = repository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
        profile.updateSkills(skills);
        return repository.save(profile);
    }

    public CandidateProfile updateExperience(UUID userId, java.util.List<String> experience) {
        var profile = repository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
        profile.updateExperience(experience);
        return repository.save(profile);
    }

    public CandidateProfile updateEducation(UUID userId, java.util.List<String> education) {
        var profile = repository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
        profile.updateEducation(education);
        return repository.save(profile);
    }

    public CandidateProfile updateResume(UUID userId, String resumeText, String resumeFileUrl) {
        var profile = repository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
        profile.updateResume(resumeText, resumeFileUrl);
        return repository.save(profile);
    }

    public CandidateProfile updatePreferences(UUID userId, String desiredRole, String desiredLocation,
                                                Integer salaryMin, Integer salaryMax, String currency,
                                                String employmentType, String workPreference) {
        var profile = repository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
        profile.updatePreferences(desiredRole, desiredLocation, salaryMin, salaryMax, currency, employmentType, workPreference);
        return repository.save(profile);
    }

    public void delete(UUID userId) {
        var profile = repository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
        repository.delete(profile.profileId());
    }
}
