package com.jobpilot.infrastructure.ratelimit;

import com.jobpilot.common.exception.RateLimitException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        var key = resolveBucketKey(request);
        var bucket = buckets.computeIfAbsent(key, this::createBucket);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            var retryAfter = bucket.getAvailableTokens();
            log.warn("Rate limit exceeded for key: {}", key);
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            throw new RateLimitException(retryAfter);
        }
    }

    private String resolveBucketKey(HttpServletRequest request) {
        var userId = request.getHeader("X-User-Id");
        if (userId != null) return "user:" + userId;

        var ip = request.getRemoteAddr();
        return "ip:" + ip;
    }

    private Bucket createBucket(String key) {
        var limit = 1000;
        var refill = Refill.intervally(limit, Duration.ofMinutes(1));
        var bandwidth = Bandwidth.classic(limit, refill);
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
