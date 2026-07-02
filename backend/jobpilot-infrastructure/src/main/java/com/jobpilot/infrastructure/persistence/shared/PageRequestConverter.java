package com.jobpilot.infrastructure.persistence.shared;

import com.jobpilot.common.model.PageRequest;
import org.springframework.data.domain.Sort;

public final class PageRequestConverter {
    private PageRequestConverter() {}

    public static org.springframework.data.domain.PageRequest toSpring(PageRequest request) {
        var dir = "ASC".equalsIgnoreCase(request.direction())
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        return org.springframework.data.domain.PageRequest.of(
            request.page(), request.size(), Sort.by(dir, request.sort()));
    }
}
