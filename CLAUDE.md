<!-- GSD:project-start source:PROJECT.md -->
## Project

**Metro Builder Game**

A metro building simulation game developed in JavaFX. It allows players to construct and manage a metro system while adhering to a strict MVC (Model-View-Controller) architecture.

**Core Value:** A highly interactive JavaFX application that seamlessly demonstrates advanced concepts like Property Binding, dynamic ListViews, and reliable JDBC-based data persistence.

### Constraints

- **Tech stack**: JavaFX, JDBC — Specified by the requirements
- **Architecture**: MVC Pattern — Must be strictly followed for the core structure
<!-- GSD:project-end -->

<!-- GSD:stack-start source:research/STACK.md -->
## Technology Stack

## Recommended Stack
### Core Framework
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| OpenJDK | 21.0+ | Runtime Environment | Current Long Term Support (LTS) release, stable for desktop apps. |
| JavaFX | 21.0.2+ | UI Framework | Matches LTS JDK. Provides robust 2D rendering, FXML, and Property Binding required by the project. |
| JavaFX FXML | 21.0.2+ | View Definition | Enforces strict MVC separation by decoupling UI layout (XML) from application logic (Controller). |
### Database & Persistence
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| SQLite JDBC | 3.45+ | Local Database | Zero-configuration, serverless, file-based database perfect for a single-player simulation game. |
| HikariCP | 5.1.0 | Connection Pooling | Reliable and high-performance JDBC connection pooling to manage DB access efficiently. |
### Build & Tooling
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Maven | 3.9+ | Build Tool | Actual build tool in use (`pom.xml`). Use `mvn compile`, `mvn javafx:run`. |
### Testing
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| JUnit | 5.10+ | Unit Testing | Industry standard for testing Java backend logic (Models and Controllers). |
| TestFX | 4.0.18+ | UI Testing | Essential for programmatic testing of JavaFX components, bindings, and ListViews without manual clicking. |
## Alternatives Considered
| Category | Recommended | Alternative | Why Not |
|----------|-------------|-------------|---------|
| Architecture | Strict FXML MVC | Programmatic UI | Programmatic UI in JavaFX mixes View and Controller code too easily, violating the strict MVC requirement. |
| Database | SQLite | MySQL / PostgreSQL | Overkill for a single-player game. Requires users to run a database server locally. |
| Build Tool | Gradle | Maven | Maven is perfectly viable but Gradle's build caching and conciseness make it superior for rapid game iteration. |
| ORM | Raw JDBC | Hibernate / JPA | The requirements specifically state "JDBC-based data persistence". Full ORMs add unnecessary bloat and complexity for a simple metro builder. |
## Anti-Patterns & What NOT to Use
| Anti-Feature | Why Avoid | What to Do Instead |
|--------------|-----------|-------------------|
| JFoenix / MaterialFX | Heavy dependency that overrides standard JavaFX themes and often causes rendering lag in game loops. | Use standard JavaFX controls and style them via custom CSS. |
| Swing / AWT | Legacy, non-hardware-accelerated, and strictly prohibited when building modern JavaFX apps. | Exclusively use `javafx.*` packages. |
| Global Singletons for State | Makes testing impossible and violates MVC data flow. | Pass Model instances to Controllers via dependency injection or factory methods. |
| Logic in Controllers | Leads to "Massive View Controller" anti-pattern. | Keep Controllers thin; they should only bind Model properties to View elements and delegate user actions to the Model. |
## Installation
## Sources
- Official JavaFX Documentation (HIGH confidence)
- Maven Central repository for current stable versions (HIGH confidence)
- Java 21 LTS release specifications (HIGH confidence)
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

Conventions not yet established. Will populate as patterns emerge during development.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

Architecture not yet mapped. Follow existing patterns found in the codebase.
<!-- GSD:architecture-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd:quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd:debug` for investigation and bug fixing
- `/gsd:execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd:profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
