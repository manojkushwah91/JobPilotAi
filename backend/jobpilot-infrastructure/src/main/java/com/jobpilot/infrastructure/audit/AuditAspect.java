package com.jobpilot.infrastructure.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
public class AuditAspect {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");
    private final ObjectMapper objectMapper;

    public AuditAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(audit)")
    public Object audit(ProceedingJoinPoint pjp, AuditLog audit) throws Throwable {
        var start = Instant.now();
        var success = true;
        String errorMessage = null;

        try {
            return pjp.proceed();
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            var entry = buildLogEntry(pjp, audit, start, success, errorMessage);
            auditLog.info(objectMapper.writeValueAsString(entry));
        }
    }

    private Map<String, Object> buildLogEntry(ProceedingJoinPoint pjp, AuditLog audit,
                                                Instant start, boolean success, String error) {
        var request = getCurrentHttpRequest();
        var entry = new LinkedHashMap<String, Object>();
        entry.put("timestamp", start.toString());
        entry.put("action", audit.action());
        entry.put("resourceType", audit.resourceType());
        entry.put("success", success);
        entry.put("userId", MDC.get("userId"));
        entry.put("traceId", MDC.get("traceId"));
        if (request != null) {
            entry.put("ipAddress", request.getRemoteAddr());
            entry.put("userAgent", request.getHeader("User-Agent"));
            entry.put("path", request.getRequestURI());
            entry.put("method", request.getMethod());
        }
        if (error != null) entry.put("error", error);
        if (pjp.getArgs().length > 0) {
            entry.put("resourceId", extractResourceId(pjp.getArgs()));
        }
        return entry;
    }

    private HttpServletRequest getCurrentHttpRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }

    private String extractResourceId(Object[] args) {
        for (var arg : args) {
            if (arg instanceof String s && s.matches("[0-9a-f-]{36}")) {
                return s;
            }
        }
        return null;
    }
}
