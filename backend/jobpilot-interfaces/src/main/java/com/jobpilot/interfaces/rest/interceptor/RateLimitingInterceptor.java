package com.jobpilot.interfaces.rest.interceptor;

import com.jobpilot.application.ratelimiter.RateLimiterService;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    public RateLimitingInterceptor(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod method) {
            var annotation = method.getMethodAnnotation(RateLimited.class);
            if (annotation != null) {
                var key = resolveKey(request, annotation);
                if (!rateLimiterService.tryAcquire(key, annotation.capacity(), annotation.refillTokens(), annotation.refillDurationSeconds())) {
                    response.setStatus(429);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Too many requests\"}");
                    return false;
                }
            }
        }
        return true;
    }

    private String resolveKey(HttpServletRequest request, RateLimited annotation) {
        if (!annotation.key().isEmpty()) return annotation.key();
        var ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return "ratelimit:" + ip + ":" + request.getRequestURI();
    }
}
