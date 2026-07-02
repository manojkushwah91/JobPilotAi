# Contributing to JobPilot AI

Thank you for your interest in contributing! We welcome contributions from everyone.

## How to Contribute

1. **Fork** the repository on GitHub.
2. **Create a feature branch** from `main`:
   ```
   git checkout -b feat/your-feature-name
   ```
3. **Make your changes** following the code style guidelines below.
4. **Write tests** for your changes.
5. **Commit** using Conventional Commits format.
6. **Push** to your fork and open a Pull Request.

## Code Style

### Java / Spring Boot
- Follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
- Use 4-space indentation, no tabs.
- Package names: `com.jobpilot.*` with hexagonal architecture layers (`domain`, `application`, `infrastructure`, `interfaces`).
- Use Lombok judiciously; prefer explicit constructors for required dependencies.
- Write unit tests with JUnit 5 and Mockito.

### TypeScript / Next.js
- Follow the project's ESLint and Prettier configuration.
- Use 2-space indentation.
- Prefer functional components with hooks over class components.
- Use TypeScript strict mode; avoid `any` where possible.
- Write tests with Vitest and React Testing Library.

## Commit Message Format

We enforce **Conventional Commits**:

```
<type>(<scope>): <short summary>

[optional body]

[optional footer]
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `ci`, `perf`.

Examples:
- `feat(ats): add resume scoring endpoint`
- `fix(ui): correct job card date formatting`
- `docs(readme): update setup instructions`

## Testing Requirements

- All new features must include unit tests.
- Bug fixes must include a regression test.
- Backend: aim for ≥80% coverage on new code.
- Frontend: write component and integration tests for new UI features.
- Run the full test suite before submitting:
  ```bash
  # Backend
  cd backend && mvn test

  # Frontend
  cd frontend && npm test
  ```

## Pull Request Process

1. Ensure your branch is up to date with `main`.
2. All checks (CI, lint, tests) must pass.
3. Your PR must be reviewed by at least one maintainer.
4. Address all review feedback before the PR is merged.
5. Squash commits when merging — maintain a clean history.

## Code of Conduct

Please note that this project adheres to the [Code of Conduct](CODE_OF_CONDUCT.md). By participating you agree to abide by its terms.
