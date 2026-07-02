package com.jobpilot.infrastructure.persistence.analytics;

import com.jobpilot.application.analytics.dto.DateRangeCommand;
import com.jobpilot.application.analytics.dto.MonthlyTrend;
import com.jobpilot.application.analytics.ports.AnalyticsRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AnalyticsRepositoryImpl implements AnalyticsRepository {

    private final EntityManager em;

    public AnalyticsRepositoryImpl(EntityManager em) { this.em = em; }

    @Override public long countTotalUsers() {
        return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM users WHERE deleted_at IS NULL").getSingleResult()).longValue();
    }

    @Override public long countActiveUsers(DateRangeCommand r) {
        return ((Number) em.createNativeQuery("SELECT COUNT(DISTINCT user_id) FROM refresh_tokens WHERE created_at BETWEEN ?1 AND ?2")
            .setParameter(1, r.startDate()).setParameter(2, r.endDate()).getSingleResult()).longValue();
    }

    @Override public long countTotalApplications() {
        return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM applications").getSingleResult()).longValue();
    }

    @Override public long countApplicationsByDateRange(DateRangeCommand r) {
        return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM applications WHERE created_at BETWEEN ?1 AND ?2")
            .setParameter(1, r.startDate()).setParameter(2, r.endDate()).getSingleResult()).longValue();
    }

    @Override public long countTotalJobs() {
        return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM job_listings WHERE is_active = true").getSingleResult()).longValue();
    }

    @Override public long countTotalInterviews() {
        return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM interview_sessions").getSingleResult()).longValue();
    }

    @Override public Map<String, Long> applicationsByStatus() {
        var map = new LinkedHashMap<String, Long>();
        var rows = em.createNativeQuery("SELECT status, COUNT(*) FROM applications GROUP BY status").getResultList();
        for (var row : rows) { var o = (Object[]) row; map.put((String) o[0], ((Number) o[1]).longValue()); }
        return map;
    }

    @Override public Map<String, Long> jobsBySource() {
        var map = new LinkedHashMap<String, Long>();
        var rows = em.createNativeQuery("SELECT source, COUNT(*) FROM job_listings GROUP BY source").getResultList();
        for (var row : rows) { var o = (Object[]) row; map.put((String) o[0], ((Number) o[1]).longValue()); }
        return map;
    }

    @Override public Map<String, Long> aiUsageByUseCase(DateRangeCommand r) {
        var map = new LinkedHashMap<String, Long>();
        var rows = em.createNativeQuery("SELECT use_case, COUNT(*) FROM ai_usage_logs WHERE created_at BETWEEN ?1 AND ?2 GROUP BY use_case")
            .setParameter(1, r.startDate()).setParameter(2, r.endDate()).getResultList();
        for (var row : rows) { var o = (Object[]) row; map.put((String) o[0], ((Number) o[1]).longValue()); }
        return map;
    }

    @Override public List<MonthlyTrend> monthlyTrends(DateRangeCommand r) {
        var list = new ArrayList<MonthlyTrend>();
        var rows = em.createNativeQuery("""
            SELECT to_char(created_at, 'YYYY-MM') as month,
                   COUNT(*) FILTER (WHERE created_at BETWEEN ?1 AND ?2) as apps,
                   0 as interviews,
                   0 as ai_calls
            FROM applications GROUP BY month ORDER BY month
        """).setParameter(1, r.startDate()).setParameter(2, r.endDate()).getResultList();
        for (var row : rows) { var o = (Object[]) row; list.add(new MonthlyTrend((String) o[0], ((Number) o[1]).longValue(), 0, 0)); }
        return list;
    }
}
