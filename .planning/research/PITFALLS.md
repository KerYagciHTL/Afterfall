# Pitfalls Research

**Domain:** JavaFX MVC Desktop Application (Game)
**Researched:** 2026-04-20
**Confidence:** HIGH

## Critical Pitfalls

### Pitfall 1: The "God Controller" & MVC Bleed

**What goes wrong:**
The Controller becomes a massive, unmaintainable file that directly executes SQL queries, houses game logic, and manually manipulates JavaFX UI Nodes. The MVC pattern collapses entirely.

**Why it happens:**
JavaFX `@FXML` injection natively places UI references in the Controller. Without strict discipline, developers intuitively place button click logic, database calls, and state management in the same method.

**How to avoid:**
Enforce a strict dependency rule: The Model must never import `javafx.scene.*` (though `javafx.beans.property.*` is acceptable). The View handles pure layout (FXML). The Controller only maps Model properties to View properties (Data Binding) and delegates UI events to Model commands. 

**Warning signs:**
- `java.sql.*` imports inside the Controller.
- Controller methods exceeding 50 lines.
- Passing `Button` or `ListView` objects into Model classes.

**Phase to address:**
Phase 1: Foundation & Architecture

---

### Pitfall 2: UI Thread Blocking & Concurrency Crashes

**What goes wrong:**
The game freezes entirely during saves/loads, or throws `IllegalStateException: Not on FX application thread` and crashes when background threads try to update the UI.

**Why it happens:**
JDBC calls are synchronous and blocking. If executed on the JavaFX Application Thread, the UI hangs until the database responds. If developers move JDBC to a background thread but update bound Model properties from that thread, JavaFX throws an exception because UI-bound properties must be updated on the FX Thread.

**How to avoid:**
Execute all JDBC logic inside `javafx.concurrent.Task` or standard background threads. Crucially, when the database operation completes, use `Platform.runLater(() -> modelProperty.set(newValue))` to apply the changes back to the Model properties that are bound to the View.

**Warning signs:**
- UI stutters or becomes unresponsive when adding a metro line.
- `Platform.runLater` is scattered randomly as a band-aid rather than localized in a service layer.

**Phase to address:**
Phase 2: Database Integration

---

### Pitfall 3: Property Binding Memory Leaks

**What goes wrong:**
The game consumes increasing amounts of memory the longer it runs, eventually leading to sluggish performance or `OutOfMemoryError`.

**Why it happens:**
Adding `ChangeListener` or establishing bindings creates strong object references. If a View is destroyed and recreated (e.g., opening and closing a Metro Line detail panel), the old View isn't garbage collected because the Model (which lives longer) still holds a reference to the View's listener.

**How to avoid:**
Always implement a `dispose()` or `cleanup()` method in Controllers to explicitly call `property.unbind()` and `removeListener()`. Alternatively, use `WeakChangeListener` and `WeakInvalidationListener`.

**Warning signs:**
- RAM usage climbs consistently when switching between different UI panes.
- Console prints multiple duplicate logs for a single event (indicating orphaned listeners are still firing).

**Phase to address:**
Phase 3: Interactive UI & Data Binding

---

### Pitfall 4: Inefficient ListView Rendering

**What goes wrong:**
The Metro Station `ListView` drops its selection, loses scroll position, or visibly flickers every time a station's status changes. Alternatively, changes to a station's name don't reflect in the list at all.

**Why it happens:**
Developers completely replace the underlying list (`listView.setItems(newList)`) whenever data changes, rather than mutating the existing `ObservableList`. Furthermore, standard `ObservableList` only tracks additions and removals, not internal mutations of the items.

**How to avoid:**
Initialize the `ObservableList` with an Extractor (`FXCollections.observableArrayList(station -> new Observable[]{station.nameProperty()})`). This tells the list to trigger an update event when an item's internal property changes, without needing to recreate the list.

**Warning signs:**
- Re-querying the whole database and rebuilding the list just to update one station's status.
- UI selection suddenly jumping to the top of the list.

**Phase to address:**
Phase 3: Interactive UI & Data Binding

---

## Technical Debt Patterns

Shortcuts that seem reasonable but create long-term problems.

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Skipping Extractors for ListViews | Faster initial setup, less boilerplate | Manual list refreshes cause UI jank and lost selection state | Never for core game lists |
| Hardcoding SQL Strings | Easy to write in the moment | SQL Injection risks, hard to refactor schema changes | Only for MVP database setup scripts |
| Bi-directional binding everywhere | Less code to synchronize UI/Model | Infinite update loops, hard to debug state changes | Simple forms; avoid for complex game state |

## Integration Gotchas

Common mistakes when connecting JavaFX to JDBC.

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| JDBC Connection | Opening a new connection per query | Use a Connection Pool (e.g., HikariCP) or a Singleton connection manager for SQLite |
| JDBC Resources | Leaving `ResultSet` or `PreparedStatement` open | Use strict `try-with-resources` blocks for all database operations |
| SQLite Concurrency | Concurrent writes causing `database is locked` | Serialize database writes, or use WAL (Write-Ahead Logging) mode |

## Performance Traps

Patterns that work at small scale but fail as usage grows.

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| N+1 Query Problem | Slow loading of Metro Lines | Use JOINs to fetch Line and its Stations together | > 10 lines / 50 stations |
| Heavy Cell Factories | Scrolling the ListView lags | Only instantiate UI nodes once per cell, reuse them in `updateItem` | > 20 items in view |
| Polling DB for state | High CPU/Disk usage | Keep source-of-truth in Memory (Model), push to DB asynchronously | > 1 query per second |

## "Looks Done But Isn't" Checklist

Things that appear complete but are missing critical pieces.

- [ ] **Data Binding:** Works for text, but often missing formatting converters (e.g., Currency/Time formatters).
- [ ] **ListView Updates:** Adds/removes work, but verify that editing an *existing* item updates the cell text.
- [ ] **JDBC Saves:** Save works, but verify that exceptions (like unique constraint violations) are caught and shown to the user gracefully.
- [ ] **Window Close:** Clicking the 'X' closes the window, but verify that background threads and database connections are cleanly shut down.

## Recovery Strategies

When pitfalls occur despite prevention, how to recover.

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| UI Thread Blocking | MEDIUM | Isolate DB calls, wrap in `Task<T>`, use `setOnSucceeded` to update Model. |
| Memory Leaks | HIGH | Profile with VisualVM, track GC roots, implement `dispose()` methods across all controllers. |
| God Controller | HIGH | Extract a pure Java class as the Model, move logic there, replace Controller code with property bindings. |

## Pitfall-to-Phase Mapping

How roadmap phases should address these pitfalls.

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| God Controller | Phase 1: Foundation | Code Review: Controller has 0 business logic, Model has 0 `javafx.scene` imports. |
| Connection Leaks | Phase 2: Database | Run app, perform 100 saves, verify DB connection count remains stable. |
| UI Thread Blocking | Phase 2: Database | Add `Thread.sleep(2000)` to DB mock; verify UI remains interactive (buttons click). |
| Binding Leaks | Phase 3: Complex UI | Open/close panels repeatedly; check memory profiler for stable heap size. |
| ListView Flickering | Phase 3: Complex UI | Edit a station's property; verify cell updates without losing current scroll position. |

## Sources

- Official JavaFX Documentation (Concurrency in JavaFX)
- JavaFX Memory Leak Discussions (WeakListeners, Binding cleanup)
- JDBC Best Practices (try-with-resources, thread safety)
- Classic MVC Architecture guidelines for rich client applications
---
*Pitfalls research for: JavaFX MVC Metro Builder Game*
*Researched: 2026-04-20*