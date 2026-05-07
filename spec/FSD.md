# Functional Specification Document – Afterfall

**Kerimcan Yagci, Nico Haider, Fridolino Dürk**  
Version 1.0.0 · 2026-05-07

---

## Inhaltsverzeichnis

1. [Systemarchitektur](#1-systemarchitektur)
2. [Domänenmodell](#2-domänenmodell)
3. [Datenbankschema](#3-datenbankschema)
4. [JavaFX-Pflichtbestandteile](#4-javafx-pflichtbestandteile)
5. [Spielschleife & Simulation](#5-spielschleife--simulation)
6. [Ranking-Backend API](#6-ranking-backend-api)
7. [Screen-Flow](#7-screen-flow)

---

## 1. Systemarchitektur

Das Projekt folgt dem **MVC-Pattern** als Schulpflichtanforderung.

```
┌─────────────────────────────────────────────────────────┐
│                      JavaFX App                         │
│                                                         │
│  ┌──────────┐    observiert    ┌────────────────────┐   │
│  │  Model   │◄─────────────── │    Controller      │   │
│  │          │                  │                    │   │
│  │ GameWorld│  Property-Events │ GameController     │   │
│  │ Station  │ ───────────────► │ SaveGameController │   │
│  │ Track    │                  │ RankingController  │   │
│  │ Train    │                  └────────┬───────────┘   │
│  │ Route    │                           │ aktualisiert  │
│  │ Economy  │                  ┌────────▼───────────┐   │
│  │ ...      │                  │       View         │   │
│  └────┬─────┘                  │                    │   │
│       │                        │ GameView (Canvas)  │   │
│       │ JDBC                   │ SaveGameView       │   │
│       ▼                        │ RankingView        │   │
│  ┌──────────┐                  └────────────────────┘   │
│  │ SQLite   │                                           │
│  │ .db File │                                           │
│  └──────────┘                                           │
└─────────────────────────────────────────────────────────┘
                              │ HTTP (JSON)
                              ▼
                   ┌──────────────────────┐
                   │  Ranking Backend     │
                   │  (Docker, REST API)  │
                   └──────────────────────┘
```

### Paketstruktur

```
src/main/java/at/htl/afterfall/
  model/
    GameWorld.java          // Wurzel aller Entities
    Station.java
    Track.java
    Train.java              // enum TrainType { STANDARD, MEDIUM, SUPER }
    Route.java
    Passenger.java
    Economy.java
    Satisfaction.java
    SaveGame.java           // Metadaten eines Spielstands
  view/
    GameView.java           // JavaFX Canvas – Kartenrendering
    SaveGameView.java       // ListView der Spielstände
    RankingView.java
    HudView.java            // Kontostand, Zufriedenheit, Net Worth
  controller/
    GameController.java     // Hauptlogik, Maus-/Tastaturevents
    SaveGameController.java // Speichern, Laden, Löschen
    RankingController.java  // HTTP-Calls zum Backend
  persistence/
    DatabaseManager.java    // Connection-Pool, Schema-Init
    StationDao.java
    TrackDao.java
    TrainDao.java
    RouteDao.java
    EconomyDao.java
    SaveGameDao.java
  simulation/
    GameLoop.java           // AnimationTimer-basierte Spielschleife
    PassengerSimulation.java
    PathFinder.java         // BFS über Station-Graph
    SatisfactionEngine.java
    EconomyEngine.java
  ranking/
    RankingClient.java      // HTTP-Client (java.net.http.HttpClient)
    RankingEntry.java       // DTO
  util/
    ColorGenerator.java     // Zufällige Linienfarben (HSB-gefiltert)
```

---

## 2. Domänenmodell

### Klassenübersicht

```
GameWorld
 ├── List<Station>
 ├── List<Track>
 ├── List<Train>
 ├── List<Route>
 ├── List<Passenger>
 ├── Economy
 └── Satisfaction

Station
 ├── id: int
 ├── name: String
 ├── x: DoubleProperty
 ├── y: DoubleProperty
 └── waitingPassengers: ObservableList<Passenger>

Track
 ├── id: int
 ├── from: Station
 ├── to: Station
 └── length: double          // Euklidische Distanz in Spieleinheiten

Train
 ├── id: int
 ├── type: TrainType
 ├── route: Route
 ├── active: BooleanProperty
 └── position: double        // 0.0–1.0 entlang der aktuellen Strecke

TrainType (enum)
 ├── STANDARD  capacity=50   speed=1.0  buyCost=5000   opCostPerKm=0.10
 ├── MEDIUM    capacity=120  speed=1.0  buyCost=12000  opCostPerKm=0.20
 └── SUPER     capacity=300  speed=1.3  buyCost=30000  opCostPerKm=0.40

Route
 ├── id: int
 ├── stops: List<Station>    // geordnet
 ├── color: Color
 ├── active: BooleanProperty
 └── trains: List<Train>

Passenger
 ├── id: int
 ├── origin: Station
 ├── destination: Station
 └── path: List<Station>     // berechnet via PathFinder

Economy
 ├── balance: DoubleProperty
 ├── netWorth: DoubleProperty
 └── ticketPricePerStop: DoubleProperty

Satisfaction
 └── value: DoubleProperty   // 0.0–100.0
```

### TrainType-Werte (Übersicht)

| Typ | Kapazität | Speed-Faktor | Kaufpreis | Betrieb / km |
|---|---|---|---|---|
| STANDARD | 50 | 1,0x | 5.000 | 0,10 |
| MEDIUM | 120 | 1,0x | 12.000 | 0,20 |
| SUPER | 300 | 1,3x | 30.000 | 0,40 |

### Linienfarben-Algorithmus

```java
// ColorGenerator.java
Color generateRouteColor() {
    Random rng = new Random();
    Color c;
    do {
        double hue = rng.nextDouble() * 360;
        double sat = 0.5 + rng.nextDouble() * 0.5;   // 0.50–1.00
        double bri = 0.4 + rng.nextDouble() * 0.45;  // 0.40–0.85
        c = Color.hsb(hue, sat, bri);
    } while (alreadyUsed(c));
    return c;
}
```

---

## 3. Datenbankschema

Eine SQLite-Datei pro Installation (`afterfall.db`). Alle Spielstände liegen in derselben Datei, getrennt via `save_id`.

```sql
CREATE TABLE IF NOT EXISTS save_games (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    name         TEXT    NOT NULL,
    created_at   TEXT    NOT NULL,  -- ISO-8601
    last_saved   TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS stations (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    save_id INTEGER NOT NULL REFERENCES save_games(id) ON DELETE CASCADE,
    name    TEXT    NOT NULL,
    x       REAL    NOT NULL,
    y       REAL    NOT NULL
);

CREATE TABLE IF NOT EXISTS tracks (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    save_id      INTEGER NOT NULL REFERENCES save_games(id) ON DELETE CASCADE,
    from_station INTEGER NOT NULL REFERENCES stations(id),
    to_station   INTEGER NOT NULL REFERENCES stations(id),
    length       REAL    NOT NULL
);

CREATE TABLE IF NOT EXISTS routes (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    save_id   INTEGER NOT NULL REFERENCES save_games(id) ON DELETE CASCADE,
    color_hex TEXT    NOT NULL,  -- z.B. "#E53935"
    active    INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS route_stops (
    route_id   INTEGER NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    station_id INTEGER NOT NULL REFERENCES stations(id),
    position   INTEGER NOT NULL,  -- Reihenfolge innerhalb der Route
    PRIMARY KEY (route_id, position)
);

CREATE TABLE IF NOT EXISTS trains (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    save_id  INTEGER NOT NULL REFERENCES save_games(id) ON DELETE CASCADE,
    type     TEXT    NOT NULL,  -- 'STANDARD' | 'MEDIUM' | 'SUPER'
    route_id INTEGER REFERENCES routes(id),
    active   INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS economy (
    save_id          INTEGER PRIMARY KEY REFERENCES save_games(id) ON DELETE CASCADE,
    balance          REAL    NOT NULL DEFAULT 10000,
    net_worth        REAL    NOT NULL DEFAULT 10000,
    ticket_price     REAL    NOT NULL DEFAULT 1.5
);

CREATE TABLE IF NOT EXISTS satisfaction (
    save_id INTEGER PRIMARY KEY REFERENCES save_games(id) ON DELETE CASCADE,
    value   REAL    NOT NULL DEFAULT 50.0
);
```

### DAO-Muster

Jedes DAO erhält eine `Connection` via `DatabaseManager.getConnection()` und implementiert mindestens `findAll(int saveId)`, `save(Entity)`, `delete(int id)`.

```java
// Beispiel
public class StationDao {
    public List<Station> findAll(int saveId) { ... }
    public int insert(int saveId, Station s)  { ... }
    public void delete(int stationId)         { ... }
}
```

---

## 4. JavaFX-Pflichtbestandteile

### ListView — Spielstand-Auswahl

```java
ListView<SaveGame> saveList = new ListView<>();
saveList.setItems(FXCollections.observableArrayList(saveGameDao.findAll()));

// Sortieren (nach last_saved, absteigend)
saveList.getItems().sort(Comparator.comparing(SaveGame::getLastSaved).reversed());

// Filtern
FilteredList<SaveGame> filtered = new FilteredList<>(master);
searchField.textProperty().addListener((obs, old, val) ->
    filtered.setPredicate(sg -> sg.getName().toLowerCase().contains(val.toLowerCase()))
);

// Selektion
saveList.getSelectionModel().selectedItemProperty().addListener(
    (obs, old, sg) -> loadGame(sg)
);
```

### Property Binding

```java
// Unidirektional — HUD-Labels
balanceLabel.textProperty().bind(
    economy.balanceProperty().asString("%.2f €")
);
satisfactionBar.progressProperty().bind(
    satisfaction.valueProperty().divide(100.0)
);
netWorthLabel.textProperty().bind(
    economy.netWorthProperty().asString("%.0f")
);

// Bidirektional — Ticketpreis-Slider
ticketSlider.valueProperty().bindBidirectional(
    economy.ticketPricePerStopProperty()
);
```

### EventHandler / KeyEvents

```java
// GameController
scene.setOnKeyPressed(e -> {
    switch (e.getCode()) {
        case SPACE  -> gameLoop.togglePause();
        case ESCAPE -> cancelBuildMode();
        case DELETE -> deleteSelected();
        case S      -> { if (e.isControlDown()) saveGame(); }
    }
});

// Maus — Stationsplatzierung
canvas.setOnMouseClicked(e -> {
    if (buildMode == BUILD_STATION) placeStation(e.getX(), e.getY());
    else if (buildMode == BUILD_TRACK) selectStationForTrack(e.getX(), e.getY());
});
```

---

## 5. Spielschleife & Simulation

### GameLoop

```java
// Läuft im JavaFX Application Thread via AnimationTimer
public class GameLoop extends AnimationTimer {
    private static final double TICK_SECONDS = 1.0 / 60.0;
    private long lastTime = 0;

    @Override
    public void handle(long now) {
        if (paused) return;
        double delta = (now - lastTime) / 1_000_000_000.0;
        lastTime = now;

        passengerSim.tick(delta);
        economyEngine.tick(delta);
        satisfactionEngine.tick(delta);
        gameView.render();
    }
}
```

### Passagier-Pathfinding (BFS)

- Graph: Knoten = `Station`, Kanten = aktive `Route`-Verbindungen zwischen aufeinanderfolgenden Stops
- Gewicht: Anzahl Stationen (nicht km) → kürzester Weg = wenigste Umsteiger
- Pfad wird beim Entstehen des Passagiers einmalig berechnet; bei Netzänderung neu berechnet

### Wirtschaftstick

```
pro Tick (delta Sekunden):
  Einnahmen  += Σ(beförderte Passagiere) × ticketPrice × gefahrene_stops
  Betriebskosten += Σ(aktive Züge) × opCostPerKm × zurückgelegte_km × delta
  balance    = balance + Einnahmen - Betriebskosten
  netWorth   = netWorth + Einnahmen - Betriebskosten
```

### Zufriedenheits-Engine

Zufriedenheit konvergiert pro Tick in Richtung eines Zielwerts:

```
Zielwert = 50
  + 30  × (angebundene_Orte / gesamt_Orte)
  - 20  × clamp(durchschn_Wartezeit / MAX_WAIT, 0, 1)
  - 15  × clamp((ticketPrice - FAIR_PRICE) / FAIR_PRICE, 0, 1)
  - 15  × (überfüllte_Routen / gesamt_Routen)

satisfaction += (Zielwert - satisfaction) × 0.01 × delta
```

---

## 6. Ranking-Backend API

Dockerisierter REST-Service auf privatem Server. Client: `java.net.http.HttpClient`.

### Endpunkte

#### `GET /ranking`

Gibt die Top-100-Einträge zurück.

**Response `200 OK`:**
```json
[
  { "rank": 1, "playerName": "kxrim", "netWorth": 248300.0 },
  { "rank": 2, "playerName": "fridolino", "netWorth": 195000.0 }
]
```

---

#### `POST /ranking`

Trägt oder aktualisiert den Spieler-Eintrag.

**Request Body:**
```json
{
  "playerName": "kxrim",
  "netWorth": 248300.0,
  "token": "<shared-secret>"
}
```

**Response `200 OK`:**
```json
{ "rank": 1 }
```

**Response `401 Unauthorized`:** Falsches Token.

---

### Fehlerbehandlung Client-seitig

```java
try {
    HttpResponse<String> resp = client.send(request, BodyHandlers.ofString());
    // verarbeiten
} catch (IOException | InterruptedException e) {
    // Offline → RankingView zeigt "Nicht verfügbar" an, Spiel läuft weiter
}
```

---

## 7. Screen-Flow

```
┌─────────────────┐
│   Hauptmenü     │
│ [Neues Spiel]   │──────────────────────────────────┐
│ [Laden]         │──► SaveGame-ListView              │
│ [Rangliste]     │──► RankingView                    │
│ [Beenden]       │                                   │
└─────────────────┘                                   │
                                                      ▼
                                          ┌───────────────────────┐
                                          │      GameView         │
                                          │  ┌─────────────────┐  │
                                          │  │   Karte/Canvas  │  │
                                          │  └─────────────────┘  │
                                          │  HUD: Kontostand,     │
                                          │       Zufriedenheit,  │
                                          │       Net Worth,      │
                                          │       Pause-Status    │
                                          │  Seitenleiste:        │
                                          │       Routen-Liste    │
                                          │       (ListView)      │
                                          └───────────────────────┘
```

### Build-Modi (GameView)

| Modus | Aktivierung | Aktion |
|---|---|---|
| `NONE` | Standard / Escape | Selektion & Kamera |
| `BUILD_STATION` | Button / Hotkey S | Nächster Klick = neue Station |
| `BUILD_TRACK` | Button / Hotkey T | Klick Station A → Klick Station B = neue Strecke |
| `BUY_TRAIN` | Button im Menü | Dialog: Zugtyp wählen + Route zuweisen |
