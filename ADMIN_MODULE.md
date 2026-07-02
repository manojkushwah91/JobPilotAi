# JobPilot AI — Admin Module

**Version:** 1.0  
**Status:** Draft  
**Phase:** 22 of 35  
**Author:** Chief Software Architect  

---

## 1. Module Purpose

Admin dashboard, user management, system monitoring, feature flags, audit log viewer, and platform configuration.

---

## 2. Domain Model

```
AdminUser (extends User with ADMIN role):
  - User details + admin-specific permissions (admin_granular_permissions: JSON)

AuditLog (append-only):
  - id, actorId, actorEmail, action, resourceType, resourceId, details(JSON), ipAddress, userAgent, timestamp

SystemMetric (time-series, stored in separate metrics schema or Prometheus):
  - metricName, value, tags(JSON), timestamp
  # Read from external monitoring system

FeatureFlag:
  - key: String (unique), enabled: Boolean, description: String, updatedAt
```

---

## 3. Admin Action Categories

| Category | Actions |
|----------|---------|
| User Management | List users, view detail, suspend/unsuspend, delete, change role |
| System Monitoring | View metrics (CPU, memory, API latency, error rate, active users) |
| Audit Log | Search by actor, action, date range, resource type |
| Feature Flags | Toggle on/off (reloads without restart via refresh endpoint) |
| Job Sources | Enable/disable configured sources, trigger manual scrape |
| AI Providers | View cost breakdown, switch primary/fallback, clear cache |
| Support | Impersonate user (audit logged), view user's application details |

---

## 4. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/admin/users` | List all users (paginated, filterable) |
| GET | `/api/v1/admin/users/{id}` | User detail |
| PUT | `/api/v1/admin/users/{id}/suspend` | Suspend user |
| PUT | `/api/v1/admin/users/{id}/unsuspend` | Unsuspend user |
| GET | `/api/v1/admin/audit-log` | Search audit log |
| GET | `/api/v1/admin/metrics/summary` | System metrics summary |
| GET | `/api/v1/admin/feature-flags` | List all flags |
| PUT | `/api/v1/admin/feature-flags/{key}` | Toggle feature flag |
| POST | `/api/v1/admin/job-sources/{id}/scrape` | Trigger manual scrape |
| GET | `/api/v1/admin/ai-providers/costs` | AI cost breakdown |

---

**End of Admin Module v1.0**
