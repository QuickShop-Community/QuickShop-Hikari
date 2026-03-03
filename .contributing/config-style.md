# Configuration Style Guide (QuickShop-Hikari)

This guide standardizes how contributors should **add or modify configuration options** in a configuration file so the file stays:

- beginner-friendly for casual server owners
- understandable for non-native English speakers
- technically helpful for developers
- consistent, readable, and stable across releases

---

## Goals

All new configuration entries must be:

- **Beginner-safe**: clearly explains what it does and when to change it.
- **Non-native friendly**: short sentences, simple vocabulary, minimal idioms.
- **Developer-informative**: optional technical detail without overwhelming admins.
- **Backward compatible**: never silently break existing configs.
- **Discoverable**: predictable placement and consistent structure.

---

## File Structure and Placement

### Top-level section ordering

Add new options in the most relevant existing section. Prefer this general order:

1. Core / version / language
2. Taxes / economy
3. Logging
4. Database
5. Limits
6. Shop blocks
7. `shop:` (main shop behavior)
8. `matcher:`
9. `protect:`
10. `backup-policy:`
11. `purge:`
12. Debug / legacy / migration
13. Commands
14. Privacy / metrics

If an option does not clearly belong anywhere, create a new section only when:
- the feature introduces **3+ related options**, or
- it is a distinct feature area (example: “shop tags”, “market analytics”).

### Grouping inside a section

Within a section (example: `shop:`), group options in this order:

1. Safety / permissions / protection
2. Performance / caching
3. UX / messages
4. Feature toggles
5. Costs / economics
6. Advanced / experimental
7. Integration-specific options

Add new options next to the most related existing options. Do not append unrelated items at the end.

### Section headers

Use consistent section separators for major blocks:

```yaml
# ----------------------------------------------------------
# Section Title
# ----------------------------------------------------------
```

Use `=` separators only for rare “banner” sections (not everywhere).

---

## Configuration Key Naming

### Key style

* Use **kebab-case**: `price-change-requires-fee`
* Keep keys **descriptive but not long**
* Avoid abbreviations unless widely known: `uuid`, `papi`, `sql`
* Avoid repeating section context:

    * ✅ `shop: display-items`
    * ❌ `shop: shop-display-items`

### Boolean naming

Boolean keys should read naturally as yes/no:

* ✅ `enable`, `use-cache`, `ignore-cancelled-interact-event`
* ✅ `disable-quick-create` (negative form is OK when the feature is known as “quick create”)

Avoid double negatives:

* ❌ `dont-disable-x`
* ❌ `disable-no-x`

### Enum and mode naming

For multi-mode options, prefer:

* `type:` or `mode:`

Document the allowed values clearly. If numeric for legacy reasons, document the mapping.

### Lists and maps

* Use plural nouns for lists: `enabled-languages`, `shop-blocks`
* Use descriptive names for maps: `brackets`, `alternate-currency-symbol-list`

---

## Comment Style Rules

### Required comment layers (in order)

Every new option should have up to 3 layers:

1. **Plain-English purpose** *(required)*
2. **Behavior details** *(required if non-obvious)*
3. **Advanced / technical notes** *(optional, only if helpful)*

Example:

```yaml
# Charge a fee when changing shop prices.
# Helps reduce endless price undercutting.
# (Advanced: Fee is charged only when the price update succeeds.)
price-change-requires-fee: true
```

### Beginner-first, developer-second

If you include internals (events, classes, timing), mark them clearly:

* `Advanced:` or `(Advanced: ...)`
* `Note:` for important behavior notes
* `WARNING:` for risky settings
* `EXPERIMENTAL:` for unstable/beta behavior

### Grammar and readability

* Use short sentences.
* Avoid slang, humor, idioms, and culture-specific expressions.
* Avoid vague statements like “could cause lag” unless you specify *what kind of load*.
* Prefer measurable phrasing:

    * ✅ “May increase database writes.”
    * ✅ “May load chunks during lookups.”
    * ❌ “Could cause lag.”

### Warnings and risk labels

Use consistent labels:

* `NOTE:` safe but important
* `WARNING:` risk of data loss, dupes, corruption, exploits, or severe performance issues
* `ADVANCED:` safe but requires technical knowledge
* `EXPERIMENTAL:` may be unstable or change

Example:

```yaml
# WARNING: May corrupt shop data if misused. Always backup before enabling.
# ADVANCED: Intended for cross-version migration only.
force-load-downgrade-items:
  enable: false
```

### Allowed values and examples

If an option has multiple valid values, list them and include a short example if formatting is not obvious.

```yaml
# Preferred protocol implementation for virtual displays.
# Allowed values: protocollib, packetevents
display-protocol: 'protocollib'
```

For format-based configs, always describe the format:

```yaml
# Format:
#   <currencyName>;<symbol>
alternate-currency-symbol-list:
  - USD;$
```

### Avoid redundant comments

Do not restate what the key already says.

* ❌ `# Enable caching` for `use-cache: true`
* ✅ `# Use shop caching to improve lookup performance.`

---

## Defaults and Safety

### Default value expectations

Defaults should favor:

* stability
* data safety
* predictable performance
* minimal surprise

If enabling a feature has risk, default it to **false** and explain why.

### Risky options must include mitigation

If your option can cause:

* data corruption
* duplication
* compatibility issues
* heavy server load

You must include:

* a `WARNING:`
* what to do first (example: make a backup)
* when this option should be used

---

## YAML Formatting Rules

### Indentation

* Use **2 spaces** per level.
* Do not use tabs.
* Keep nested blocks aligned exactly.

### Quoting

* Quote strings only when needed (special characters, leading zeros, etc.)
* Follow the existing file’s style consistently.

### Blank lines

* Use blank lines between major blocks.
* Avoid inserting blank lines inside small lists/maps unless it improves readability.

---

## Workflow Checklist (Adding a New Option)

When adding a new configuration option:

1. **Choose the correct location**

    * nearest relevant section and subgroup
2. **Choose a consistent key name**

    * kebab-case, clear boolean naming, no redundancy
3. **Write comments**

    * purpose → behavior → advanced notes → warnings
4. **Pick a safe default**
5. **Document allowed values / format**
6. **Consider migration**

    * if this replaces or changes behavior of an older key
7. **Ensure discoverability**

    * place near related options; cross-reference if needed

---

## Deprecations and Renames

### Never silently remove keys

If a key is being replaced:

* keep the old key for at least one major cycle
* mark it as deprecated
* point to the replacement

```yaml
# DEPRECATED: Use shop.new-setting instead.
# This will be removed in a future release.
old-setting: true
```

### Define precedence when both keys exist

If both old and new keys are present:

* define which one wins
* document the precedence clearly
* keep behavior deterministic

---

## Performance Notes Standard

When describing performance impact, name the type of load:

* CPU
* memory
* disk
* database
* network
* chunk loads

Preferred phrasing examples:

* “May increase chunk loads during lookups.”
* “Adds a database query per transaction.”
* “May increase database writes at high trade volume.”

---

## Templates

### Simple boolean option

```yaml
# Enable the shop analytics sidebar in the UI.
# NOTE: UI-only. Does not change transaction logic.
shop-analytics-sidebar: false
```

### Advanced feature toggle

```yaml
# Enable asynchronous shop scanning.
# Improves responsiveness on large servers.
# WARNING: Experimental. Report issues with debug logs enabled.
# (Advanced: Runs async tasks; ensure your platform supports this safely.)
async-shop-scan: false
```

### Structured sub-section

```yaml
# ----------------------------------------------------------
# Market Watch
# ----------------------------------------------------------
market-watch:
  # Enable market watch tracking.
  enable: false

  # Tracking interval in ticks.
  # NOTE: Lower values increase database writes.
  interval-ticks: 1200
```

---

## Project Consistency Requirements

When adding new config options, also ensure:

* documentation is updated (if required by the repo)
* code references match the config path exactly
* diagnostic output (like `/qs paste`) includes relevant options when appropriate