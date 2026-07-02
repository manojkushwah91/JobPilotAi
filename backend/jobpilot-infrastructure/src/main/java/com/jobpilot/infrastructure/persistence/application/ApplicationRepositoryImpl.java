package com.jobpilot.infrastructure.persistence.application;

import com.jobpilot.application.application.ports.ApplicationRepository;
import com.jobpilot.domain.application.Application;
import com.jobpilot.domain.application.ApplicationId;
import com.jobpilot.domain.identity.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ApplicationRepositoryImpl implements ApplicationRepository {

    private final ApplicationJpaRepository jpaRepository;

    public ApplicationRepositoryImpl(ApplicationJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }

    @Override
    public Application save(Application application) {
        return jpaRepository.save(ApplicationEntity.fromDomain(application)).toDomain();
    }

    @Override
    public Optional<Application> findById(ApplicationId id) {
        return jpaRepository.findById(id.value()).map(ApplicationEntity::toDomain);
    }

    @Override
    public List<Application> findByUserId(UserId userId) {
        return jpaRepository.findByUserIdOrderByUpdatedAtDesc(userId.value()).stream()
            .map(ApplicationEntity::toDomain).toList();
    }

    @Override
    public List<Application> findByUserIdAndStatus(UserId userId, String status) {
        return jpaRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(userId.value(), status).stream()
            .map(ApplicationEntity::toDomain).toList();
    }

    @Override
    public void delete(Application application) {
        jpaRepository.deleteById(application.applicationId().value());
    }
}
