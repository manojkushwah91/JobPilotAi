package com.jobpilot.modules.ai.application.service;

import com.jobpilot.modules.ai.domain.model.PromptUseCase;
import com.jobpilot.modules.ai.domain.port.AIProviderPort;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProviderSelector {

    private final Map<PromptUseCase, ProviderConfig> routing;

    public ProviderSelector(List<AIProviderPort> providers) {
        this.routing = new EnumMap<>(PromptUseCase.class);
        var byName = new HashMap<String, AIProviderPort>();
        providers.forEach(p -> byName.put(p.providerName(), p));

        routing.put(PromptUseCase.RESUME_TAILORING,       new ProviderConfig(byName.get("openai"),     byName.get("anthropic")));
        routing.put(PromptUseCase.RESUME_SCORING,          new ProviderConfig(byName.get("anthropic"),  byName.get("openai")));
        routing.put(PromptUseCase.COVER_LETTER_GENERATION, new ProviderConfig(byName.get("openai"),     byName.get("anthropic")));
        routing.put(PromptUseCase.INTERVIEW_QUESTION,      new ProviderConfig(byName.get("openai"),     byName.get("anthropic")));
        routing.put(PromptUseCase.ANSWER_SCORING,          new ProviderConfig(byName.get("anthropic"),  null));
        routing.put(PromptUseCase.CAREER_PATH,             new ProviderConfig(byName.get("openai"),     null));
        routing.put(PromptUseCase.SKILLS_GAP,              new ProviderConfig(byName.get("openai"),     null));
        routing.put(PromptUseCase.NETWORKING_MESSAGE,      new ProviderConfig(byName.get("openai"),     null));
        routing.put(PromptUseCase.JOB_MATCH_EXPLANATION,   new ProviderConfig(byName.get("openai"),     null));
    }

    public record ProviderConfig(AIProviderPort primary, AIProviderPort fallback) {}

    public AIProviderPort select(PromptUseCase useCase) {
        var config = routing.get(useCase);
        if (config == null) throw new IllegalArgumentException("No provider routing for: " + useCase);
        return config.primary();
    }

    public AIProviderPort fallback(PromptUseCase useCase) {
        var config = routing.get(useCase);
        return config != null ? config.fallback() : null;
    }
}
