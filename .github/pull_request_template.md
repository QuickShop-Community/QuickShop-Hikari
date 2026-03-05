<!--
Thank you for contributing to QuickShop-Hikari! 
Please complete all relevant sections below before requesting review.
-->

# Type of Change

Please select the type of change this PR represents:

- [ ] feat – New feature
- [ ] fix – Bug fix
- [ ] hotfix – Urgent production fix
- [ ] refactor – Code improvement (no behavior change)
- [ ] docs – Documentation/config comment changes only
- [ ] chore – Build/tooling/internal maintenance
- [ ] breaking – Breaking change (requires migration)

> If this is a breaking change, clearly describe the migration steps below.

---

# General Contribution Checklist

- [ ] I have read and understood the [CONTRIBUTING.md](.contributing/contributing.md).
- [ ] My branch name follows the naming conventions in CONTRIBUTING.md.
- [ ] I have signed the Contributor License Agreement (CLA), if required.
- [ ] My changes are based on the latest `hikari` branch.
- [ ] I have tested my changes locally.
- [ ] Existing functionality has not been unintentionally broken.

---

# Description of Changes

Clearly explain:

- What was changed?
- Why was it changed?
- What issue does it solve?
- Is this user-facing, developer-facing, or internal?

Example:

> This PR fixes incorrect tax calculation when `show: false` caused balance overflow checks to misbehave.

---

# Configuration Changes (if applicable)

If this PR modifies config.yml or another configuration file or adds new configuration options:

- [ ] All new config entries follow [CONFIG-STYLE.md](.contributing/config-style.md).
- [ ] Comments follow the required structure:
  - Plain-English purpose
  - Behavior details (if needed)
  - Advanced notes (if applicable)
  - Warnings (if applicable)
- [ ] Section placement follows the defined ordering rules.
- [ ] Defaults favor safety and stability.
- [ ] Risky settings include `WARNING:` comments and mitigation guidance.
- [ ] Deprecated keys (if any) are clearly marked and documented.
- [ ] No existing config defaults were changed unintentionally.

If config changes were made, explain them:

```markdown
Example:
Added shop.async-price-validation
- Default: false
- Purpose: Improves responsiveness on large servers
- Risk: May increase async task load
```

---

# Migration Notes (Required for breaking changes)

If this PR includes a breaking change:

* What changed?
* Who is affected?
* Required admin action?
* Is there automatic migration?

Example:

```markdown
- shop.display-type=1 is no longer supported.
- Servers must switch to display-type=2.
- No automatic migration is performed.
```

---

# Related Issues / References

* Fixes: #___
* Relates to: #___
* Documentation PR: #___

---

# Testing Notes

How was this tested?

* [ ] Local test server
* [ ] Networked proxy setup
* [ ] With database (MySQL)
* [ ] With H2
* [ ] Other integrations (describe below)

Additional notes:

---

# Changelog Entry

If this change is user-facing, provide a changelog entry, which also includes your name/online alias:

```markdown
### Fix
- Corrected progressive tax bracket calculation edge case.(creatorfromhell)
```

---

# Maintainer Review Checklist (Internal)
<!--
 Only a member of the QuickShop community maintainer should fill this part out.
-->

* [ ] Code structure meets project standards
* [ ] No unintended side effects
* [ ] Config additions follow style guide
* [ ] Documentation updated (if needed)
* [ ] Changelog entry added
* [ ] Breaking change properly labeled

---

Thank you for contributing to QuickShop-Hikari, it means a lot to us!