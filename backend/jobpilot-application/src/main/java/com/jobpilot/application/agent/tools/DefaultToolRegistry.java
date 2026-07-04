package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.ToolRegistry;
import com.jobpilot.domain.agent.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DefaultToolRegistry implements ToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(DefaultToolRegistry.class);

    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    public DefaultToolRegistry(List<Tool> toolList) {
        for (var tool : toolList) {
            register(tool);
        }
        log.info("Registered {} tools: {}", tools.size(), tools.keySet());
    }

    @Override
    public void register(Tool tool) {
        tools.put(tool.name(), tool);
        log.debug("Registered tool: {}", tool.name());
    }

    @Override
    public Optional<Tool> findByName(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    @Override
    public List<Tool> findAll() {
        return new ArrayList<>(tools.values());
    }

    @Override
    public List<Tool> findByCapability(String capability) {
        return tools.values().stream()
            .filter(t -> t.name().contains(capability) || t.description().contains(capability))
            .toList();
    }
}
