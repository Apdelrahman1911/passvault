# Collection maintenance

- Keep skills generic and operational: inspect, detect, remediate, test, verify.
- Use only `name` and `description` in `SKILL.md` frontmatter.
- Keep `SKILL.md` concise; move detailed matrices and version notes into `references/`.
- Put deterministic, non-mutating inspection/validation utilities in `scripts/` and test success and failure fixtures.
- Do not add product names, package/bundle IDs, branch names, repository URLs, local absolute paths, credentials, tester data, or real user data.
- Update `catalog/skills.json` when skill names, priorities, tags, dependencies, or source issue ownership change.
- Every source issue from 1 through 157 must have exactly one entry in `catalog/source-issue-coverage.json` after ranges are expanded.
- Run `python3 scripts/validate_collection.py` and `python3 scripts/run_fixture_tests.py` before proposing a change.
- Treat project mirrors as generated output pinned to a committed central revision; never edit them independently.
