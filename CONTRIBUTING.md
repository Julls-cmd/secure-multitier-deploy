# Contributing to Secure Multi-tier Deploy

## Branching Strategy
- `main`: stable, production-ready code. No direct pushes.
- `feature/<short-description>`: new features.
- `fix/<short-description>`: bug fixes.
- `docs/<short-description>`: documentation changes.

Always create your branch from an up-to-date `main`:
```bash
git checkout main
git pull
git checkout -b feature/my-feature
```

## Commit Conventions
This project follows [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` a new feature
- `fix:` a bug fix
- `docs:` documentation only changes
- `style:` formatting, no code change
- `refactor:` code change that neither fixes a bug nor adds a feature
- `test:` adding or updating tests
- `chore:` maintenance tasks (Docker, dependencies, config)

Example: `docs: update installation steps in README`

## Pull Request Guidelines
1. Push your branch and open a PR targeting `main`.
2. Use a clear title and explain **what** changed and **why**.
3. Make sure the stack still starts with `docker compose up -d`.
4. At least **one approval** is required before merging.
5. Resolve any merge conflicts before requesting a review.
6. Delete the branch after merging.