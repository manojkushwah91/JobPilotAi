package com.jobpilot.infrastructure.config;

import com.jobpilot.common.util.ClockProvider;
import com.jobpilot.common.util.TimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonBeanConfig {

    @Bean
    public ClockProvider clockProvider() {
        return TimeProvider.system();
    }
}
