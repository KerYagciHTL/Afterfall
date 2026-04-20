# Technology Stack

**Project:** Metro Builder Game
**Researched:** 2026-04-20
**Overall Confidence:** HIGH

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
| Gradle | 8.7+ | Build Tool | Faster builds than Maven, excellent Kotlin DSL, and top-tier support for JavaFX via plugins. |
| org.openjfx.javafxplugin | 0.1.0+ | JavaFX Integration | Official Gradle plugin for seamless JavaFX dependency management and running. |

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

```gradle
// build.gradle.kts
plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
    version = "21.0.2"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.45.1")
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.testfx:testfx-core:4.0.18")
    testImplementation("org.testfx:testfx-junit5:4.0.18")
}
```

## Sources

- Official JavaFX Documentation (HIGH confidence)
- Maven Central repository for current stable versions (HIGH confidence)
- Java 21 LTS release specifications (HIGH confidence)