# 3. Use Playwright for Browser Automation

**Date:** 2026-07-03

## Status

Accepted

## Context

The application needs to automate job board applications. Browser automation is required because many job portals lack public APIs or use anti-bot measures that require a real browser environment.

## Decision

Use **Playwright** (via `com.microsoft.playwright`) for browser automation. The `PlaywrightAutomationAdapter` implements the `AutomationPort` interface and supports:
- Headless Chromium by default.
- Navigation, form filling, clicking, and screenshot capture.
- Resource cleanup via `DisposableBean`.

## Consequences

- Playwright bundles its own browser binaries (~300MB) downloaded at build time.
- Screenshots are stored to the local filesystem (MinIO integration deferred).
- Automation logic is isolated behind the `AutomationPort` interface, allowing future replacement (e.g., Selenium).
