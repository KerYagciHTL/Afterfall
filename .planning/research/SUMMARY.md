# Project Research Summary

**Project:** Metro Builder Game
**Domain:** JavaFX MVC Desktop Application
**Researched:** 2026-04-20
**Confidence:** HIGH

## Executive Summary

The Metro Builder Game is a single-player desktop simulation built entirely with JavaFX and modern Java methodologies. Experts building robust desktop applications today rely on strict separation of concerns via the Model-View-Controller (MVC) pattern. In this system, the View is strictly declarative (FXML), the Model manages game state with reactive Properties, and the Controller solely orchestrates binding and inputs. 

The recommended technical approach leverages OpenJDK 21 LTS, JavaFX 21, and Gradle. For persistence, a serverless local SQLite database accessed via JDBC fits perfectly, supported by HikariCP for connection pooling. A heavy emphasis is placed on using JavaFX’s native capabilities (like unidirectional and bidirectional bindings and Observable Collections) over third-party UI bloat.

The primary risks for this kind of architecture stem from poor MVC enforcement—resulting in "God Controllers" that mix SQL queries, UI manipulation, and game logic—and concurrency crashes when blocking database calls halt the main UI thread. Mitigating these involves stringent boundary rules (the Model must never import `javafx.scene`), proper listener cleanup to avoid memory leaks, and moving JDBC operations strictly into background `Task` components that update the UI safely via `Platform.runLater()`.

## Key Findings

### Recommended Stack

The architecture relies entirely on native JavaFX and stable, lightweight database libraries suitable for single-player offline experiences. We explicitly avoid heavy ORMs (like JPA/Hibernate) and bulky UI libraries (like JFoenix).

**Core technologies:**
- **OpenJDK / JavaFX 21+:** Runtime & UI Framework — Current LTS releases offering excellent 2D rendering, FXML decoupling, and robust property binding.
- **SQLite JDBC (3.45+) & HikariCP (5.1.0):** Local Database & Pooling — Zero-config, file-based persistence that handles the required "JDBC-based data persistence" perfectly.
- **Gradle (8.7+):** Build Tool — Superior caching and JavaFX plugin integration compared to Maven.
- **JUnit 5.10+ & TestFX 4.0.18+:** Testing — Standard unit testing combined with automated UI interactions to ensure bindings hold.

### Expected Features

We are prioritizing the fundamental mechanics of a metro builder layout while strictly adhering to MVC principles. Heavy dynamic simulations are deferred to later versions to prevent scope creep.

**Must have (table stakes):**
- **Interactive Map Canvas** — Core spatial layout required to see stations and lines.
- **Node (Station) & Edge (Line) Generation** — Procedural/interactive mapping to create the network.
- **Line Management UI** — Interactive ListView to filter, select, and manage active lines.
- **Save/Load System** — Game state persistence fulfilling the JDBC requirement.

**Should have (competitive):**
- **Advanced Keyboard Shortcuts** — Fulfillment of key event requirements and power-user accessibility.
- **Real-Time Analytics Dashboard** — Demonstrates advanced bidirectional property bindings.

**Defer (v2+):**
- **Passenger Simulation & Train Entities** — Complex routing and AnimationTimer loops should only be tackled after static network and MVC persistence run flawlessly.
- **Multiplayer / 3D Graphics** — Anti-features that directly clash with the single-player desktop focus.

### Architecture Approach

The system enforces strict JavaFX MVC, utilizing native reactive bindings rather than manual polling or view mutation.

**Major components:**
1. **Model (`MetroMap`, `Station`, `Line`)** — Holds domain state and logic, exposing properties (`StringProperty`, `ObservableList`) directly for the UI to observe.
2. **View (FXML/Layouts)** — Declarative layout and initial UI routing. Fully observes the Model.
3. **Controller** — Modifies Model state based on inputs and dispatches data to DAOs. Keeps no complex game state itself.
4. **Persistence Layer (DAOs)** — Background service handling local SQLite queries.

### Critical Pitfalls

1. **The "God Controller" & MVC Bleed** — Avoid putting SQL logic, UI references, and game loops in one file. Keep the Model free of `javafx.scene` imports.
2. **UI Thread Blocking** — Prevent the application from hanging on saves. Ensure all JDBC calls execute inside `javafx.concurrent.Task` and UI updates use `Platform.runLater`.
3. **Property Binding Memory Leaks** — Avoid OutOfMemoryErrors as UI panels are swapped. Always clean up listeners via `dispose()` or rely on `WeakChangeListener`.
4. **Inefficient ListView Rendering** — Prevent lists from dropping scroll position or flickering by using property extractors in the `ObservableList` to notify changes seamlessly.

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Core Models & MVC Foundation
**Rationale:** The entire reactive system hinges on stable Models without UI bleed.
**Delivers:** Domain entities (`Station`, `Line`, `MetroMap`) wrapped in JavaFX `Property` wrappers, with basic JUnit validation.
**Addresses:** Establishes data structures needed for everything else.
**Avoids:** The "God Controller" pitfall by establishing strict import boundaries early.

### Phase 2: Local Database Integration
**Rationale:** Data schemas need to mirror the Models before interactive features are built on top of them.
**Delivers:** SQLite schema, JDBC connection manager via HikariCP, and foundational DAO services.
**Uses:** SQLite JDBC, HikariCP.
**Avoids:** Connection leaks and ensures isolated CRUD testing.

### Phase 3: Static Views & Binding Integration
**Rationale:** With Models and DB ready, the frontend skeleton can be wired up via reactive binding.
**Delivers:** FXML layouts, the Canvas foundation, and the interactive Line Management ListView.
**Addresses:** Line Management UI.
**Avoids:** Inefficient ListView rendering by initializing `ObservableList` with an Extractor.

### Phase 4: Interactive Gameplay & Events
**Rationale:** Connects user interactions to update the bound models.
**Delivers:** Node/Edge generation via mouse clicks, keyboard shortcuts, and map interactivity.
**Addresses:** Interactive Map Canvas, Node Generation, Edge Creation, Keyboard Shortcuts.
**Avoids:** Binding memory leaks (verifying dynamic UI elements dispose correctly).

### Phase 5: Full Persistence & Concurrency
**Rationale:** Unifies the interactive UI with the JDBC database.
**Delivers:** Save/Load System triggered by controllers via background tasks.
**Addresses:** Save/Load System.
**Avoids:** UI Thread Blocking by wrapping all DAOs in `javafx.concurrent.Task` and updating via `Platform.runLater()`.

### Phase Ordering Rationale

- **Dependency Driven:** We cannot bind UI if the Models lack Property wrappers, and we cannot save data if the schema doesn't exist. Thus, Models and Database precede UI.
- **Risk Mitigation:** Database concurrency is isolated and tested in Phase 5 *after* standard interactive state changes are verified in Phase 4.

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 3/4 (Complex Canvas Interactivity):** JavaFX 2D Canvas rendering optimization and accurate coordinate mapping for complex node-edge graph dragging.

Phases with standard patterns (skip research-phase):
- **Phase 1 & 2:** Standard Model-View-Controller property wrapping and JDBC SQLite DAOs are highly documented.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Validated against official OpenJDK and JavaFX specifications. |
| Features | HIGH | Clear mapping to requirements (`PROJECT.md`) focusing heavily on technical milestones over scope creep. |
| Architecture | HIGH | Standard JavaFX reactive design principles are well established. |
| Pitfalls | HIGH | Typical desktop concurrency and memory leak traps are fully documented. |

**Overall confidence:** HIGH

### Gaps to Address

- **Complex Routing Algorithm for Passenger Simulation:** Deferred to v2+, but determining the exact performance threshold for calculating routes synchronously vs asynchronously will require proof-of-concept testing.

## Sources

### Primary (HIGH confidence)
- Official JavaFX Documentation (Oracle/OpenJFX) — MVC patterns, concurrency in JavaFX, property binding.
- Java 21 LTS release specifications — Runtime compatibility.

### Secondary (MEDIUM confidence)
- Game design principles for transport routing puzzles — Feature landscape abstractions.
- General MVC Architecture guidelines for rich client desktop applications.

---
*Research completed: 2026-04-20*
*Ready for roadmap: yes*