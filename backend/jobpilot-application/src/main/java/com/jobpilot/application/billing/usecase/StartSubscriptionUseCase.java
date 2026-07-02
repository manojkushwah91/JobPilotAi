package com.jobpilot.application.billing.usecase;

import com.jobpilot.application.billing.dto.StartSubscriptionCommand;
import com.jobpilot.application.billing.dto.SubscriptionResponse;
import com.jobpilot.application.shared.UseCase;

public interface StartSubscriptionUseCase extends UseCase<StartSubscriptionCommand, SubscriptionResponse> {}
