package com.jobpilot.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CsrfFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CsrfFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (isSafeMethod(request.getMethod()) || isApiRequest(request)) {
            chain.doFilter(request, response);
            return;
        }

        String csrfToken = request.getHeader("X-CSRF-Token");
        String expectedToken = request.getHeader("X-CSRF-Expected");

        if (csrfToken == null || expectedToken == null || !csrfToken.equals(expectedToken)) {
            log.warn("CSRF token mismatch for {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(403);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"CSRF token validation failed\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isSafeMethod(String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)
            || "OPTIONS".equalsIgnoreCase(method) || "TRACE".equalsIgnoreCase(method);
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/");
    }

    @Override
    public void destroy() {}
}
