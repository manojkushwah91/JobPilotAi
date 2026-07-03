# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **AI Response Parsing** — Real JSON parsing for resume scoring, skill gap analysis, and job matching (was returning hardcoded zeros)
- **Playwright Automation** — Full Playwright browser automation adapter with headless Chrome, screenshot capture
- **Email Template Rendering** — HTML email templates with variable substitution for welcome, password-reset, application-status, interview-feedback, automation-completed/failed
- **Flyway V9+V10** — Missing tables for `automation_sessions` and `subscriptions`
- **User Account Deletion** — `DELETE /api/v1/users/me` now actually soft-deletes via `DeleteUserService`
- **Forgot/Reset Password** — Endpoints in AuthController, frontend forgot-password page
- **Root Landing Page** — `/` public landing page with features showcase and CTA
- **Notifications Page** — `/notifications` with read/unread tabs, mark-as-read
- **Cover Letters Page** — `/cover-letters` with create, AI generate, and delete
- **Custom 404 & Error Pages** — `not-found.tsx` and `error.tsx` boundaries
- **Security Policy** — `SECURITY.md` for vulnerability reporting
- **Docker Security** — Backend Dockerfile now runs as non-root user with healthcheck
- **.dockerignore** — Prevents bloat in Docker build context
- **CODE_OF_CONDUCT** — Filled in contact method placeholder

### Fixed
- AI services now properly parse and return LLM JSON responses instead of placeholder defaults
- User account deletion now actually calls the soft-delete service
- Email sender now renders HTML templates with variable substitution

## [1.0.0] - 2024-08-01

### Added

- **Backend Foundation** — Hexagonal architecture with Spring Boot 3, Maven multi-module setup
- **Frontend Foundation** — Next.js 14 with App Router, TypeScript, Tailwind CSS
- **Authentication & Authorization** — JWT-based auth with Spring Security, OAuth2 social login
- **User Profile Module** — Resume management, skill extraction, preference settings
- **Job Discovery** — Multi-source job aggregation with search, filters, and pagination
- **Job Matching** — AI-powered job scoring and recommendation engine
- **Application Tracker** — Kanban-style pipeline with status tracking and history
- **ATS Optimizer** — Resume tailoring and keyword gap analysis
- **Cover Letter Engine** — AI-generated personalized cover letters
- **Resume Studio** — Rich resume builder with templates and PDF export
- **Company Intelligence** — Company profile enrichment, culture insights, salary data
- **Browser Automation** — Playwright-based job application automation
- **Interview Hub** — Interview scheduling, preparation, and feedback tracking
- **Career Analytics** — Dashboard with visual insights and trend analysis
- **Notification Module** — Email, in-app, and push notifications
- **Settings Module** — User preference management and account controls
- **Admin Module** — Admin dashboard with user management and system monitoring
- **AI Provider Layer** — Pluggable AI provider abstraction (OpenAI, Anthropic, etc.)
- **Prompt Engineering** — Versioned prompt templates and management system
- **Caching Strategy** — Redis-based distributed caching
- **Search Engine** — Elasticsearch integration for full-text search
- **Security** — Rate limiting, encryption, audit logging, CSP headers
- **Logging & Observability** — Structured logging, metrics, health checks
- **Testing Strategy** — Unit, integration, contract, E2E, and performance tests
- **CI/CD** — GitHub Actions workflows for lint, test, build, and deploy
- **Database** — PostgreSQL schema with Flyway migrations
- **Docker & Containerization** — Dockerfiles and docker-compose for local and production
- **Documentation** — Architecture docs, API docs, setup guides
