# Metro Builder Game

## What This Is

A metro building simulation game developed in JavaFX. It allows players to construct and manage a metro system while adhering to a strict MVC (Model-View-Controller) architecture.

## Core Value

A highly interactive JavaFX application that seamlessly demonstrates advanced concepts like Property Binding, dynamic ListViews, and reliable JDBC-based data persistence.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] MVC Pattern as the core structural foundation
- [ ] Interactive ListView with reactive changes/selection, sorting, and filtering
- [ ] Property Binding (both unidirectional and bidirectional)
- [ ] EventHandlers for user interaction (e.g., KeyEvents)
- [ ] Data persistence via JDBC

### Out of Scope

- Multiplayer features — Focus is on demonstrating core JavaFX features in a single-player environment
- Complex 3D Graphics — JavaFX 2D UI components are sufficient for the current scope

## Context

The project serves as a practical implementation for a JavaFX application, ensuring clean architecture (MVC) and database integration.

## Constraints

- **Tech stack**: JavaFX, JDBC — Specified by the requirements
- **Architecture**: MVC Pattern — Must be strictly followed for the core structure

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Use JavaFX | Primary requested technology for UI | — Pending |
| JDBC for Persistence | Required for data persistence | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-20 after initialization*
