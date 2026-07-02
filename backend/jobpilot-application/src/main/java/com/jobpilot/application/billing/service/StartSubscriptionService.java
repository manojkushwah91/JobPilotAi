package com.jobpilot.application.billing.service;

import com.jobpilot.application.billing.dto.StartSubscriptionCommand;
import com.jobpilot.application.billing.dto.SubscriptionResponse;
import com.jobpilot.application.billing.ports.SubscriptionRepository;
import com.jobpilot.application.billing.usecase.StartSubscriptionUseCase;
import com.jobpilot.domain.billing.Subscription;
import com.jobpilot.domain.billing.SubscriptionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StartSubscriptionService implements StartSubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;

    public StartSubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public SubscriptionResponse execute(StartSubscriptionCommand command) {
        var subscriptionId = SubscriptionId.generate();
        var subscription = Subscription.start(subscriptionId, command.userId(), command.plan());
        subscriptionRepository.save(subscription);
        return SubscriptionResponse.from(subscription);
    }
}
