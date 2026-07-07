package com.jobpilot.application.automation.ports;

import java.util.Map;

public interface PortalDetectorPort {
    String detectPortal(String url);
    Map<String, String> getPortalSelectors(String portal);
    boolean isApplicationForm(String url);
}
