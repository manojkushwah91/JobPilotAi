# Contributing

## Branch Naming

```
feature/{JIRA-ID}-kebab-case-description
bugfix/{JIRA-ID}-kebab-case-description
hotfix/{JIRA-ID}-kebab-case-description
release/v{major}.{minor}.{patch}
```

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(scope): add new feature
fix(scope): fix bug description
refactor(scope): restructure without behavior change
test(scope): add tests for module
docs(scope): update documentation
chore(scope): tooling, dependency updates
perf(scope): performance improvement
security(scope): security fix
```

## Pull Request Checklist

- [ ] Code follows project coding standards
- [ ] Tests added/updated with ≥ 80% coverage
- [ ] `make lint` passes
- [ ] `make test` passes
- [ ] Documentation updated (if changing behavior)
- [ ] Commit messages follow conventions
- [ ] Branch is up to date with `develop`
- [ ] PR title follows `<type>(<scope>): <description>` format

## Code Review

- All PRs require at least one approval
- Reviewers check: correctness, security, performance, test coverage, code style
- Automated checks must pass before merge
- Use squash merge to keep history clean

## Development Workflow

1. Pick an issue from the project board
2. Create a branch from `develop`
3. Implement the feature/fix
4. Write/update tests
5. Run `make lint && make test`
6. Push and create PR
7. Address review feedback
8. Squash merge to `develop`
9. Delete the feature branch

## Getting Help

- Check `docs/guides/` for setup and architecture guides
- Run `make help` for available commands
- Open an issue for questions
