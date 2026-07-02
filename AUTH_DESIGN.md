# JobPilot AI — Authentication & Authorization

**Version:** 1.0  
**Status:** Draft  
**Phase:** 7 of 35  
**Author:** Chief Software Architect  

---

## Table of Contents

1. Authentication Architecture
2. JWT Design
3. Refresh Token Design
4. OAuth 2.0 Integration
5. Role-Based Access Control (RBAC)
6. Permission Model
7. Session Management
8. Spring Security Configuration
9. Password Policies
10. API Security

---

## 1. Authentication Architecture

### 1.1 Auth Flow Diagram

```
┌──────────┐    ┌──────────┐    ┌────────────┐    ┌──────────┐    ┌──────────┐
│  Client   │    │ Gateway   │    │ Auth       │    │  Redis    │    │  DB      │
│  (Next.js)│    │ (SCG)    │    │ Service    │    │          │    │          │
└─────┬─────┘   └─────┬─────┘   └──────┬──────┘   └─────┬────┘   └─────┬────┘
      │                │                │                │              │
      │ 1. POST /login │                │                │              │
      │    {email,pass} │                │                │              │
      │─────────┬──────│───────────────►│                │              │
      │         │      │                │ 2. Find user   │              │
      │         │      │                │──────────────────────────────►│
      │         │      │                │◄──────────────────────────────│
      │         │      │                │                │              │
      │         │      │                │ 3. Verify pass │              │
      │         │      │                │                │              │
      │         │      │                │ 4. Generate    │              │
      │         │      │                │    Access JWT  │              │
      │         │      │                │    (RS256,15m) │              │
      │         │      │                │                │              │
      │         │      │                │ 5. Generate    │              │
      │         │      │                │    Refresh Tok │              │
      │         │      │                │    (opaque)    │              │
      │         │      │                │────────────────►│ store hash  │
      │         │      │                │                │              │
      │         │      │◄───────────────│                │              │
      │         │      │ tokens         │                │              │
      │         │      │                │                │              │
      │◄────────│──────│                │                │              │
      │         │      │                │                │              │
      │ Set httpOnly │                  │                │              │
      │ cookie +     │                  │                │              │
      │ return JSON  │                  │                │              │
```

### 1.2 Token Transport

| Token | Transport | Storage | Details |
|-------|-----------|---------|---------|
| **Access Token (JWT)** | `Authorization: Bearer <jwt>` header + `access_token` httpOnly cookie | Memory (Zustand) + httpOnly cookie | 15 min expiry, stateless validation |
| **Refresh Token** | `refresh_token` httpOnly cookie (path=/api/v1/auth) | Redis (hashed) | 7 day expiry, rotation on use |
| **OAuth State** | Session cookie | Memory | Anti-CSRF for OAuth flow |

### 1.3 Token Refresh Flow

```
Client → 401 from API → Axios interceptor:
  1. Check if request already retried (prevent loop)
  2. POST /api/v1/auth/refresh (cookie auto-sent)
  3. Server validates refresh token in Redis
  4. If valid: invalidate old refresh, generate new pair
  5. If invalid: clear auth state, redirect to login
  6. Retry original request with new access token
```

---

## 2. JWT Design

### 2.1 Token Structure

```json
// JWT Header
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "key-2026-01"       // Key ID for rotation
}

// JWT Payload (Access Token)
{
  "sub": "usr_a1b2c3d4",           // User UUID
  "email": "user@example.com",
  "role": "PRO_USER",
  "iat": 1748822400,               // Issued at
  "exp": 1748823300,               // Expires (15 min)
  "jti": "tok_xyz789",             // JWT ID (unique, for blacklist)
  "iss": "jobpilot.ai",
  "aud": "api.jobpilot.ai"
}
```

### 2.2 Signing & Verification

```
Algorithm: RS256 (RSA Signature with SHA-256)
  - Asymmetric: private key signs, public key verifies
  - Private key: only on auth service (or API Gateway)
  - Public key: distributed to all services via /api/v1/auth/.well-known/jwks.json

Key Rotation:
  - New key pair generated every 90 days
  - Old public key retained until all tokens expire
  - JWKS endpoint lists all active public keys with kid

Key Size: 2048 bits
Key Storage: HashiCorp Vault (never in filesystem)
```

### 2.3 JWT Validation (API Gateway)

```
Gateway filter chain for protected routes:
  1. Extract JWT from Authorization header (or cookie)
  2. Verify signature (use JWKS public key)
  3. Check expiry (reject if expired)
  4. Check JTI against Redis blacklist (logout)
  5. Extract claims (sub, role, email) → set headers for downstream:
     X-User-Id, X-User-Role, X-User-Email
  6. If invalid: 401 response

Gateway does NOT validate refresh tokens (only auth service does)
```

---

## 3. Refresh Token Design

### 3.1 Token Properties

```
Format: Opaque UUID v7 (non-guessable, not JWT)
Generation: crypto.randomUUID()
Storage: Redis hashed (SHA-256)
  Key: refresh_token:{hashed_token}
  Value: { user_id, expires_at, family (for rotation detection) }

Security:
  - NEVER sent in URL, NEVER in JS-accessible storage
  - httpOnly, Secure, SameSite=Strict cookie, path=/api/v1/auth
  - One-time use (rotation invalidates old token)
  - Family tracking detects token theft (if old token used after rotation)
```

### 3.2 Rotation Flow

```
1. Client sends refresh token (cookie)
2. Server hashes token, looks up in Redis
3. If found:
   a. Delete old hash
   b. Generate new refresh token
   c. Store new hash with same family ID
   d. Generate new access token
   e. Return new pair
4. If found but family mismatch (old token replayed):
   a. Delete ALL tokens in that family (token theft detected)
   b. Require re-authentication
5. If not found → 401 "Session expired"
```

### 3.3 Token Cleanup

```
Scheduled job (every hour):
  - Scan Redis for expired refresh tokens
  - Delete expired hashes
  - Log cleanup count

On logout:
  - Blacklist access token JTI in Redis (TTL: remaining token lifetime)
  - Delete refresh token hash
  - Clear cookies
```

---

## 4. OAuth 2.0 Integration

### 4.1 Supported Providers

| Provider | Grant Type | Scopes | Use Case |
|----------|-----------|--------|----------|
| Google | Authorization Code | email, profile | Fast signup, most popular |
| LinkedIn | Authorization Code | email, openid, profile | Professional context, job data |
| GitHub | Authorization Code | user:email, read:user | Developer audience |
| Microsoft | Authorization Code | User.Read, email | Enterprise users |

### 4.2 OAuth Flow

```
1. Client: "Login with Google" → redirect to backend:
   GET /api/v1/auth/oauth/google?redirect_uri=/dashboard

2. Backend: generate state (anti-CSRF), redirect to Google:
   https://accounts.google.com/o/oauth2/v2/auth
     ?client_id=xxx
     &redirect_uri=https://api.jobpilot.ai/api/v1/auth/oauth/google/callback
     &response_type=code
     &scope=email+profile
     &state={random_state}
     &access_type=offline

3. User authenticates with Google, grants consent
4. Google redirects to callback:
   GET /api/v1/auth/oauth/google/callback?code=xxx&state=yyy

5. Backend:
   a. Verify state matches (Redis, 10 min TTL)
   b. Exchange code for tokens (POST to Google)
   c. Get user info from Google (/userinfo)
   d. Find user by email or provider_id
   e. If exists: login
   f. If not: create user account (auto-register)
   g. Generate JWT + refresh token
   h. Redirect to client with tokens (or set cookies)

6. Client: /dashboard
```

### 4.3 Account Linking

```
If a user signs up with email/password first, then later uses OAuth:
  - Match by email
  - Link OAuth provider to existing account
  - User can login via either method

Edge cases:
  - Email exists but not verified → ask to verify first
  - Same email, different provider → link to same account
  - Different email from OAuth → create separate account? NO — link by provider ID not email
```

---

## 5. Role-Based Access Control (RBAC)

### 5.1 Role Hierarchy

```
ADMIN
  └── ENTERPRISE_USER
        └── PRO_USER
              └── FREE_USER
```

### 5.2 Role Definitions

| Role | Description | Assigned By |
|------|-------------|-------------|
| `FREE_USER` | Default on registration. Limited features. | System (auto) |
| `PRO_USER` | Paid subscription. Full features. | Subscription activation |
| `ENTERPRISE_USER` | Team accounts, custom features | Admin |
| `ADMIN` | Platform administration | Super admin only |

### 5.3 Role Enforcement Points

```
1. API Gateway: JWT validation extracts role from claims
2. Controller: @PreAuthorize("hasRole('PRO_USER')")
3. Service layer: @PreAuthorize annotations
4. Frontend: RoleGuard components, API 403 handling
5. Database: queries filter by user_id (row-level security)
```

---

## 6. Permission Model

### 6.1 Permission Matrix

| Resource | FREE_USER | PRO_USER | ENTERPRISE | ADMIN |
|----------|-----------|----------|------------|-------|
| Read own profile | ✓ | ✓ | ✓ | ✓ |
| Update own profile | ✓ | ✓ | ✓ | ✓ |
| Search jobs | ✓ | ✓ | ✓ | ✓ |
| Apply manually | 10/month | Unlimited | Unlimited | — |
| Auto-apply (browser) | ✗ | 50/month | Unlimited | — |
| Resume builder | 1 resume | 10 resumes | Unlimited | — |
| AI resume tailoring | ✗ | ✓ | ✓ | — |
| ATS scoring | ✗ | ✓ | ✓ | — |
| Cover letter (AI) | ✗ | ✓ | ✓ | — |
| Interview practice | 5/month | 30/month | Unlimited | — |
| Career path analysis | Basic | Advanced | Full | — |
| Saved searches | 3 | 50 | Unlimited | — |
| Networking messages | ✗ | 10/month | Unlimited | — |
| Analytics | Basic | Advanced | Full | — |
| Export data | ✗ | ✓ | ✓ | — |
| Admin panel | ✗ | ✗ | ✗ | ✓ |
| View all users | ✗ | ✗ | ✗ | ✓ |
| Manage subscriptions | ✗ | ✗ | ✗ | ✓ |
| Manage feature flags | ✗ | ✗ | ✗ | ✓ |

### 6.2 Resource-Level Authorization

```
Rule: Users can only access their own resources.

Enforced via:
  1. Repository queries always include userId filter
  2. @PostFilter / @PostAuthorize for collections
  3. Application service validates ownership before returning data

Example:
  applicationService.findById(userId, applicationId)
    → Repository: WHERE id = ? AND user_id = ?
    → Returns Application only if it belongs to the requesting user
```

---

## 7. Session Management

### 7.1 Session Limits

| Tier | Max Active Sessions | Concurrent Automation Sessions |
|------|---------------------|-------------------------------|
| FREE_USER | 1 | 0 |
| PRO_USER | 5 | 3 |
| ENTERPRISE | 10 | 10 |
| ADMIN | 3 | 0 |

### 7.2 Session Tracking

```
Redis stores:
  user_sessions:{userId}
    - Set of session IDs (each login creates a new session)
    - Each session: { token_jti, refresh_family, created_at, ip, user_agent }

On login:
  - If session count exceeds limit → evict oldest session
  - Evicted session's tokens added to blacklist

On admin suspend:
  - Delete all sessions for user
  - Blacklist all active tokens
  - User is immediately logged out
```

---

## 8. Spring Security Configuration

### 8.1 Security Filter Chain

```
Order 0: /api/v1/auth/** → permit all (no auth)
Order 1: /api/v1/admin/** → hasRole('ADMIN')
Order 2: /api/v1/webhooks/** → permit all (signature verification)
Order 3: /api/v1/** → authenticated
Order 4: /actuator/health → permit all
Order 5: /actuator/** → hasRole('ADMIN')
Order 6: /swagger-ui/** → permit all
Order 7: /v3/api-docs/** → permit all
```

### 8.2 Security Beans

```
SecurityFilterChain:
  - JwtAuthenticationFilter (reads JWT from header/cookie, sets SecurityContext)
  - RateLimitingFilter (per-user + per-IP)
  - RequestLoggingFilter (request_id, user_id → MDC)
  - CorsFilter (configured origins)
  - CsrfFilter (disabled for API — protected by token + CORS)
  - SessionCreationPolicy.STATELESS

AuthenticationManager:
  - JwtAuthenticationProvider (validates JWT, extracts claims)
  - DaoAuthenticationProvider (email/password login)

PasswordEncoder:
  - BCryptPasswordEncoder(strength=12)
```

### 8.3 Method Security

```java
@EnableMethodSecurity  // Enables @PreAuthorize, @PostAuthorize, @Secured

// Examples:
@PreAuthorize("hasRole('PRO_USER')")
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("#userId == authentication.principal.id")  // Resource owner check
@PostAuthorize("returnObject.userId == authentication.principal.id")
```

---

## 9. Password Policies

### 9.1 Requirements

```
Minimum length: 12 characters
Must contain: uppercase, lowercase, digit, special character
Maximum length: 128 characters
Password history: last 5 passwords (prevent reuse)
Maximum failed attempts: 5 (before 15-minute lockout)
Password expiry: 90 days (optional, configurable)
Common password check: reject top 10,000 common passwords (OWASP list)
```

### 9.2 Password Reset

```
Flow:
  1. User requests reset → generate UUID token
  2. Token stored in user record (SHA-256 hashed), TTL 1 hour
  3. Email sent with link: /reset-password?token=xxx
  4. User submits new password + token
  5. Validate token (hash match + not expired)
  6. Update password + invalidate all sessions
  7. Send confirmation email
```

---

## 10. API Security

### 10.1 Rate Limiting

| Endpoint Group | Limit | Window | Scope |
|----------------|-------|--------|-------|
| `/api/v1/auth/login` | 5 | 15 min | Per IP + email |
| `/api/v1/auth/register` | 3 | 60 min | Per IP |
| `/api/v1/auth/forgot-password` | 2 | 60 min | Per email |
| `/api/v1/auth/refresh` | 10 | 15 min | Per user |
| `/api/v1/**` (general) | 100 | 1 min | Per user (authenticated) |
| `/api/v1/**` (general) | 20 | 1 min | Per IP (unauthenticated) |
| `/api/v1/**/tailor` (AI) | 10 | 1 min | Per user |
| `/api/v1/**/automate` | 5 | 1 min | Per user |

### 10.2 CORS Configuration

```
Allowed Origins:
  - https://app.jobpilot.ai (production)
  - https://staging.jobpilot.ai
  - http://localhost:3000 (dev)

Allowed Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
Allowed Headers: Authorization, Content-Type, X-Request-Id
Exposed Headers: X-Request-Id
Credentials: true (for httpOnly cookies)
Max Age: 3600s
```

### 10.3 CSRF Protection

```
Strategy: Double Submit Cookie + SameSite
- Not using traditional CSRF tokens (API is stateless)
- Protected by:
  1. SameSite=Strict on all cookies
  2. CORS restricts to known origins
  3. JWT in Authorization header (not cookie for API calls)
  4. Refresh token cookie is path-scoped (/api/v1/auth)
```

---

*Phase 7 defines the complete authentication system — JWT with RS256, opaque refresh tokens with rotation theft detection, OAuth 2.0 across 4 providers, RBAC with 4 tiers, resource-level authorization, rate limiting, and Spring Security configuration.*

---

**End of Authentication & Authorization v1.0**
