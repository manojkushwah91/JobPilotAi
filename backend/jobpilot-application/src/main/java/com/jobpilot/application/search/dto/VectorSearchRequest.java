package com.jobpilot.application.search.dto;

import com.jobpilot.common.exception.ValidationException;
import java.util.List;

public record VectorSearchRequest(String query, List<String> skills) {
    public VectorSearchRequest {
        if ((query == null || query.isBlank()) && (skills == null || skills.isEmpty())) {
            throw new ValidationException("query|skills", "Either query or skills must be provided");
        }
    }
}
