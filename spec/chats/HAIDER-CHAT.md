# HAIDER-CHAT.md

**Zweck:** Diese Datei dient als fortlaufendes Gedächtnis (Context) für den KI-Assistenten über alle Chat-Sitzungen hinweg. 
**Anweisung an die KI:** Lies diese Datei **IMMER ZU BEGINN** jeder neuen Chat-Sitzung, um den aktuellen Stand des Projekts zu verstehen. Aktualisiere diese Datei kontinuierlich mit wichtigen neuen Erkenntnissen, Entscheidungen, Fortschritten und dem aktuellen Fokus.

---

## Projekt: Afterfall (U-Bahn Management & Aufbauspiel)

### Aktueller Status
* **Letztes Update:** 08. Mai 2026
* **Branch:** `frontend/core-implementation`
* **Abgabe:** 26. Mai 2026 | **Präsentation:** 28. Mai 2026

---

### Zuletzt implementiert (diese Session)

#### Fundament & Epic 2 – Infrastruktur-Bau (vollständig)

**Model-Layer** (`model/`)
- `GameWorld`, `Station`, `Track`, `Train`, `TrainType` (enum), `Route`, `Passenger`, `Economy`, `Satisfaction`, `SaveGame`
- Alle Properties via JavaFX `DoubleProperty`, `BooleanProperty`, `ObservableList`

**Persistence-Layer** (`persistence/`)
- `DatabaseManager` – SQLite-Connection + Schema-Init (alle 8 Tabellen per `CREATE TABLE IF NOT EXISTS`)
- DAOs: `SaveGameDao`, `StationDao`, `TrackDao`, `RouteDao`, `TrainDao`, `EconomyDao`
- SQLite-Dep in `pom.xml` ergänzt: `org.xerial:sqlite-jdbc:3.47.0.0`

**Simulation-Layer** (`simulation/`)
- `GameLoop` – `AnimationTimer`-basiert, `togglePause()`, delta-time
- `PassengerSimulation` – Spawn, BFS-Wegfindung, Zug-Bewegung, Boarding, Revenue bei Ankunft
- `PathFinder` – BFS über aktive Route-Stops
- `EconomyEngine` – Betriebskosten-Tick (km × opCostPerKm × delta)
- `SatisfactionEngine` – Konvergiert zu Zielwert basierend auf Anbindung, Wartezeit, Ticketpreis

**View-Layer** (`view/`)
- `GameView extends Canvas` – rendert Strecken, Routen (farbig), Stationen, Züge
- Kamera: Rechtsklick+Drag, Scroll = Zoom
- `toWorldX/Y()`, `findStationAt()`, `setHighlightStation()`

**Controller** (`controller/`)
- `GameController` – MVC-Haupt-Controller
- `BuildMode` enum: `NONE`, `BUILD_STATION`, `BUILD_TRACK`, `BUILD_ROUTE`, `BUY_TRAIN`
- KeyEvents: SPACE (Pause), ESC (Abbrechen), S/T (Baumodi), Strg+S (Speichern)

**FXML** (`view/game.fxml`)
- `BorderPane`: HUD-Toolbar (top) + Canvas-StackPane (center) + TabPane-Sidebar (right)
- Sidebar Tab "Routen" + Tab "Züge"

**Tutorial-Start**
- 2 Stationen (`Hauptbahnhof` / `Stadtzentrum`) + 1 kostenloser Standardzug beim Start

---

### Letzte Verbesserungen

1. **Ticketpreis-Slider** – 50-Cent-Schritte (`snapToTicks`, `majorTickUnit=0.5`, `blockIncrement=0.5`), Range 0.50–5.00 €, Label "Ticketpreis pro Halt:", dynamischer Wert-Label daneben (`1.50 €` etc.)

2. **Route bearbeiten** – Route in Sidebar auswählen → "✏ Bearbeiten" klicken → BUILD_ROUTE-Modus mit bestehender Route aktiv → weitere Stationen anklicken erweitert die Route → ESC beendet. Status-Label zeigt aktiven Modus an.

3. **Züge-Tab (eigenständig)** – Eigene `ListView<Train>` zeigt alle Züge mit Typ + zugewiesener Route (farbig). Aktionen:
   - "Route zuweisen" → ChoiceDialog
   - "Route entfernen" → Zug bleibt erhalten, Route = null
   - "Zug verkaufen" → 50% Rückgabe, Zug wird entfernt

4. **Fix Route löschen** – Züge werden NICHT mehr mitgelöscht. `route = null` gesetzt, Zug bleibt in `world.getTrains()`.

---

### Bekannte offene Punkte / Bugs

- `game.fxml` hatte ein übrig gebliebenes `</VBox>`-Tag (Zeile 90) → wurde gefixt
- Zug-Bewegung ist vereinfacht (kein echtes Boarding-Tracking per Zug, vereinfachte Revenue-Berechnung)
- DELETE-Taste noch nicht implementiert (Selektionssystem fehlt)
- Speichern (Strg+S) persistiert noch keine Routen/Züge vollständig nach erneutem Laden
- `PassengerSimulation.boardAndAlightPassengers()` vereinfacht – kein reales Kapazitätslimit pro Zug
- Kein Hauptmenü / SaveGame-Ladescreen (wurde bewusst auf später verschoben)

---

### Nächste Schritte (Priorität)

1. **Bug-Fix / Verbesserungen letzte Änderungen**
   - Zug-Bewegung stabiler machen (Bounce an Routen-Enden)
   - Train-ListView auto-refresh wenn Route sich ändert (ObservableList-Listener statt manuell `.refresh()`)
   - Route-Bearbeiten: Highlight/Feedback welche Stationen schon in der Route sind
   - Speichern/Laden vollständig implementieren (Routen, Züge, Route-Stops)

2. **Epic 3 – Zugmanagement verfeinern**
   - Kapazitätsanzeige pro Zug in der Liste
   - Zug-Status (fährt / wartet / inaktiv) sichtbar machen

3. **Epic 5 – Wirtschafts-UI**
   - Einnahmen/Kosten-Übersicht (aktueller Tick)
   - Warnung bei negativem Kontostand

4. **Epic 1 – Tutorial**
   - Tutorial-Dialog beim ersten Start
   - Aufgabe: Stationen verbinden → Abschluss-Feedback

5. **Epic 6 – Stadtwachstum**
   - Neue Stationen spawnen automatisch über Zeit

6. **Epic 7 – Ranking-Client**
   - `RankingClient` (java.net.http.HttpClient), `RankingView`

---

### Wichtige Design-Entscheidungen & Kontext

- **Genre:** Pausierbares Endlos-Aufbauspiel (kein Game Over)
- **Bau-System:** Freies Platzieren, keine Kollisionsabfrage, keine Signale
- **Wirtschaft:** Einnahmen = Passagier × ticketPrice × Stops; Kosten = opCostPerKm × km × delta
- **Passagiere:** BFS-Wegfindung, Umsteigen möglich; Züge haben Kapazitätslimit, Stationen unbegrenzt
- **MVC-Pattern:** Schulpflicht – strikt eingehalten
- **Schulpflicht-Bestandteile (alle implementiert):**
  - ListView ✅ (Routen + Züge + später SaveGames)
  - Property Binding ✅ (balance, satisfaction, netWorth, ticketPrice)
  - EventHandler ✅ (KeyEvents + MouseEvents auf Canvas)
  - JDBC/SQLite ✅
- **Tech-Stack:** Maven Multi-Module, JavaFX 23, SQLite via JDBC, Java 21
- **Paketstruktur:** `at.htl.afterfall.{model,view,controller,persistence,simulation,util}`
- **Keine Gradle** – immer Maven (`pom.xml`)
