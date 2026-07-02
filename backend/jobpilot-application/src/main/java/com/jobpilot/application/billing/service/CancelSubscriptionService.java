package com.jobpilot.application.billing.service;

import com.jobpilot.application.billing.dto.CancelSubscriptionCommand;
import com.jobpilot.application.billing.ports.SubscriptionRepository;
import com.jobpilot.application.billing.usecase.CancelSubscriptionUseCase;
import com.jobpilot.common.exception.NotFoundException;
import com.jobpilot.domain.billing.SubscriptionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CancelSubscriptionService implements CancelSubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;

    public CancelSubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public Void execute(CancelSubscriptionCommand command) {
        var id = SubscriptionId.from(UUID.fromString(command.subscriptionId()));
        var subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Subscription", command.subscriptionId()));
        subscription.cancel();
        subscriptionRepository.save(subscription);
        return null;
    }
}
