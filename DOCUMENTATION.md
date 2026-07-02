# JobPilot AI — Documentation

**Version:** 1.0  
**Status:** Draft  
**Phase:** 33 of 35  
**Author:** Chief Software Architect  

---

## 1. Documentation Checklist

| Document | Format | Audience | Location |
|----------|--------|----------|----------|
| Setup Guide | Markdown | Developers | `docs/SETUP.md` |
| API Reference | OpenAPI 3.0 | Developers | `docs/api/jobpilot-api.yaml` |
| Architecture Overview | Markdown | Developers/Architects | `docs/ARCHITECTURE.md` |
| Deployment Guide | Markdown | DevOps | `docs/DEPLOYMENT.md` |
| Developer Guide | Markdown | Developers | `docs/DEVELOPER.md` |
| User Guide | Markdown | End Users | `docs/USER_GUIDE.md` |
| Contribution Guide | Markdown | Contributors | `CONTRIBUTING.md` |
| README | Markdown | Everyone | `README.md` |
| Swagger UI | HTML | Developers | `/swagger-ui/index.html` |

---

## 2. README Structure

```
# JobPilot AI — The AI Career Operating System

## Overview
One-sentence description + badge row (CI, coverage, license, version)

## Features
- 6 bullet points of key capabilities
- Screenshot/GIF of dashboard

## Quick Start
```bash
git clone ...
docker compose up
open http://localhost:3000
```

## Documentation
- [Setup Guide](docs/SETUP.md)
- [API Reference](docs/api/...)
- [Architecture](docs/ARCHITECTURE.md)

## Tech Stack
Mentioned: Java 21, Spring Boot 3, Next.js 14, PostgreSQL, Redis, etc.

## License
MIT / Proprietary
```

---

## 3. API Documentation

```
Format: OpenAPI 3.0 (YAML)
Generation: springdoc-openapi (auto), annotated with @Operation @ApiResponse

Documentation annotations per endpoint:
  @Operation(summary = "...", description = "...")
  @ApiResponse(responseCode = "200", description = "...")
  @ApiResponse(responseCode = "401", description = "...")
  @ApiResponse(responseCode = "403", description = "...")
  @ApiResponse(responseCode = "429", description = "...")

Exposed at: /swagger-ui/index.html (dev/staging only, disabled in prod)
```

---

**End of Documentation v1.0**
