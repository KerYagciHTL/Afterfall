# Requirements: Metro Builder Game

**Defined:** 2026-04-20
**Core Value:** A highly interactive JavaFX application that seamlessly demonstrates advanced concepts like Property Binding, dynamic ListViews, and reliable JDBC-based data persistence.

## v1 Requirements

### Architecture & Foundation

- [ ] **ARCH-01**: Implement strict MVC (Model-View-Controller) structure, separating UI from business logic.
- [ ] **ARCH-02**: Game state must be represented by JavaFX Properties (StringProperty, IntegerProperty, ObservableList) in the Model.
- [ ] **ARCH-03**: Setup SQLite database for local data persistence.

### Core Gameplay & UI

- [ ] **GAME-01**: User can interact with a 2D map canvas to place stations (nodes).
- [ ] **GAME-02**: User can create lines (edges) connecting stations.
- [ ] **GAME-03**: System simulates basic passenger movement between stations.
- [ ] **GAME-04**: System simulates train movement along created lines.
- [ ] **GAME-05**: User can manage lines via a dynamic ListView.

### Advanced UI Interactions

- [ ] **UI-01**: ListView automatically reacts to underlying data changes (add/remove lines).
- [ ] **UI-02**: ListView supports selecting lines to view details.
- [ ] **UI-03**: ListView supports sorting lines (e.g., by name or passenger count).
- [ ] **UI-04**: ListView supports filtering lines based on user input.
- [ ] **UI-05**: UI elements utilize unidirectional and bidirectional Property Binding to stay in sync with the Model.
- [ ] **UI-06**: System handles KeyEvents (e.g., shortcuts for tools or actions) via EventHandlers.

### Persistence

- [ ] **DATA-01**: User can save the current game state to the SQLite database via JDBC.
- [ ] **DATA-02**: User can load a previously saved game state from the SQLite database via JDBC.
- [ ] **DATA-03**: Database operations must run asynchronously (e.g., using Task) to prevent UI thread freezing.

## v2 Requirements

### Analytics & Polish

- **V2-01**: Real-Time Analytics Dashboard for passenger flow.
- **V2-02**: Advanced Keyboard Shortcuts and macro management.
- **V2-03**: Dynamic Event System (e.g., rush hours, delays).

## Out of Scope

| Feature | Reason |
|---------|--------|
| Multiplayer | Focus is on demonstrating core JavaFX features in a single-player environment. |
| Complex 3D Graphics | JavaFX 2D UI components are sufficient for the current scope. |
| Deep economic simulations | Adds unnecessary complexity unrelated to UI and MVC architecture goals. |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| ARCH-01 | Phase 1 | Pending |
| ARCH-02 | Phase 1 | Pending |
| ARCH-03 | Phase 1 | Pending |
| GAME-01 | Phase 2 | Pending |
| GAME-02 | Phase 2 | Pending |
| GAME-03 | Phase 2 | Pending |
| GAME-04 | Phase 2 | Pending |
| GAME-05 | Phase 3 | Pending |
| UI-01 | Phase 3 | Pending |
| UI-02 | Phase 3 | Pending |
| UI-03 | Phase 3 | Pending |
| UI-04 | Phase 3 | Pending |
| UI-05 | Phase 3 | Pending |
| UI-06 | Phase 2 | Pending |
| DATA-01 | Phase 4 | Pending |
| DATA-02 | Phase 4 | Pending |
| DATA-03 | Phase 4 | Pending |

**Coverage:**
- v1 requirements: 17 total
- Mapped to phases: 17
- Unmapped: 0 ✓

---
*Requirements defined: 2026-04-20*
*Last updated: 2026-04-20 after initial definition*
