# Architecture Patterns

**Domain:** JavaFX MVC Metro Builder Game
**Researched:** 2026-04-20

## Recommended Architecture

The system strictly adheres to the **Model-View-Controller (MVC)** architectural pattern, leveraging JavaFX's native reactive capabilities (Property Binding and Observable Collections) and a dedicated persistence layer via JDBC.

### Component Boundaries

| Component | Responsibility | Communicates With |
|-----------|---------------|-------------------|
| **Model** (`MetroMap`, `Station`, `Line`) | Holds domain state and business logic. Exposes properties (`StringProperty`, `ObservableList`) for observation. | Observed by Views; Modified by Controllers. |
| **View** (FXML or Java-based Layouts) | Renders the UI (e.g., Map Canvas, interactive ListViews). Defines layout and basic input targets. | Observes Model (via Binding); Triggers events on Controllers. |
| **Controller** (`MainController`, etc.) | Handles user interactions (EventHandlers, KeyEvents). Orchestrates data flow between Model and DB. | Listens to Views; Modifies Model; Calls Persistence Layer. |
| **Persistence / DAOs** | Manages JDBC database connections, executing CRUD operations for stations and lines. | Called by Controllers; Retrieves/Saves Model data. |

### Data Flow

1. **User Interaction:** The user interacts with the **View** (e.g., selects a station in the ListView or presses a key on the map).
2. **Event Handling:** The **Controller** catches the event via injected `@FXML` handlers or programmatic `EventHandlers`.
3. **State Mutation / DB Call:** 
   - If persistence is required, the Controller dispatches a request to the **Persistence Layer (DAO)**.
   - The Controller updates the **Model**'s state.
4. **Reactive UI Update:** The **View** automatically updates its display because its elements are bound to the **Model**'s `Property` or `ObservableList` (via unidirectional or bidirectional binding).

## Patterns to Follow

### Pattern 1: Reactive Model-View Binding
**What:** Views observe Models directly using JavaFX property bindings rather than Controllers manually updating View fields.
**When:** Always, for syncing UI state (like ListViews or TextFields) with backend data.
**Example:**
```java
// In Controller during initialization
stationListView.setItems(model.getObservableStationList());
stationNameTextField.textProperty().bindBidirectional(model.selectedStationNameProperty());
```

### Pattern 2: Background Task Persistence
**What:** Offloading JDBC operations to background threads using `javafx.concurrent.Task` to avoid blocking the UI.
**When:** Any time the application reads from or writes to the database.
**Example:**
```java
Task<Void> saveTask = new Task<>() {
    @Override protected Void call() throws Exception {
        stationDao.save(currentStation);
        return null;
    }
};
new Thread(saveTask).start();
```

## Anti-Patterns to Avoid

### Anti-Pattern 1: God Controller
**What:** Placing business logic, database queries, and view manipulation inside a single Controller class.
**Why bad:** Violates MVC, makes the code unmaintainable, untestable, and negates the benefits of JavaFX Property Binding.
**Instead:** Keep Controllers thin. Delegate logic to the Model and DB queries to dedicated DAOs. Rely on binding for UI updates.

### Anti-Pattern 2: Blocking the JavaFX Application Thread
**What:** Running synchronous JDBC database queries directly inside an EventHandler on the main thread.
**Why bad:** The UI will freeze while waiting for the database to respond, resulting in a poor user experience.
**Instead:** Use `Task` or `CompletableFuture` to handle DB operations asynchronously, updating the UI safely via `Platform.runLater()` if manual updates are needed.

## Suggested Build Order (Dependencies)

To ensure a smooth implementation based on dependencies, phases should be ordered as follows:

1. **Core Models & Properties:** Implement domain entities (`Station`, `Line`, `MetroMap`) using JavaFX `Property` wrappers and `ObservableList`. *(Foundation for everything).*
2. **Persistence Layer (JDBC):** Establish the database schema, connection manager, and DAOs. Verify CRUD operations independently of the UI.
3. **Static Views & Layouts:** Build the UI skeleton (Canvas for the map, ListViews for controls) using FXML or Java code.
4. **Controllers & Binding Integration:** Wire Models to Views. Implement unidirectional/bidirectional binding and populate the ListView dynamically.
5. **Interactive Event Handling:** Add KeyEvents, MouseEvents, and complex ListView interactions (selection, filtering, sorting).
6. **Persistence Integration:** Connect user actions in the Controller to background JDBC tasks to achieve full data lifecycle management.

## Sources

- JavaFX Official Documentation (Oracle/OpenJFX) - High confidence
- Standard MVC/DAO architectural patterns in Java - High confidence
