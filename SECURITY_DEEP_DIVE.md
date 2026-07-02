# JobPilot AI — Security Deep Dive

**Version:** 1.0  
**Status:** Draft  
**Phase:** 26 of 35  
**Author:** Chief Software Architect  

---

## 1. Security Pillars

| Pillar | Implementation | Priority |
|--------|---------------|----------|
| Authentication | JWT RS256 + OAuth 2.0 + 2FA | Critical |
| Authorization | RBAC (4 tiers) + @PreAuthorize on every endpoint | Critical |
| Input Validation | Bean Validation + HTML sanitization (Jsoup) | Critical |
| CSRF | Stateless JWT → no CSRF (SPA-first). OAuth state param | High |
| XSS | Content-Security-Policy + output encoding + React DOM | High |
| SQL Injection | JPA/Spring Data (parameterized queries), no raw SQL | Critical |
| Rate Limiting | Token bucket per endpoint group | High |
| Data Encryption | AES-256-GCM at rest (PII columns), TLS 1.3 in transit | Critical |
| Audit Logging | All admin + sensitive actions logged | Medium |

---

## 2. JWT Security

```
Algorithm: RS256 (RSA 2048-bit)
Key rotation: JWKS endpoint at /api/v1/auth/jwks.json, rotated every 90 days
Claims: { sub, roles[], tier, iat, exp, jti, sessionId }
Audience: "jobpilot-api"
Issuer: "jobpilot-auth-service"
Access token TTL: 15 min (PRO), 10 min (FREE)
Refresh token TTL: 7 days (PRO), 24h (FREE)
```

---

## 3. RBAC Permission Matrix (All Endpoints)

```
┌──────────────────────────────┬──────────┬──────────┬──────────┬──────────┐
│ Endpoint Group               │ FREE     │ PREMIUM  │ PRO      │ ADMIN    │
├──────────────────────────────┼──────────┼──────────┼──────────┼──────────┤
│ POST /auth/register          │ ✓        │ ✓        │ ✓        │ ✓        │
│ POST /auth/login             │ ✓        │ ✓        │ ✓        │ ✓        │
│ GET  /users/me               │ ✓        │ ✓        │ ✓        │ ✓        │
│ PUT  /users/me               │ ✓        │ ✓        │ ✓        │ ✓        │
│ POST /resumes                │ 3 max    │ 10 max   │ unlimited│ unlimited│
│ GET  /resumes                │ ✓        │ ✓        │ ✓        │ ✓        │
│ POST /resumes/score          │ ✗        │ ✗        │ ✓        │ ✓        │
│ POST /cover-letters/generate │ 2/month  │ 10/month │ unlimited│ unlimited│
│ GET  /jobs                   │ ✓        │ ✓        │ ✓        │ ✓        │
│ GET  /jobs/matches           │ ✗        │ ✓        │ ✓        │ ✓        │
│ POST /applications           │ 5/month  │ 30/month │ unlimited│ unlimited│
│ POST /applications/automate  │ ✗        │ ✗        │ ✓        │ ✓        │
│ POST /interviews/sessions    │ 1/month  │ 5/month  │ unlimited│ unlimited│
│ GET  /analytics/*            │ basic    │ full     │ full     │ full     │
│ GET  /admin/*                │ ✗        │ ✗        │ ✗        │ ✓        │
└──────────────────────────────┴──────────┴──────────┴──────────┴──────────┘
```

---

## 4. Rate Limiting

```
Token bucket per IP + per userId, configurable in application.yml:

  auth:
    login: 5/min (10 with captcha)
    register: 3/min
    refresh: 10/min
    forgot-password: 3/hour
  api:
    general: 100/min
    ai-endpoints: 10/min
    automation: 5/min
    search: 30/min
  admin:
    general: 60/min

Implementation: Bucket4j with Redis backend (distributed)
Response: 429 Too Many Requests + Retry-After header
```

---

## 5. Data Encryption

```
PII columns encrypted at rest with AES-256-GCM:
  - user_profiles.phone_number
  - user_profiles.address
  - user_profiles.date_of_birth
  - user_settings.payment_method
  - notifications.email_body

Key hierarchy:
  - Master Key (KMS / Vault)
  - Data Encryption Key (DEK, per column family)
  - Rotated every 90 days

Implementation: Spring CryptoModule + @Encrypted annotation
```

---

## 6. Security Headers

```
Content-Security-Policy: default-src 'self';
                         script-src 'self' https://js.stripe.com;
                         style-src 'self' 'unsafe-inline';
                         img-src 'self' data: https://logo.clearbit.com;
                         connect-src 'self' https://api.stripe.com wss://ws.jobpilot.dev;
                         frame-src https://js.stripe.com;

Strict-Transport-Security: max-age=63072000; includeSubDomains; preload
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: camera=(), microphone=(self), geolocation=(self)
```

---

**End of Security Deep Dive v1.0**
