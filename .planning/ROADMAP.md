# Project Roadmap

## Phases
- [ ] **Phase 1: Foundation & Models** - Establish core MVC architecture and database schema
- [ ] **Phase 2: Interactive Map & Gameplay** - Users can build and simulate a basic metro network
- [ ] **Phase 3: Line Management & Data Binding** - Users can manage lines through a reactive UI
- [ ] **Phase 4: Persistence** - Users can save and load their metro networks

## Phase Details

### Phase 1: Foundation & Models
**Goal**: Establish the core MVC architecture and database schema.
**Depends on**: None
**Requirements**: ARCH-01, ARCH-02, ARCH-03
**Success Criteria**:
  1. Application launches with a distinct separation of Model, View, and Controller classes.
  2. Core game state is observable via JavaFX Properties.
  3. Local SQLite database file is successfully created on startup.
**Plans**: TBD

### Phase 2: Interactive Map & Gameplay
**Goal**: Users can build and simulate a basic metro network on the map.
**Depends on**: Phase 1
**Requirements**: GAME-01, GAME-02, GAME-03, GAME-04, UI-06
**Success Criteria**:
  1. User can click on the map to place station nodes.
  2. User can connect stations to form metro lines.
  3. User can see visual representation of trains and passengers moving along the lines.
  4. User can use keyboard shortcuts to trigger game actions.
**Plans**: TBD
**UI hint**: yes

### Phase 3: Line Management & Data Binding
**Goal**: Users can manage their metro lines through a reactive UI.
**Depends on**: Phase 2
**Requirements**: GAME-05, UI-01, UI-02, UI-03, UI-04, UI-05
**Success Criteria**:
  1. User can view a list of all created lines that updates automatically.
  2. User can select a line from the list to see its details.
  3. User can sort and filter the lines in the list.
  4. UI elements automatically sync with game state changes without manual refresh.
**Plans**: TBD
**UI hint**: yes

### Phase 4: Persistence
**Goal**: Users can save and load their metro networks.
**Depends on**: Phase 3
**Requirements**: DATA-01, DATA-02, DATA-03
**Success Criteria**:
  1. User can save the current map layout and lines to the database.
  2. User can load a previously saved game and resume playing.
  3. UI remains responsive (no freezing) while saving or loading data.
**Plans**: TBD

## Progress

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Foundation & Models | 0/0 | Not started | - |
| 2. Interactive Map & Gameplay | 0/0 | Not started | - |
| 3. Line Management & Data Binding | 0/0 | Not started | - |
| 4. Persistence | 0/0 | Not started | - |
