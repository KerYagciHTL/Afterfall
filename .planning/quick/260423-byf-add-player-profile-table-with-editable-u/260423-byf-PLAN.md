---
phase: quick
plan: 260423-byf
type: execute
wave: 1
depends_on: []
files_modified:
  - src/main/resources/database/schema.sql
  - src/main/java/com/metrobuilder/model/PlayerProfile.java
  - src/main/java/com/metrobuilder/model/dao/PlayerProfileDao.java
  - src/main/java/com/metrobuilder/db/DatabaseManager.java
  - src/main/java/com/metrobuilder/app/Main.java
  - src/main/resources/fxml/main.fxml
  - src/main/java/com/metrobuilder/controller/MainController.java
autonomous: true
requirements: []

must_haves:
  truths:
    - "App loads and creates the player_profile row automatically on first run"
    - "Username TextField is visible and editable; changes reflect on the model live"
    - "Playtime label shows accumulated seconds formatted as HH:MM:SS and increments each session"
    - "Profile persists across restarts (username and total_playtime_seconds survive app close/reopen)"
  artifacts:
    - path: "src/main/resources/database/schema.sql"
      provides: "player_profile table DDL"
      contains: "CHECK(id = 1)"
    - path: "src/main/java/com/metrobuilder/model/PlayerProfile.java"
      provides: "JavaFX property model"
      exports: ["usernameProperty", "totalPlaytimeSecondsProperty"]
    - path: "src/main/java/com/metrobuilder/model/dao/PlayerProfileDao.java"
      provides: "load() and save() JDBC operations"
    - path: "src/main/java/com/metrobuilder/db/DatabaseManager.java"
      provides: "HikariCP DataSource singleton + schema init"
    - path: "src/main/java/com/metrobuilder/app/Main.java"
      provides: "Application lifecycle with playtime tracking"
    - path: "src/main/resources/fxml/main.fxml"
      provides: "TextField + playtime Label wired to fx:id"
    - path: "src/main/java/com/metrobuilder/controller/MainController.java"
      provides: "initialize() loads profile, bidirectional username binding"
  key_links:
    - from: "Main.java stop()"
      to: "PlayerProfileDao.save()"
      via: "elapsed seconds added to totalPlaytimeSeconds before save"
    - from: "MainController.initialize()"
      to: "PlayerProfileDao.load()"
      via: "direct DAO call returning PlayerProfile"
    - from: "usernameTextField (FXML)"
      to: "profile.usernameProperty()"
      via: "Bindings.bindBidirectional in controller"
---

<objective>
Add a player_profile table with editable username and cumulative playtime tracking.

Purpose: Gives the game a persistent player identity and session-aware playtime counter, demonstrating Property Binding and JDBC persistence as required by the project's core value.
Output: PlayerProfile model, DAO, DB init layer, shutdown hook in Main, and UI TextField + playtime label wired through MainController.
</objective>

<execution_context>
@$HOME/.claude/get-shit-done/workflows/execute-plan.md
@$HOME/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md

<!-- Existing source files (read before editing) -->
@src/main/resources/database/schema.sql
@src/main/java/com/metrobuilder/app/Main.java
@src/main/java/com/metrobuilder/controller/MainController.java
@src/main/resources/fxml/main.fxml
@src/main/java/com/metrobuilder/model/Station.java

<interfaces>
<!-- Existing model pattern — follow this for PlayerProfile -->
<!-- From src/main/java/com/metrobuilder/model/Station.java -->
```java
// Property naming convention: SimpleXxxProperty(this, "fieldName")
// Accessor pattern: getX(), setX(v), xProperty()
private final StringProperty name = new SimpleStringProperty(this, "name");
public String getName() { return name.get(); }
public void setName(String v) { this.name.set(v); }
public StringProperty nameProperty() { return name; }
```

<!-- Build tool: Maven (pom.xml). Run with: mvn javafx:run -->
<!-- Dependencies already in pom.xml: sqlite-jdbc 3.45.1.0, HikariCP 5.1.0, slf4j-simple 2.0.12 -->
<!-- DB file location convention (not yet established — use: metro-builder.db in working dir) -->
<!-- No DatabaseManager or DAO exists yet — create from scratch -->
</interfaces>
</context>

<tasks>

<task type="auto">
  <name>Task 1: Schema, Model, DAO, and DB init layer</name>
  <files>
    src/main/resources/database/schema.sql,
    src/main/java/com/metrobuilder/model/PlayerProfile.java,
    src/main/java/com/metrobuilder/model/dao/PlayerProfileDao.java,
    src/main/java/com/metrobuilder/db/DatabaseManager.java
  </files>
  <action>
**schema.sql** — Append after the existing `lines` table DDL:

```sql
CREATE TABLE IF NOT EXISTS player_profile (
    id INTEGER PRIMARY KEY CHECK(id = 1),
    username TEXT NOT NULL DEFAULT 'Player',
    total_playtime_seconds INTEGER NOT NULL DEFAULT 0
);
```

**DatabaseManager.java** — New class in package `com.metrobuilder.db`. Provides a HikariCP DataSource and runs schema.sql on first connection:

```java
package com.metrobuilder.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseManager {
    private static HikariDataSource dataSource;

    public static void initialize() {
        if (dataSource != null) return;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:metro-builder.db");
        config.setMaximumPoolSize(4);
        config.setConnectionTimeout(30_000);
        dataSource = new HikariDataSource(config);
        runSchema();
    }

    public static DataSource getDataSource() {
        if (dataSource == null) throw new IllegalStateException("DatabaseManager not initialized");
        return dataSource;
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) dataSource.close();
    }

    private static void runSchema() {
        try (InputStream is = DatabaseManager.class.getResourceAsStream("/database/schema.sql");
             Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            // Execute each statement separated by semicolons
            for (String s : sql.split(";")) {
                String trimmed = s.strip();
                if (!trimmed.isEmpty()) stmt.execute(trimmed);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to run schema", e);
        }
    }
}
```

**PlayerProfile.java** — New class in `com.metrobuilder.model`, following the Station.java property pattern:

```java
package com.metrobuilder.model;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PlayerProfile {
    private final StringProperty username = new SimpleStringProperty(this, "username", "Player");
    private final LongProperty totalPlaytimeSeconds = new SimpleLongProperty(this, "totalPlaytimeSeconds", 0L);

    public PlayerProfile(String username, long totalPlaytimeSeconds) {
        this.username.set(username);
        this.totalPlaytimeSeconds.set(totalPlaytimeSeconds);
    }

    public String getUsername() { return username.get(); }
    public void setUsername(String v) { username.set(v); }
    public StringProperty usernameProperty() { return username; }

    public long getTotalPlaytimeSeconds() { return totalPlaytimeSeconds.get(); }
    public void setTotalPlaytimeSeconds(long v) { totalPlaytimeSeconds.set(v); }
    public LongProperty totalPlaytimeSecondsProperty() { return totalPlaytimeSeconds; }
}
```

**PlayerProfileDao.java** — New class in package `com.metrobuilder.model.dao` (create the `dao` subdirectory):

```java
package com.metrobuilder.model.dao;

import com.metrobuilder.db.DatabaseManager;
import com.metrobuilder.model.PlayerProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PlayerProfileDao {

    /** Ensures the single row exists, then loads and returns it. */
    public PlayerProfile load() {
        try (Connection conn = DatabaseManager.getDataSource().getConnection()) {
            // Guarantee the singleton row
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT OR IGNORE INTO player_profile (id, username, total_playtime_seconds) VALUES (1, 'Player', 0)")) {
                ins.executeUpdate();
            }
            try (PreparedStatement sel = conn.prepareStatement(
                    "SELECT username, total_playtime_seconds FROM player_profile WHERE id = 1");
                 ResultSet rs = sel.executeQuery()) {
                if (rs.next()) {
                    return new PlayerProfile(rs.getString("username"), rs.getLong("total_playtime_seconds"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load player profile", e);
        }
        return new PlayerProfile("Player", 0L); // fallback (should never happen)
    }

    /** Persists the current state of the profile back to the single row. */
    public void save(PlayerProfile profile) {
        try (Connection conn = DatabaseManager.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE player_profile SET username = ?, total_playtime_seconds = ? WHERE id = 1")) {
            ps.setString(1, profile.getUsername());
            ps.setLong(2, profile.getTotalPlaytimeSeconds());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save player profile", e);
        }
    }
}
```
  </action>
  <verify>
    <automated>mvn compile -q 2>&amp;1 | grep -E "ERROR|error:" || echo "COMPILE OK"</automated>
  </verify>
  <done>All four files exist, compile without errors. `DatabaseManager`, `PlayerProfile`, and `PlayerProfileDao` are in their correct packages.</done>
</task>

<task type="auto">
  <name>Task 2: Lifecycle wiring in Main.java (session start + playtime save on stop)</name>
  <files>src/main/java/com/metrobuilder/app/Main.java</files>
  <action>
Rewrite `Main.java` to:
1. Call `DatabaseManager.initialize()` in `init()` (runs on launcher thread before JavaFX starts).
2. Load `PlayerProfile` via `PlayerProfileDao` in `init()` and store as a field.
3. Record `Instant sessionStart = Instant.now()` in `init()`.
4. Pass `profile` to `MainController` via `FXMLLoader.getController()` after `loader.load()` in `start()`.
5. In `stop()`: compute elapsed seconds, add to `profile.getTotalPlaytimeSeconds()`, call `dao.save(profile)`, then `DatabaseManager.shutdown()`.

```java
package com.metrobuilder.app;

import com.metrobuilder.controller.MainController;
import com.metrobuilder.db.DatabaseManager;
import com.metrobuilder.model.PlayerProfile;
import com.metrobuilder.model.dao.PlayerProfileDao;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.time.Instant;

public class Main extends Application {

    private PlayerProfile profile;
    private PlayerProfileDao dao;
    private Instant sessionStart;

    @Override
    public void init() {
        DatabaseManager.initialize();
        dao = new PlayerProfileDao();
        profile = dao.load();
        sessionStart = Instant.now();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlLocation = getClass().getResource("/fxml/main.fxml");
        if (fxmlLocation == null) {
            System.err.println("Kritischer Fehler: main.fxml wurde nicht gefunden!");
            System.exit(1);
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setProfile(profile);

        primaryStage.setTitle("Metro Builder");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    @Override
    public void stop() {
        long elapsed = Instant.now().getEpochSecond() - sessionStart.getEpochSecond();
        profile.setTotalPlaytimeSeconds(profile.getTotalPlaytimeSeconds() + elapsed);
        dao.save(profile);
        DatabaseManager.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

Note: `controller.setProfile(profile)` must be called AFTER `loader.load()` (not before) because `initialize()` runs inside `load()`. `setProfile` will do the binding — see Task 3.
  </action>
  <verify>
    <automated>mvn compile -q 2>&amp;1 | grep -E "ERROR|error:" || echo "COMPILE OK"</automated>
  </verify>
  <done>`Main.java` compiles with `init()`, `start()`, and `stop()` overrides. Profile is loaded before the scene opens and saved with accumulated playtime on close.</done>
</task>

<task type="auto">
  <name>Task 3: Controller binding + FXML UI (username TextField + playtime Label)</name>
  <files>
    src/main/java/com/metrobuilder/controller/MainController.java,
    src/main/resources/fxml/main.fxml
  </files>
  <action>
**MainController.java** — Add `setProfile(PlayerProfile)` method that creates the bidirectional binding and updates the playtime label. Keep the controller thin — no logic, only binding:

```java
package com.metrobuilder.controller;

import com.metrobuilder.model.PlayerProfile;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import com.metrobuilder.model.Station;

public class MainController {

    @FXML private Label statusLabel;
    @FXML private Button buildButton;
    @FXML private TextField usernameField;
    @FXML private Label playtimeLabel;

    private Station currentStation;

    @FXML
    public void initialize() {
        statusLabel.setText("System bereit. Keine Station ausgewählt.");
        // Profile binding happens in setProfile(), called by Main after FXML load
    }

    /** Called by Main.java after FXMLLoader.load() with the loaded PlayerProfile. */
    public void setProfile(PlayerProfile profile) {
        // Bidirectional: TextField <-> profile.username
        usernameField.textProperty().bindBidirectional(profile.usernameProperty());

        // Format total_playtime_seconds as HH:MM:SS using a string binding
        playtimeLabel.textProperty().bind(
            Bindings.createStringBinding(() -> {
                long total = profile.getTotalPlaytimeSeconds();
                long hours = total / 3600;
                long minutes = (total % 3600) / 60;
                long seconds = total % 60;
                return String.format("Spielzeit: %02d:%02d:%02d", hours, minutes, seconds);
            }, profile.totalPlaytimeSecondsProperty())
        );
    }

    @FXML
    private void onBuildClicked() {
        statusLabel.setText("Baue Metro-Linie...");
    }
}
```

**main.fxml** — Replace the existing VBox content to add the username row and playtime label. Import TextField. Keep the existing statusLabel and buildButton:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.layout.VBox?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.TextField?>

<VBox xmlns="http://javafx.com/javafx/21" xmlns:fx="http://javafx.com/fxml/1"
      fx:controller="com.metrobuilder.controller.MainController"
      alignment="CENTER" spacing="20">

    <HBox alignment="CENTER" spacing="10">
        <Label text="Spielername:" />
        <TextField fx:id="usernameField" promptText="Spielername" prefWidth="200" />
    </HBox>

    <Label fx:id="playtimeLabel" text="Spielzeit: 00:00:00" />

    <Label fx:id="statusLabel" text="Lädt..." style="-fx-font-size: 16px; -fx-font-weight: bold;"/>
    <Button fx:id="buildButton" text="Metro-Linie bauen" onAction="#onBuildClicked" />

</VBox>
```
  </action>
  <verify>
    <automated>mvn compile -q 2>&amp;1 | grep -E "ERROR|error:" || echo "COMPILE OK"</automated>
  </verify>
  <done>App compiles and runs (`mvn javafx:run`). Username TextField is visible and pre-populated from DB. Editing the name updates live. Playtime label shows accumulated HH:MM:SS. After closing and reopening, playtime has increased and username is retained.</done>
</task>

</tasks>

<verification>
After all three tasks complete, run the full feature verification:

1. `mvn compile` — must produce zero errors.
2. `mvn javafx:run` — app window opens, username TextField shows "Player", playtime shows "Spielzeit: 00:00:00".
3. Change the username to "TestUser" in the TextField.
4. Close the app.
5. Reopen with `mvn javafx:run` — username field shows "TestUser", playtime shows a non-zero value (e.g., "Spielzeit: 00:00:05").
6. Check `metro-builder.db` exists in the working directory and contains the `player_profile` table with one row: `sqlite3 metro-builder.db "SELECT * FROM player_profile;"`.
</verification>

<success_criteria>
- `player_profile` table exists in `metro-builder.db` with exactly one row (`id=1`).
- Username TextField is bidirectionally bound: UI edits are reflected in `PlayerProfile.usernameProperty()` and persisted to DB on app close.
- Playtime label displays formatted HH:MM:SS derived from `totalPlaytimeSecondsProperty`.
- Each app session adds its elapsed seconds to `total_playtime_seconds` in the DB.
- `DatabaseManager` handles HikariCP pool lifecycle (initialize on start, shutdown on stop).
- Strict MVC respected: no business logic in controller; controller only binds and delegates.
</success_criteria>

<output>
After completion, create `.planning/quick/260423-byf-add-player-profile-table-with-editable-u/260423-byf-SUMMARY.md`
</output>
