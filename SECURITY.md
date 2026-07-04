# JobPilot AI v2.0 — Security Documentation

**Version:** 2.0  
**Status:** Draft  
**Product:** JobPilot AI — "Offline-First Autonomous AI Job Agent"  
**Author:** Chief Software Architect  

---

## Table of Contents

1. Security Principles
2. Authentication & Authorization
3. Data Privacy & Protection
4. API Security
5. Browser Automation Security
6. AI Provider Security
7. Infrastructure Security
8. Compliance
9. Security Monitoring
10. Incident Response

---

## 1. Security Principles

### 1.1 Core Principles

| Principle | Application |
|-----------|-------------|
| **Offline-First Privacy** | Default AI inference is local (Ollama). Cloud AI is opt-in only. |
| **Zero Trust** | All requests are authenticated and authorized, regardless of source. |
| **Defense in Depth** | Multiple layers of security (authentication, authorization, encryption, rate limiting). |
| **Least Privilege** | Users and services have minimum necessary permissions. |
| **Data Minimization** | Collect and store only necessary data. |
| **Encryption Everywhere** | Encrypt data at rest and in transit. |
| **Audit Everything** | Log all security-relevant events. |

---

## 2. Authentication & Authorization

### 2.1 Authentication

**JWT-Based Authentication:**
- Access tokens: 15-minute expiration
- Refresh tokens: 7-day expiration with rotation
- Tokens stored in HTTP-only cookies (not localStorage)
- Refresh token rotation on every use

**Password Security:**
- BCrypt hashing with cost factor 12
- Minimum password length: 12 characters
- Password complexity: uppercase, lowercase, number, special character
- Password history: last 5 passwords not allowed
- Account lockout: 5 failed attempts, 15-minute lockout

**Email Verification:**
- Email verification required for new registrations
- Verification token: 24-hour expiration
- Resend verification: limit to 3 per hour

### 2.2 Authorization

**Role-Based Access Control (RBAC):**

| Role | Permissions |
|------|-------------|
| USER | Create/own missions, view own applications, manage own profile |
| ADMIN | Manage users, view all data, system configuration |

**Method-Level Security:**
```java
@PreAuthorize("hasRole('USER') and #userId == authentication.principal.id")
public Mission getMission(UUID missionId, UUID userId) {
    // User can only access their own missions
}

@PreAuthorize("hasRole('ADMIN')")
public List<User> getAllUsers() {
    // Only admins can view all users
}
```

**Resource-Level Security:**
- Users can only access their own data (missions, applications, profiles)
- Admins can access all data
- All repository queries include user ID filter for non-admin users

---

## 3. Data Privacy & Protection

### 3.1 Data Classification

| Classification | Data Examples | Storage | Encryption |
|----------------|---------------|---------|------------|
| PII (Personally Identifiable Information) | Name, email, phone, address | PostgreSQL | AES-256 |
| Sensitive Personal Data | Resume, cover letter, salary expectations | PostgreSQL (encrypted) | AES-256 |
| Business Data | Mission config, job listings, applications | PostgreSQL | AES-256 |
| Public Data | Job titles, company names | PostgreSQL | None |

### 3.2 Encryption at Rest

**PostgreSQL Encryption:**
- Use `pgcrypto` extension for AES-256 encryption
- Encryption keys stored in environment variables or Vault
- PII columns encrypted: `email`, `phone`, `resume.content`, `cover_letter.body`

**File Storage Encryption:**
- Resumes and cover letters encrypted at rest
- Screenshots encrypted at rest
- Encryption keys managed by application

### 3.3 Encryption in Transit

**TLS Configuration:**
- TLS 1.3 only
- Strong cipher suites
- HSTS enabled
- Certificate pinning for external APIs (optional)

**WebSocket Security:**
- WSS (WebSocket Secure) only
- Token-based authentication via query parameter or header

### 3.4 Data Retention

| Data Type | Retention Period | Deletion Method |
|-----------|------------------|----------------|
| User data | Until account deletion | Soft delete, hard delete after 30 days |
| Mission data | 1 year after completion | Soft delete, hard delete after 30 days |
| Application data | 2 years after submission | Soft delete, hard delete after 30 days |
| Memory data | 1 year | Soft delete, hard delete after 30 days |
| Logs | 90 days | Automatic deletion |

### 3.5 Data Export & Deletion

**GDPR Compliance:**
- Users can export all their data via API
- Users can request account deletion
- Deletion process: soft delete → hard delete after 30 days
- Data export format: JSON

---

## 4. API Security

### 4.1 Rate Limiting

**Rate Limiting Rules:**

| Endpoint | Rate Limit | Burst |
|----------|------------|-------|
| POST /api/v1/auth/login | 5 requests/minute | 10 |
| POST /api/v1/auth/register | 3 requests/hour | 5 |
| POST /api/v1/missions | 10 requests/minute | 20 |
| GET /api/v1/* | 100 requests/minute | 200 |
| WebSocket | 10 connections/minute | 20 |

**Implementation:**
- Redis-based rate limiting
- Rate limit by user ID and IP address
- Rate limit headers included in responses

### 4.2 Input Validation

**Validation Rules:**
- All input validated at controller level
- Use Bean Validation annotations
- Custom validators for business logic
- SQL injection prevention via JPA parameterized queries

**Example:**
```java
public class CreateMissionRequest {
    @NotBlank
    @Size(min = 3, max = 255)
    private String name;
    
    @NotBlank
    @Size(min = 10, max = 1000)
    private String goal;
    
    @Min(0)
    @Max(1000000)
    private Integer targetSalary;
    
    @NotEmpty
    private List<@NotBlank String> locations;
}
```

### 4.3 Output Encoding

**XSS Prevention:**
- React automatically escapes JSX
- Sanitize user-generated content before rendering
- Content Security Policy (CSP) headers

**CSP Configuration:**
```
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' wss://localhost:3000;
```

### 4.4 CSRF Protection

**CSRF Tokens:**
- CSRF tokens for all state-changing requests
- Tokens stored in HTTP-only cookies
- Tokens validated on server

**Spring Security Configuration:**
```java
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
)
```

---

## 5. Browser Automation Security

### 5.1 Credential Management

**Credential Storage:**
- Job board credentials stored encrypted in PostgreSQL
- Credentials never stored in browser profiles
- Credentials provided per session by user
- Credentials never logged

**Credential Transmission:**
- Credentials transmitted over HTTPS
- Credentials never included in logs
- Credentials never included in error messages

### 5.2 Session Management

**Browser Session Isolation:**
- Each mission has isolated browser context
- Cookies and local storage isolated per session
- Sessions cleared after mission completion
- No session persistence across missions

**Session Timeout:**
- Browser sessions timeout after 30 minutes of inactivity
- Sessions manually cleared on agent pause/stop

### 5.3 CAPTCHA/MFA Handling

**CAPTCHA Detection:**
- Agent detects CAPTCHA via DOM analysis
- Agent pauses and notifies user
- User manually completes CAPTCHA
- Agent resumes after user confirmation

**MFA Detection:**
- Agent detects MFA challenges
- Agent pauses and notifies user
- User manually completes MFA
- Agent resumes after user confirmation

**Security:**
- CAPTCHA/MFA screenshots stored encrypted
- Screenshots deleted after 90 days
- User must approve before resuming

---

## 6. AI Provider Security

### 6.1 Ollama Security (Default)

**Local Inference:**
- Ollama runs on localhost (127.0.0.1)
- No external network access for inference
- Data never leaves user's machine
- Models downloaded from official Ollama registry only

**Model Security:**
- Only use verified models from Ollama registry
- Model integrity verified via checksum
- Models stored in local directory with restricted permissions

### 6.2 Cloud AI Security (Optional)

**Opt-In Only:**
- Cloud AI providers are opt-in only
- User must explicitly configure API keys
- User must consent to data leaving local machine
- Default is always Ollama (local)

**API Key Management:**
- API keys encrypted at rest (AES-256)
- API keys never logged
- API keys never included in error messages
- API keys can be revoked by user at any time

**Data Transmission:**
- Data transmitted over HTTPS
- Data transmitted only to configured provider
- No data shared with third parties

**Provider-Specific Security:**
- OpenAI: Follow OpenAI security best practices
- Gemini: Follow Google security best practices
- Claude: Follow Anthropic security best practices

---

## 7. Infrastructure Security

### 7.1 Container Security

**Docker Security:**
- Use official base images (e.g., eclipse-temurin:21-jre-alpine)
- Run containers as non-root user
- Minimal container footprint (alpine-based)
- Regular security scans (Trivy)

**Docker Compose Security:**
- Isolated Docker network
- No privileged containers
- Resource limits (CPU, memory)
- Volume mounts with restricted permissions

### 7.2 Database Security

**PostgreSQL Security:**
- Strong password for database user
- No remote access (localhost only in dev)
- SSL/TLS for database connections in production
- Regular security updates

**Connection Security:**
- Connection pooling with HikariCP
- Connection encryption in production
- Connection timeout: 30 seconds
- Maximum pool size: 20

### 7.3 Redis Security

**Redis Security:**
- Password authentication enabled
- No remote access (localhost only in dev)
- TLS for Redis connections in production
- No dangerous commands (FLUSHDB, CONFIG)

### 7.4 File Storage Security

**File Upload Security:**
- File type validation (whitelist: PDF, DOCX, PNG, JPG)
- File size limits (max 10MB)
- Virus scanning (ClamAV) in production
- Files stored outside web root

**File Access Security:**
- Files served via authenticated API
- No direct file access
- File URLs signed with expiration
- File access logged

---

## 8. Compliance

### 8.1 GDPR Compliance

**Data Subject Rights:**
- Right to access: Users can export all data
- Right to rectification: Users can edit their data
- Right to erasure: Users can request account deletion
- Right to portability: Data export in standard format (JSON)
- Right to object: Users can opt out of data processing

**Data Processing Agreement:**
- Clear privacy policy
- Consent for data processing
- Data processing purposes clearly stated
- Data retention periods documented

### 8.2 CCPA Compliance

**Consumer Rights:**
- Right to know: Data collection transparency
- Right to delete: Account deletion
- Right to opt-out: Opt-out of data selling (not applicable, no data selling)
- Right to non-discrimination: No discrimination for exercising rights

### 8.3 SOC 2 Compliance (Future)

**Security Principles:**
- Security: System is protected against unauthorized access
- Availability: System is available for operation and use
- Processing integrity: System processing is complete, accurate, timely, and valid
- Confidentiality: Information is restricted to authorized access
- Privacy: Personal information is collected, used, retained, disclosed, and disposed of

---

## 9. Security Monitoring

### 9.1 Logging

**Security Events Logged:**
- Authentication failures (login attempts, password changes)
- Authorization failures (access denied)
- Rate limit violations
- Suspicious activity (unusual patterns)
- Data access (who accessed what data)
- Configuration changes

**Log Format:**
```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "level": "WARN",
  "logger": "com.jobpilot.security",
  "message": "Authentication failed",
  "mdc": {
    "userId": "uuid",
    "ip": "192.168.1.1",
    "userAgent": "Mozilla/5.0...",
    "attemptCount": 5
  }
}
```

### 9.2 Metrics

**Security Metrics:**
- Authentication failure rate
- Authorization failure rate
- Rate limit violations
- Suspicious activity count
- CAPTCHA detection rate
- MFA detection rate

### 9.3 Alerts

**Alert Triggers:**
- 10+ failed authentication attempts from same IP (5 minutes)
- 100+ failed authorization attempts from same user (1 hour)
- Rate limit violations exceeding threshold
- Unusual data access patterns
- Configuration changes by non-admin users

**Alert Channels:**
- In-app notifications
- Email alerts (optional)
- Slack alerts (optional)

---

## 10. Incident Response

### 10.1 Incident Classification

| Severity | Description | Response Time |
|----------|-------------|---------------|
| 1 (Critical) | Data breach, unauthorized access to PII | 1 hour |
| 2 (High) | Service disruption, security vulnerability | 4 hours |
| 3 (Medium) | Suspicious activity, potential vulnerability | 24 hours |
| 4 (Low) | Minor security issue, policy violation | 72 hours |

### 10.2 Incident Response Process

**1. Detection:**
- Automated monitoring alerts
- User reports
- Security audit findings

**2. Containment:**
- Isolate affected systems
- Disable affected accounts
- Block malicious IPs

**3. Eradication:**
- Remove malicious code
- Patch vulnerabilities
- Update security configurations

**4. Recovery:**
- Restore from backups
- Verify system integrity
- Resume normal operations

**5. Lessons Learned:**
- Document incident
- Update security policies
- Improve monitoring

### 10.3 Security Contacts

**Primary Security Contact:** manojkushwah91@gmail.com

**Vulnerability Reporting:**
- Email security vulnerabilities to manojkushwah91@gmail.com
- Include detailed description and proof of concept
- Response within 48 hours

---

## 11. Security Best Practices for Users

### 11.1 Password Security

- Use strong, unique passwords
- Enable two-factor authentication (future)
- Change password if compromised
- Never share password

### 11.2 Credential Security

- Never share job board credentials
- Use unique passwords for each job board
- Rotate credentials regularly
- Report compromised credentials immediately

### 11.3 Data Security

- Review uploaded resumes regularly
- Delete old data no longer needed
- Be cautious with sensitive information
- Report suspicious activity

---

**End of Security Documentation v2.0**
