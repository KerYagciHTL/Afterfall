# Core Gameplay Implementation Plan

## Overview

Implements the core metro-building game loop: lobby screen → game view → grid-based station and rail placement.

## Design Decisions

| Topic | Decision |
|-------|----------|
| Grid placement | Intersection points (where grid lines cross) |
| Station cost | 500 / 750 / 1000 for stations 1–3, then 1000 fixed |
| Station build time | 3 seconds (animated) |
| Rail placement | Auto-connect two intersections; also freely placeable on grid lines |
| Rail directions | Horizontal and vertical only |
| Rail cost | 100 per grid segment |
| Build menu style | Bottom toolbar |
| Build trigger | B key toggles toolbar; ESC exits tool / closes toolbar |
| Screen transition | Animated fade |
| ESC in game | Returns to normal view mode (not lobby) |
| Grid size | 40×30 cells (configurable constant) |
| Scroll / Zoom | Mouse drag to scroll, Ctrl+scroll to zoom |
| Starting budget | 10,000 |

---

## Tasks

### Task 1 — Screen Architecture & Transition
**Scope:** Navigation shell between lobby and game.

- `LobbyView.fxml`: title, player stats (username + formatted playtime), Start Game button
- `GameView.fxml`: empty shell with grid area + HUD sidebar placeholder
- Animated fade transition on Start Game
- ESC from game view returns to lobby

---

### Task 2 — Grid System
**Scope:** The scrollable, zoomable canvas that all gameplay sits on.

- `Canvas`-based grid rendered inside `MapView`
- Configurable constants: cell size, grid width (40), grid height (30)
- Scroll: mouse drag or scrollbars; Zoom: Ctrl+scroll wheel
- Intersection point hit detection (snap to nearest crossing within threshold)
- Coordinate transforms: screen ↔ grid

---

### Task 3 — HUD Sidebar
**Scope:** Reactive status panel alongside the grid.

- Money display (starts at 10,000; updates on spend)
- Lines list (empty for now, populates in a later task)
- Build tool status: "None" / "Station" / "Rail"
- All values bound to model properties

---

### Task 4 — Build Toolbar & Mode
**Scope:** UI entry point for placing objects.

- Bottom toolbar with "Station" and "Rail" buttons
- B key toggles toolbar open/closed
- Selecting a tool activates it (button highlight + HUD status update)
- ESC deactivates current tool / closes toolbar

---

### Task 5 — Station System
**Scope:** Placing and rendering stations on the grid.

- Click intersection → deduct cost → animated build progress (circle filling over 3 s) → station appears
- Cost schedule: 500 → 750 → 1000 → 1000 (fixed from station 4 onward)
- Visual: classic metro icon — black outline, white fill circle
- Stations stored in model, rendered on canvas

---

### Task 6 — Rail System
**Scope:** Connecting intersections with track.

- Rail tool active: click first intersection → click second → auto-connects via straight horizontal/vertical segments (L-shape if corners required)
- Can also place freely on any grid line (not only station-to-station)
- Cost: 100 per grid segment
- Visual: thick colored line along grid edges
- Rails stored in model, rendered on canvas

---

## Dependency Order

```
Task 1 (Screens)
    └── Task 2 (Grid)
            ├── Task 3 (HUD)      ← parallel
            └── Task 4 (Toolbar)  ← parallel
                    └── Task 5 (Stations)
                            └── Task 6 (Rails)
```

Each task leaves the app in a runnable, committable state.
