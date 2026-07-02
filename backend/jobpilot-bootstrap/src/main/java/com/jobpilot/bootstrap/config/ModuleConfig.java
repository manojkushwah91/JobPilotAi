package com.jobpilot.bootstrap.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {
    "com.jobpilot.interfaces",
    "com.jobpilot.infrastructure",
    "com.jobpilot.application",
    "com.jobpilot.domain",
    "com.jobpilot.common"
})
@EnableJpaRepositories(basePackages = "com.jobpilot.infrastructure.persistence")
@EntityScan(basePackages = "com.jobpilot.infrastructure.persistence")
public class ModuleConfig {
}
