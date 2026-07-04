package com.jobpilot.application.agent;

import com.jobpilot.application.agent.ports.AiProviderPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestAiProviderConfig {

    @Bean
    @Primary
    public AiProviderPort aiProviderPort() {
        return new AiProviderPort() {
            @Override
            public String executePrompt(String systemPrompt, String userPrompt, String model,
                                          double temperature, int maxTokens) {
                return "{\"status\": \"mock_response\"}";
            }

            @Override
            public String executePromptWithContext(String systemPrompt, String userPrompt,
                                                     String context, String model,
                                                     double temperature, int maxTokens) {
                return "{\"status\": \"mock_response\"}";
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public String providerName() {
                return "mock";
            }
        };
    }
}
