package com.jobpilot.infrastructure.persistence.billing;

import com.jobpilot.application.billing.ports.SubscriptionRepository;
import com.jobpilot.domain.billing.Subscription;
import com.jobpilot.domain.billing.SubscriptionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

    private final SubscriptionJpaRepository jpaRepository;

    public SubscriptionRepositoryImpl(SubscriptionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Subscription save(Subscription sub) {
        return jpaRepository.save(SubscriptionEntity.fromDomain(sub)).toDomain();
    }

    @Override
    public Optional<Subscription> findById(SubscriptionId id) {
        return jpaRepository.findById(id.value()).map(SubscriptionEntity::toDomain);
    }

    @Override
    public Optional<Subscription> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(SubscriptionEntity::toDomain);
    }

    @Override
    public Page<Subscription> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(SubscriptionEntity::toDomain);
    }
}
