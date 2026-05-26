# Verbesserungen – Implementierungsanweisungen

---

## 1. Drag-&-Drop: Vorschau und direkte Routenverschiebung

### Ziel
Beim Verschieben eines Route-Segments soll eine visuelle Vorschau angezeigt werden und die gesamte Route direkt angepasst werden – nicht nur die zugrundeliegende Strecke.

### Verschiebungs-Logik
- Der Nutzer klickt auf ein Segment zwischen zwei aufeinanderfolgenden Stops einer Route und zieht es auf eine andere Station.
- Während des Ziehens wird eine **gestrichelte Vorschaulinie** vom ersten angrenzenden Stop über die Zielstation zum zweiten angrenzenden Stop gezeichnet (in der Farbe der Route, leicht transparent).
- Die beiden **angrenzenden Stops** (linker und rechter Nachbar des verschobenen Segments) bleiben erhalten.
- Die **Zielstation** wird zwischen den beiden angrenzenden Stops in die Route **eingefügt** – sie ersetzt nicht einen der bestehenden Stops, sondern wird zwischen ihnen eingeschlossen.
- Beispiel: Route A–B–C, Segment B–C wird auf Station D gezogen → Ergebnis: A–B–D–C.
- Ein Snap-Radius zeigt durch visuelles Hervorheben der Zielstation an, wenn eine Station eingerastet ist (z. B. gelber Glow).
- Beim Loslassen (MouseReleased) wird die Route dauerhaft angepasst. Bei Abbruch (ESC oder außerhalb gültigem Bereich losgelassen) bleibt die Route unverändert.

### Technische Umsetzung
- Die Vorschau wird in `GameView` innerhalb der Render-Schleife gezeichnet – nur wenn ein Drag aktiv ist.
- `GameController` verarbeitet das MouseDragged- und MouseReleased-Event und aktualisiert die Route in `GameWorld`.
- Neue Strecken (Tracks) zwischen den angrenzenden Stops und der Zielstation werden automatisch angelegt, falls noch nicht vorhanden (analog zur bestehenden `buildTrackSilent()`-Logik).
- Züge auf der betroffenen Route werden nach der Änderung auf Stop-Index 0 zurückgesetzt.

---

## 2. Vollbild-Modus immer aktiv

### Ziel
Das Fenster soll sowohl in der Lobby als auch während des Spiels dauerhaft im maximierten Zustand bleiben.

### Umsetzung
- In `MainApp.start()` wird `stage.setMaximized(true)` gesetzt – dies gilt für den initialen Fenster-Start mit der Lobby-Szene.
- Beim Wechsel von der Lobby in das Spiel (`openGame()` in `MainController`) darf `stage.setMaximized(false)` **nicht** gesetzt werden; stattdessen soll `stage.setMaximized(true)` explizit beibehalten oder erneut gesetzt werden.
- Beim Zurückkehren aus dem Spiel in die Lobby (`onMainMenu()` in `GameController`) wird `stage.setMaximized(false)` **entfernt** und durch `stage.setMaximized(true)` ersetzt.
- Der Nutzer soll das Fenster nicht manuell verkleinern können (optional: `stage.setResizable(false)` prüfen, aber Maximized-Mode sollte ausreichen).

---

## 3. Spielstand speichern beim Rückkehr ins Hauptmenü

### Ziel
Wenn der Nutzer im Spiel auf den „Zurück zum Menü"-Button klickt, soll der aktuelle Spielstand automatisch gespeichert werden, bevor die Lobby geladen wird – ohne Rückfrage.

### Umsetzung
- In `GameController.onMainMenu()` wird **vor** dem Stoppen des GameLoops und dem Laden der Lobby-Szene `saveGame()` aufgerufen.
- Das Speichern erfolgt immer, unabhängig davon ob `currentSave` gesetzt ist (falls null: kein Speichern, aber kein Fehler).
- Nach dem Speichern wird der Toast „Spielstand gespeichert." **nicht** angezeigt (da die Szene sofort wechselt) – der `showToast()`-Aufruf in `saveGame()` kann für diesen Fall unterdrückt oder akzeptiert werden.
- Die Reihenfolge in `onMainMenu()`: `saveGame()` → `gameLoop.stop()` → Lobby-Szene laden.

> **Hinweis:** `saveGame()` speichert bereits vollständig (Stationen, Strecken, Routen, Züge, Economy, Satisfaction). Es ist kein zusätzlicher Speicher-Code notwendig.

---

## 4. Rangliste (US 7.1 + US 8.1 Ranglisten-Bereich)

### Kontext & Backend-API

Das Backend läuft als Spring Boot App (`/backend`, Port 8080). Bereits vorhanden:

- `GET /ranking` → JSON-Array: `[{"rank": 1, "playerName": "...", "netWorth": 12345.0}, ...]` (Top 100)
- `POST /ranking` → Body: `{"token": "<TOKEN>", "playerName": "...", "netWorth": 12345.0}` → Response: `{"rank": 3}`

Das Token ist serverseitig als Env-Var `RANKING_TOKEN` gesetzt und muss im Frontend mitgeschickt werden.

---

### Styling-Pflicht

**Alle neuen UI-Elemente müssen dem bestehenden Design-System entsprechen:**
- Hintergrundfarben: `#0c0d18` (Canvas/Fenster), `#151728` (Panels/Karten), `#13141f` (Listen-Zellen)
- Textfarben: `white` für Haupttext, `#78909c` für Sekundärtext
- Akzentfarben: `#3d5af1` (Blau/Primary), `#43d494` (Grün/Positiv), `#f05454` (Rot/Fehler)
- Schrift: `Segoe UI, SF Pro Display, system-ui`
- Rahmen: `#252848` / `#1e2040`, Radius 8–10px
- Buttons: Inline-Style wie bestehende Lobby-Buttons (`-fx-background-color: #1b5e20; -fx-text-fill: white; ...`)
- **Kein abweichendes Styling** – neue Labels/Buttons/ListViews exakt wie vorhandene Elemente in `main.fxml` stylen

---

### Schritt 1: Konfigurationsdateien anlegen

**`frontend/src/main/resources/at/htl/afterfall/config.properties`** (neu, niemals committen mit echtem Token):
```properties
ranking.url=http://localhost:8080/ranking
ranking.token=HIER_DAS_ECHTE_TOKEN
```

**`frontend/src/main/resources/at/htl/afterfall/ranking.properties`** (wird zur Laufzeit beschrieben, leer anlegen):
```properties
player.name=
```

---

### Schritt 2: `RankingClient.java` anlegen

**Pfad:** `frontend/src/main/java/at/htl/afterfall/util/RankingClient.java`

Diese Klasse kapselt die gesamte HTTP-Logik mit Java 11 `HttpClient` (keine externe Bibliothek nötig – HttpClient ist in Java 11+ enthalten).

```java
package at.htl.afterfall.util;

import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.function.Consumer;

public class RankingClient {

    // --- Konfiguration laden ---
    private static final String BASE_URL;
    private static final String TOKEN;
    private static final java.nio.file.Path PLAYER_NAME_FILE;

    static {
        // config.properties aus resources lesen
        try (var in = RankingClient.class.getResourceAsStream("/at/htl/afterfall/config.properties")) {
            var props = new Properties();
            props.load(in);
            BASE_URL = props.getProperty("ranking.url", "http://localhost:8080/ranking");
            TOKEN    = props.getProperty("ranking.token", "");
        } catch (Exception e) { throw new RuntimeException(e); }

        // ranking.properties liegt neben der JAR / im working directory (wird geschrieben)
        PLAYER_NAME_FILE = java.nio.file.Path.of("ranking.properties");
    }

    // --- Spielername laden/speichern ---
    private static String loadPlayerName() {
        try {
            if (java.nio.file.Files.exists(PLAYER_NAME_FILE)) {
                var p = new Properties();
                p.load(java.nio.file.Files.newBufferedReader(PLAYER_NAME_FILE));
                return p.getProperty("player.name", "").strip();
            }
        } catch (Exception ignored) {}
        return "";
    }

    private static void savePlayerName(String name) {
        try {
            var p = new Properties();
            p.setProperty("player.name", name);
            p.store(java.nio.file.Files.newBufferedWriter(PLAYER_NAME_FILE), null);
        } catch (Exception ignored) {}
    }

    // --- Rangliste abrufen (synchron, auf eigenem Thread aufrufen!) ---
    public static List<RankingEntry> fetchRanking() {
        try {
            var client = HttpClient.newHttpClient();
            var req = HttpRequest.newBuilder(URI.create(BASE_URL))
                    .GET().build();
            var res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return parseEntries(res.body());
        } catch (Exception e) {
            return null; // null = nicht erreichbar
        }
    }

    // --- Score einreichen (async, kein UI-Block) ---
    // onSuccess: wird auf JavaFX-Thread mit erreichtem Rang aufgerufen
    // onError:   wird auf JavaFX-Thread aufgerufen wenn nicht erreichbar
    public static void submitScore(double netWorth, Consumer<Integer> onSuccess, Runnable onError) {
        // Spielernamen ermitteln (ggf. Dialog – muss auf FX-Thread!)
        String existing = loadPlayerName();
        if (existing.isBlank()) {
            Platform.runLater(() -> {
                TextInputDialog dlg = new TextInputDialog("");
                dlg.setTitle("Rangliste");
                dlg.setHeaderText("Dein Name für die Rangliste:");
                dlg.showAndWait().ifPresent(name -> {
                    if (!name.isBlank()) {
                        savePlayerName(name.strip());
                        doSubmit(name.strip(), netWorth, onSuccess, onError);
                    }
                });
            });
        } else {
            doSubmit(existing, netWorth, onSuccess, onError);
        }
    }

    private static void doSubmit(String name, double netWorth,
                                  Consumer<Integer> onSuccess, Runnable onError) {
        String body = """
                {"token":"%s","playerName":"%s","netWorth":%s}
                """.formatted(TOKEN, name, netWorth).strip();

        var client = HttpClient.newHttpClient();
        var req = HttpRequest.newBuilder(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
              .thenAccept(res -> {
                  if (res.statusCode() == 200) {
                      int rank = parseRank(res.body());
                      Platform.runLater(() -> onSuccess.accept(rank));
                  } else {
                      Platform.runLater(onError);
                  }
              })
              .exceptionally(ex -> { Platform.runLater(onError); return null; });
    }

    // Minimales JSON-Parsing ohne externe Bibliothek
    private static List<RankingEntry> parseEntries(String json) { /* ... */ }
    private static int parseRank(String json) { /* ... */ }

    public record RankingEntry(int rank, String playerName, double netWorth) {}
}
```

**JSON-Parsing:** Da kein JSON-Parser als Dependency vorhanden ist, entweder:
- **Option A (einfacher):** `javax.json` / `org.json` als Dependency in `frontend/pom.xml` eintragen (z. B. `org.json:json:20240303`)
- **Option B (kein extra Dep):** Einfaches String-Parsing mit `split` und Regex für das simple Format

Empfehlung: **Option A** – eine Zeile `pom.xml`, viel sauberer.

---

### Schritt 3: `GameController.saveGame()` erweitern

In `GameController.java`, in der Methode `saveGame()`, direkt nach `economyDao.save(...)` und **vor** `saveGameDao.updateLastSaved(id)` einfügen:

```java
RankingClient.submitScore(
    world.getEconomy().getNetWorth(),
    rank -> showToast("Rang #" + rank + " in der Rangliste! 🏆", false),
    ()   -> {} // Fehler lautlos – lokales Speichern läuft durch
);
```

Import hinzufügen: `import at.htl.afterfall.util.RankingClient;`

Der HTTP-Call läuft async (`sendAsync`) und blockiert die UI nicht. Beim ersten Aufruf öffnet sich der Spielername-Dialog auf dem FX-Thread.

---

### Schritt 4: `main.fxml` – Ranglisten-Abschnitt einfügen

In `main.fxml` nach dem Titel-Label und **vor** dem "Neues Spiel"-Button einen neuen Abschnitt einfügen:

```xml
<Label text="🏆  Globale Rangliste"
       style="-fx-text-fill: #c8c8d8; -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 16 0 4 0;"/>
<ListView fx:id="rankingListView"
          prefWidth="500" prefHeight="180"
          style="-fx-background-color: transparent; -fx-border-color: #252848; -fx-border-radius: 8; -fx-background-radius: 8;"/>
<Label fx:id="rankingStatusLabel" text="Lade Rangliste..."
       style="-fx-text-fill: #78909c; -fx-font-size: 11; -fx-padding: 0 0 8 0;"/>
```

ListCell-Styling (inline, wie `saveListView`): Zeilenhintergrund `#13141f`, Rang in `#3d5af1` (Blau), Name in `white`, NetWorth in `#43d494` (Grün).

---

### Schritt 5: `MainController.java` erweitern

```java
@FXML private ListView<RankingClient.RankingEntry> rankingListView;
@FXML private Label rankingStatusLabel;
```

In `initialize()` am Ende aufrufen:
```java
loadRanking();
```

Neue Methode:
```java
private void loadRanking() {
    rankingStatusLabel.setText("Lade Rangliste...");
    CompletableFuture.supplyAsync(RankingClient::fetchRanking)
        .thenAcceptAsync(entries -> Platform.runLater(() -> {
            if (entries == null) {
                rankingStatusLabel.setText("Rangliste nicht erreichbar.");
                return;
            }
            rankingStatusLabel.setText("");
            rankingListView.getItems().setAll(entries);
        }));

    rankingListView.setCellFactory(lv -> new ListCell<>() {
        @Override protected void updateItem(RankingClient.RankingEntry e, boolean empty) {
            super.updateItem(e, empty);
            if (empty || e == null) { setGraphic(null); setStyle(""); return; }
            // HBox: Rang (#1) | Name | NetWorth
            // Farben: #3d5af1 für Rang, white für Name, #43d494 für NetWorth
            // Padding: 8 16, Hintergrund #13141f, Radius 6
        }
    });
}
```

Import: `import java.util.concurrent.CompletableFuture;` und `import at.htl.afterfall.util.RankingClient;`

---

### Schritt 6: `frontend/pom.xml` – JSON-Dependency eintragen

```xml
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20240303</version>
</dependency>
```

---

### Verifikation

1. `mvn compile` → kein Fehler
2. Backend starten: `cd backend && RANKING_TOKEN=test mvn spring-boot:run`
3. Frontend starten: `cd frontend && mvn javafx:run`
4. Lobby: Rangliste zeigt "Lade..." → danach Einträge (oder "Nicht erreichbar" wenn Backend aus)
5. Neues Spiel → bauen → Ctrl+S → Spielername-Dialog (einmalig) → Toast mit Rang
6. Zweites Ctrl+S → kein Dialog mehr, sofortiger Submit
7. Lobby → eigener Eintrag in Rangliste sichtbar
