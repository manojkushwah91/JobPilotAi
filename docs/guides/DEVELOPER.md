# Developer Guide

## Coding Standards

### Java

- Java 21 features required: Records, Pattern Matching, Sealed Classes, Text Blocks
- Clean Architecture: Domain → Application → Infrastructure → Interfaces
- Constructor injection only (no field injection)
- `private final` for all injected dependencies
- Domain layer must have ZERO Spring imports
- Use records for value objects and DTOs
- Max line length: 120 characters
- Indentation: 4 spaces (no tabs)

### TypeScript/React

- Strict mode enabled
- Functional components with hooks (no classes)
- Props interface for every component
- Co-located tests (Component.test.tsx next to Component.tsx)
- Custom hooks for reusable logic
- Zod schemas for form validation

## Branch Strategy

```
main           ← Production-ready code
  └── develop  ← Integration branch
       ├── feature/JOB-123-description  ← New features
       ├── bugfix/JOB-456-description   ← Bug fixes
       └── release/v1.2.3              ← Release preparation
```

## Commit Convention

```
<type>(<scope>): <subject>

Types: feat, fix, refactor, test, docs, chore, perf, security
Scopes: auth, resume, job, application, interview, ai, infra, frontend

Examples:
  feat(resume): add AI-powered ATS scoring
  fix(auth): handle expired refresh tokens gracefully
  refactor(ai): extract provider selection strategy
  test(application): add state machine transition tests
  security(infra): update JWT secret rotation
```

## PR Process

1. Create feature branch from `develop`
2. Write tests (coverage ≥ 80%)
3. Run `make lint` and `make test`
4. Create PR against `develop`
5. PR must pass CI (build + test + lint + security scan)
6. Get approval from at least one reviewer
7. Merge squash into `develop`

## Testing Expectations

| Layer | Coverage | Framework | Tools |
|-------|----------|-----------|-------|
| Domain | 95%+ | JUnit 5 + AssertJ | ArchUnit |
| Application | 90%+ | JUnit 5 + Mockito | — |
| Infrastructure | 80%+ | Testcontainers | WireMock |
| Interfaces | 85%+ | @WebMvcTest | MockMvc |
| Frontend | 80%+ | Vitest + Testing Library | — |
| E2E | Critical paths | Playwright Java | — |

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `DB_URL` | Yes | PostgreSQL JDBC URL |
| `DB_USERNAME` | Yes | Database user |
| `DB_PASSWORD` | Yes | Database password |
| `REDIS_HOST` | Yes | Redis host |
| `AI_OPENAI_API_KEY` | Prod | OpenAI API key |
| `AI_ANTHROPIC_API_KEY` | Prod | Anthropic API key |
| `JWT_PRIVATE_KEY_PATH` | Prod | RS256 private key |
| `JWT_PUBLIC_KEY_PATH` | Yes | RS256 public key |
| `SENDGRID_API_KEY` | Prod | Email delivery |
| `STRIPE_SECRET_KEY` | Prod | Payment processing |
| `AWS_ACCESS_KEY_ID` | Prod | S3 file storage |

See `.env.example` for complete list.

## Monitoring

| Tool | URL | Credentials (dev) |
|------|-----|-------------------|
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3000 | admin / admin |
| pgAdmin | http://localhost:5050 | admin@jobpilot.dev / admin |
| MailHog | http://localhost:8025 | — |
| MinIO Console | http://localhost:9001 | jobpilot / jobpilot_dev |
