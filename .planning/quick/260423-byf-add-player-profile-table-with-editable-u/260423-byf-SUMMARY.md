---
phase: quick
plan: 260423-byf
subsystem: persistence, model, ui
tags: [player-profile, jdbc, hikaricp, property-binding, fxml, mvc]
dependency_graph:
  requires: []
  provides: [player-profile-persistence, session-playtime-tracking, username-binding]
  affects: [Main.java, MainController.java, schema.sql, main.fxml]
tech_stack:
  added: [HikariCP DataSource, SQLite player_profile table, PlayerProfile JavaFX model, PlayerProfileDao JDBC DAO]
  patterns: [JavaFX LongProperty/StringProperty with named bean constructor, bidirectional TextField binding, Bindings.createStringBinding for derived label, init()/stop() Application lifecycle hooks]
key_files:
  created:
    - src/main/java/com/metrobuilder/db/DatabaseManager.java
    - src/main/java/com/metrobuilder/model/PlayerProfile.java
    - src/main/java/com/metrobuilder/model/dao/PlayerProfileDao.java
  modified:
    - src/main/resources/database/schema.sql
    - src/main/java/com/metrobuilder/app/Main.java
    - src/main/java/com/metrobuilder/controller/MainController.java
    - src/main/resources/fxml/main.fxml
decisions:
  - Tasks 2 and 3 executed jointly because Main.java references MainController.setProfile() which only exists after Task 3; each was committed separately after combined compilation passed.
metrics:
  duration: ~10 minutes
  completed: 2026-04-23
  tasks_completed: 3
  files_changed: 7
---

# Quick Task 260423-byf: Add Player Profile Table with Editable Username and Cumulative Playtime

**One-liner:** HikariCP-backed SQLite player_profile singleton row with JavaFX bidirectional username binding and session-accumulating HH:MM:SS playtime label.

## What Was Built

A complete player identity and session-tracking layer wired end-to-end through MVC:

- **player_profile DDL** appended to `schema.sql` — single row enforced by `CHECK(id = 1)`, defaulting to username `'Player'` and `total_playtime_seconds = 0`.
- **DatabaseManager** — static HikariCP pool singleton with `initialize()`, `getDataSource()`, and `shutdown()`. Runs the full `schema.sql` on first connection via classpath resource.
- **PlayerProfile** — JavaFX model with `StringProperty username` and `LongProperty totalPlaytimeSeconds` following the `SimpleXxxProperty(this, "fieldName", defaultValue)` bean pattern from `Station.java`.
- **PlayerProfileDao** — JDBC DAO with `load()` (INSERT OR IGNORE then SELECT) and `save()` (UPDATE WHERE id=1).
- **Main.java** rewritten — `init()` initialises DB and loads profile; `start()` passes profile to controller post-load; `stop()` accumulates elapsed seconds and persists.
- **MainController** extended — `setProfile()` establishes bidirectional `usernameField <-> profile.usernameProperty()` binding and a derived `Bindings.createStringBinding` for the `playtimeLabel` formatted as `HH:MM:SS`.
- **main.fxml** updated — added HBox for username label + TextField, playtime label with fx:id wiring.

## Tasks Completed

| Task | Name | Commit |
|------|------|--------|
| 1 | Schema, Model, DAO, and DB init layer | f5ea365 |
| 2 | Lifecycle wiring in Main.java | 6fd0bc5 |
| 3 | Controller binding + FXML UI | d02503a |

## Deviations from Plan

### Execution Order Adjustment

Tasks 2 and 3 had a compile-time dependency: `Main.java` calls `controller.setProfile()` which only exists after `MainController` is updated in Task 3. After committing Task 1, Task 2 was written (Main.java) but the compilation check correctly showed the missing symbol. Task 3 files (controller + FXML) were written immediately to resolve the dependency, then both were verified to compile together. Each task was committed separately after the combined compilation passed, preserving the intended atomic commit structure.

No rule violations occurred. No architectural changes were needed.

## Known Stubs

None. All data flows are fully wired: profile loads from SQLite on startup, username is bidirectionally bound to the TextField, playtime accumulates per session and persists on close.

## Self-Check: PASSED
