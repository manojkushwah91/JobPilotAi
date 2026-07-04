package com.jobpilot.application.agent.ports;

import com.jobpilot.domain.agent.Tool;

import java.util.List;
import java.util.Optional;

public interface ToolRegistry {

    void register(Tool tool);

    Optional<Tool> findByName(String name);

    List<Tool> findAll();

    List<Tool> findByCapability(String capability);
}
