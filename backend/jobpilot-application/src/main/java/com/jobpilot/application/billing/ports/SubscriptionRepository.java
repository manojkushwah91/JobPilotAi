package com.jobpilot.application.billing.ports;

import com.jobpilot.domain.billing.Subscription;
import com.jobpilot.domain.billing.SubscriptionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {
    Subscription save(Subscription sub);
    Optional<Subscription> findById(SubscriptionId id);
    Optional<Subscription> findByUserId(UUID userId);
    Page<Subscription> findAll(Pageable pageable);
}
