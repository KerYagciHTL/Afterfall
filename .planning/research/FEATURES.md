# Feature Landscape

**Domain:** JavaFX MVC Metro Builder Game
**Researched:** 2026-04-20

## Table Stakes

Features users expect in a Metro-building simulation. Missing these means the product feels incomplete and fails to meet basic genre or project requirements.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **Interactive Map Canvas** | Core gameplay area. Users need to see stations, lines, and moving trains. | High | Requires rendering a dynamic 2D grid/plane in JavaFX. Must update cleanly without performance drops. |
| **Node (Station) Generation** | The game must spawn stations for players to connect. | Medium | Needs a random/procedural generation logic tied to the Model, updating the View automatically. |
| **Edge (Line) Creation** | Connecting stations is the fundamental gameplay loop. | Medium | Requires drag-and-drop or click-to-connect interactions (EventHandlers). |
| **Passenger Simulation** | Gives purpose to the lines. Passengers need destinations. | High | Complex routing algorithms and state management needed in the Model. |
| **Train Entities** | Trains must move along lines picking up and dropping off passengers. | High | Requires a game loop (Timeline/AnimationTimer in JavaFX) to update positions. |
| **Save/Load System** | Players want to save their progress and custom networks. | Low | Explicitly required via **JDBC**. Needs schema for Stations, Lines, and Game State. |
| **Line Management UI** | Players must be able to view and manage active lines. | Medium | Explicitly required via **Interactive ListView**. Must support selection, sorting, and filtering (e.g., sort by passenger load). |

## Differentiators

Features that set the product apart or deeply leverage the specific technical requirements (JavaFX Property Binding, MVC).

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| **Real-Time Analytics Dashboard** | Showcases advanced unidirectional and bidirectional Property Binding. Live stats on station crowding and line efficiency. | Medium | Deeply integrates JavaFX reactive UI concepts. High educational and gameplay value. |
| **Advanced Keyboard Shortcuts** | Enables power-user controls (e.g., quick-swapping lines, pausing time). | Low | Explicitly fulfills the **KeyEvents** requirement while improving Game Feel. |
| **Dynamic Event System** | Introduces challenges (e.g., Rush Hour, Temporary Station Closures) forcing players to adapt their network. | Medium | Keeps gameplay fresh and forces dynamic updates to the Map and UI. |
| **Replay/Timeline Scrubber** | Allows players to scroll back through their network's history. | High | Very impressive feature that relies heavily on a clean MVC separation and immutable state snapshots. |

## Anti-Features

Features to explicitly NOT build to maintain scope and focus on architecture.

| Anti-Feature | Why Avoid | What to Do Instead |
|--------------|-----------|-------------------|
| **Multiplayer / Co-op** | Explicitly out of scope in `PROJECT.md`. Adds immense network and state synchronization complexity. | Focus on a robust, highly polished single-player sandbox experience. |
| **3D Graphics** | Explicitly out of scope. JavaFX 3D is clunky and distracts from core MVC UI goals. | Use clean, minimalist 2D vector graphics (like Mini Metro) on a standard JavaFX Canvas or Pane. |
| **Deep Economic/Budget Simulation** | Tracking precise dollars, ticket prices, and maintenance costs distracts from spatial puzzle mechanics. | Use simpler constraints (e.g., maximum number of lines or trains) to gate progression. |
| **Complex Terrain Generation** | Water bodies, mountains, and varied elevations complicate line routing and rendering. | Stick to a flat grid or simple geometric obstacles if necessary. |

## Feature Dependencies

```text
Interactive Map Canvas → Node Generation (Canvas needs objects to draw)
Node Generation → Edge Creation (Lines need stations to connect)
Edge Creation → Train Entities (Trains need lines to travel on)
Node Generation → Passenger Simulation (Passengers spawn at stations)
Train Entities → Passenger Simulation (Trains transport passengers)
Save/Load System → [All Core Gameplay State]
Line Management UI → Edge Creation (Needs lines to display and manage)
Real-Time Analytics Dashboard → Passenger Simulation & Train Entities
```

## MVP Recommendation

Prioritize building these to establish the core MVC architecture and technical requirements:
1. **Interactive Map Canvas** + **Node Generation** (Establish Model and View)
2. **Edge Creation** (Establish Controller and EventHandlers)
3. **Line Management UI** (Satisfy ListView and Property Binding requirements)
4. **Save/Load System** (Satisfy JDBC requirements early to lock in schema)

Defer: 
- **Passenger Simulation** and **Train Entities**: Build these *after* the static management of stations and lines works perfectly, as they introduce complex game loops.
- **Real-Time Analytics Dashboard**: Wait until the core simulation is generating interesting data to bind to.

## Sources

- `.planning/PROJECT.md`
- General game design principles for transport routing puzzles (e.g., Mini Metro, Freeways)
- JavaFX Architecture Best Practices (MVC, Property Binding, AnimationTimer)