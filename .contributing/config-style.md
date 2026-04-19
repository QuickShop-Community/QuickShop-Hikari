# QuickShop-Hikari Configuration Style Guide

This document defines the **standards contributors must follow when adding or modifying configuration options** in `config.yml`.

The goal is to keep the configuration:

* beginner-friendly for server owners
* understandable for non-native English speakers
* technically helpful for developers
* consistent across contributions
* stable across releases

All configuration changes must follow this guide.

---

# 1. Core Principles

Configuration must be:

### Beginner Safe

Comments must clearly explain what the option does and when a server owner should change it.

### Non-Native Friendly

Use simple grammar and short sentences.

Avoid:

* idioms
* slang
* unnecessary technical jargon

### Developer Informative

Technical notes may be included but must **follow the plain explanation**, not replace it.

### Consistent

All configuration options must follow the same:

* naming style
* comment structure
* placement rules
* formatting

### Backward Compatible

Never remove or change configuration behavior without proper migration guidance.

---

# 2. File Structure

Configuration sections should follow a consistent order.

Preferred top-level ordering:

1. Core configuration (version, language)
2. Localization / translation
3. Economy / taxes
4. Logging
5. Database
6. Limits
7. Shop blocks
8. `shop:` section (main shop behavior)
9. Matcher
10. Protection
11. Backup
12. Purge
13. Debug / legacy migration
14. Commands
15. Privacy / metrics

New configuration options should be placed in the **most logically related section**.

Do **not append unrelated options to the bottom of the file**.

---

# 3. Section Header Style

Configuration sections must use **simple comment headers**.

## Allowed Format

```yaml
# Backup Policy
backup-policy:
```

## Disallowed Format

Separator bars are **not allowed** in configuration sections.

```yaml
# ----------------------------------------------------------
# Backup Policy
# ----------------------------------------------------------
backup-policy:
```

### Exception: File Header

The **file header at the top of the configuration file may use separators**.

Example:

```yaml
# ==========================================================
# QuickShop-Hikari Main Configuration
# ==========================================================
#
# Website: https://quickshophikari.org/
# Documentation: https://quickshop-community.github.io/QuickShop-Hikari-Documents/
# Community Discord: https://discord.com/invite/Bu3dVtmsD3
```

This is the **only place where separators are permitted**.

---

# 4. Configuration Key Naming

## Use kebab-case

Keys must use lowercase kebab-case.

Correct:

```
display-items
price-change-requires-fee
ignore-cancelled-interact-event
```

Incorrect:

```
displayItems
DisplayItems
display_items
```

---

## Boolean Naming

Boolean options should read naturally as **true/false statements**.

Preferred examples:

```
enable
use-cache
disable-quick-create
ignore-unlimited-shop-messages
```

Avoid double negatives:

Incorrect:

```
dont-disable-x
disable-no-x
```

---

## Enum or Mode Options

For configuration values that support multiple modes, prefer keys such as:

```
type
mode
method
strategy
```

Allowed values must always be documented in comments.

Example:

```yaml
# Display protocol implementation.
# Allowed values: protocollib, packetevents
display-protocol: protocollib
```

---

# 5. Comment Structure

Each configuration option must follow this comment order.

1. **Plain explanation**
2. **Behavior details**
3. **Advanced notes (optional)**
4. **Warnings if needed**

Example:

```yaml
# Charge a fee when changing shop prices.
# Helps reduce constant price undercutting between shops.
# (Advanced: Fee is charged only if the price change succeeds.)
price-change-requires-fee: true
```

---

# 6. Warning Labels

Standardized warning labels must be used.

| Label        | Meaning                                                                  |
| ------------ | ------------------------------------------------------------------------ |
| NOTE         | Important behavior information                                           |
| WARNING      | Risk of data loss, corruption, duplication, or severe performance issues |
| ADVANCED     | Requires technical understanding                                         |
| EXPERIMENTAL | Feature may change or behave unpredictably                               |

Example:

```yaml
# WARNING: May corrupt shop data if misused.
# Always create a backup before enabling.
force-load-downgrade-items:
  enable: false
```

---

# 7. Allowed Values and Format Documentation

If a configuration option requires a specific format, the format must be documented.

Example:

```yaml
# Format:
#   <currencyName>;<symbol>
alternate-currency-symbol-list:
  - USD;$
```

For enum options:

```yaml
# Allowed values: protocollib, packetevents
display-protocol: protocollib
```

---

# 8. Performance Notes

If a configuration option affects performance, describe the **type of load**.

Preferred examples:

```
May increase database writes.
May load additional chunks during lookups.
Adds a database query per transaction.
```

Avoid vague wording like:

```
Could cause lag
```

---

# 9. YAML Formatting Rules

### Indentation

Use **2 spaces per level**.

Tabs are not allowed.

### Quoting

Only quote values when necessary.

Examples:

```
display-protocol: protocollib
sign-dye-color: ""
```

### Blank Lines

Use blank lines between logical blocks.

Avoid excessive whitespace inside lists or maps.

---

# 10. Default Values

Defaults must prioritize:

* stability
* safety
* predictable performance
* minimal surprises for server owners

Risky features should default to **false**.

---

# 11. Adding New Configuration Options

When adding a new option:

1. Choose the correct section
2. Use kebab-case naming
3. Write comments following the required structure
4. Choose a safe default
5. Document allowed values or formats
6. Ensure the option is discoverable near related options
7. Verify YAML formatting

---

# 12. Deprecation Policy

Configuration keys must **never be silently removed**.

If a configuration option is replaced:

* mark the old key as deprecated
* document the replacement
* keep compatibility for at least one major release

Example:

```yaml
# DEPRECATED: Use shop.new-setting instead.
# This will be removed in a future release.
old-setting: true
```

---

# 13. Templates

## Simple Boolean Option

```yaml
# Enable shop analytics sidebar in the UI.
# NOTE: UI-only setting. Does not affect transactions.
shop-analytics-sidebar: false
```

---

## Advanced Feature

```yaml
# Enable asynchronous shop scanning.
# Improves responsiveness on large servers.
# WARNING: Experimental feature.
# (Advanced: Uses async tasks to scan shops.)
async-shop-scan: false
```

---

## Structured Configuration Block

```yaml
# Market Watch
market-watch:
  # Enable market watch tracking.
  enable: false

  # Tracking interval in ticks.
  # Lower values increase database writes.
  interval-ticks: 1200
```

---

# 14. Contributor Checklist

When modifying `config.yml`, verify the following:

* Section headers use **simple comments (no separators)**
* Only the **file header** uses separator bars
* Keys use **kebab-case**
* Comments follow the **required structure**
* Risky options include **WARNING labels**
* YAML formatting uses **2-space indentation**
* Defaults are **safe and stable**