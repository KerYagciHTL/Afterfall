# HAIDER-CHAT.md

**Zweck:** Diese Datei dient als fortlaufendes Gedächtnis (Context) für den KI-Assistenten über alle Chat-Sitzungen hinweg. 
**Anweisung an die KI:** Lies diese Datei **IMMER ZU BEGINN** jeder neuen Chat-Sitzung, um den aktuellen Stand des Projekts zu verstehen. Aktualisiere diese Datei kontinuierlich mit wichtigen neuen Erkenntnissen, Entscheidungen, Fortschritten und dem aktuellen Fokus.

---

## Projekt: Afterfall (U-Bahn Management & Aufbauspiel)

### Aktueller Status
* **Letztes Update:** 19. Mai 2026
* **Branch:** `frontend/core-implementation`
* **Abgabe:** 26. Mai 2026 | **Präsentation:** 28. Mai 2026

---

### Zuletzt implementiert (diese Session)

#### Modernes UI-Redesign, €/s Anzeige & Performance

**Zuletzt**: 19. Mai 2026 – Session 2

#### Session 1 – UX-Overhaul & Feature-Ergänzungen

**Fullscreen / Fenster**
- `MainApp`: `stage.setMaximized(true)` → Spiel startet maximiert

**HUD vergrößert** (`game.fxml`)
- Toolbar: Padding `10 14 10 14`, alle Key-Labels auf 16–17px hochgesetzt
- Unterer Border für klare Trennung Canvas/HUD
- "Zuf:" → "Zufrieden.:" für bessere Lesbarkeit

**Ticketpreis 50-Cent-Snap** (doppelt abgesichert)
- FXML: `snapToTicks=true`, `majorTickUnit=0.5`, `blockIncrement=0.5`
- Controller: `Platform.runLater`-Listener rundet auf nächste 0.5 als Sicherheitsnetz

**Währungsformatierung** (`GameController.formatCurrency()`)
- ≥1.000 → `1.5k €`, ≥1.000.000 → `1.5 Mio €`
- `balanceLabel` + `netWorthLabel` nutzen `Bindings.createStringBinding` mit formatCurrency

**Kreis-Routen** (`Route.isCircular`, GameController, GameView, PassengerSimulation)
- Station darf nur einmal in einer Route sein
- Ausnahme: erste Station nochmal anklicken (bei ≥2 Stops) → `route.setCircular(true)`
- GameView: Kreis-Schluss-Segment als gestrichelte Linie (last→first)
- PassengerSimulation: circular → wrap statt bounce
- Jeder 2. Zug auf Kreis-Route startet in Gegenrichtung (`isForward=false`, `currentStopIndex=stops.size()-1`)

**Züge ohne Route kaufen**
- `onBuyTrain()` → öffnet In-Game-Shop-Overlay (3 Karten: Standard / Medium / Super)
- Kein Route-Pflichtfeld mehr beim Kauf
- Hover-Effekt auf Karten; disabled-State wenn kein Guthaben
- ESC schließt Shop-Overlay

**Sidebar überarbeitet** (`game.fxml`)
- Breite: 210 → 265px
- Routen-Tab: Zelle zeigt Stops (├/└ Baum) + zugewiesene Züge (mit Richtungspfeil) + ↺-Icon bei Kreis
- Züge-Tab: Zelle zeigt Typ-Icon + Kapazität + Route (orange ⚠ wenn keine Route)
- Buttons gruppiert (Ein/Aus + Löschen in einer HBox)

**Toast-System** (ersetzt alle `Alert`-Popups)
- Rot (`#c62828`) für Fehler, Blau (`#0d6efd`) für Info
- Erscheint unten rechts über dem Canvas, verblasst nach 3s (PauseTransition + FadeTransition)
- Spacer-Trick: VBox mit Priority.ALWAYS-Region → Toasts erscheinen immer unten

**BUILD_ROUTE Highlight** (`GameView`)
- Stationen die bereits in aktiver Route sind → hellgrün
- Erste Station der Route → türkis (anklickbar zum Kreis schließen)

---

#### Session 2 – Performance, €/s, UI-Redesign (Claude Design)

**€/Sekunde Anzeige** (`Economy.incomeRateProperty`, `GameLoop`, `GameController`)
- EMA (exponential moving average, 2s-Konstante) aus GameLoop für smoothen Wert
- Grün `#43d494` für Gewinn, Rot `#f05454` für Verlust, Grau bei ≈0
- UI-Update throttled auf 2×/s via `lastRateUiUpdate` Timestamp

**Performance-Optimierungen**
- GameLoop: `MAX_DELTA = 0.1s` cap → verhindert Simulation-Sprünge nach Lag-Spikes
- SatisfactionEngine: Berechnung jetzt 2×/s statt 60×/s (`INTERVAL = 0.5`) → größtes CPU-Einsparung
- SatisfactionEngine: `stream()` durch for-Schleifen ersetzt (weniger GC-Druck)
- GameView: Font-Cache `Map<Integer, Font>` → kein `Font.font()` pro Frame/Station

**GameView visuell** (`GameView.java`)
- Dot-Grid Hintergrund (Screen-Space, folgt Kamera mit Parallax) 
- Route-Linien: Glow-Layer (breiter, halbtransparent) + Hauptlinie → 3D-Tiefe
- Station-Glow für highlighted/in-route Stationen
- Richtungs-Punkt auf Zügen (kleiner Kreis in Fahrtrichtung)
- Canvas-Farbe `#0c0d18`, Dot-Grid-Farbe `#16182a`
- `StrokeLineCap.ROUND` + `StrokeLineJoin.ROUND` → glattere Linien-Ecken

**Claude Design System** (`game.css` NEU + `game.fxml` Redesign)
- Vollständige CSS-Datei: `src/main/resources/at/htl/afterfall/view/game.css`
- Toolbar zu HBox mit „Stat Chips" (VBox-Karten pro Metrik, CSS-Klasse `.stat-chip`)
- Sidebar: CSS-gestylter TabPane (`.sidebar-tab-pane`), animated hover, selected-underline
- Buttons: `.btn-primary` (Indigo), `.btn-success` (Grün), `.btn-neutral` (Dunkel), `.btn-danger` (Rot)
- ToggleButtons: `.toggle-build` mit `:selected` → Indigo-Highlight
- Slider: blauer Thumb mit Glow-Effekt
- ListView: transparenter Hintergrund, subtile Selektion
- Scrollbar: minimalistisch, Indigo thumb on hover
- `editStatusLabel`: conditional visible/managed via `setStatus()`

---

### Bekannte offene Punkte / Bugs

- DELETE-Taste noch nicht implementiert (Selektion fehlt)
- Speichern (Strg+S) persistiert noch keine Routen/Züge vollständig nach erneutem Laden
- `PassengerSimulation.boardAndAlightPassengers()` vereinfacht – kein reales Kapazitätslimit pro Zug
- Kein Hauptmenü / SaveGame-Ladescreen (bewusst auf später verschoben)
- ChoiceDialog für Route-Zuweisung noch System-Style (nicht custom)

---

### Nächste Schritte (Priorität)

1. **Stadtwachstum** – neue Stationen spawnen automatisch über Zeit (Epic 6)
2. **Speichern/Laden** – Routen, Züge, Route-Stops vollständig persistieren
3. **Ranking-Client** – `RankingClient` (java.net.http.HttpClient), `RankingView` (Epic 7)
4. **Tutorial-Dialog** beim ersten Start (Epic 1)
5. **DELETE-Taste** – Station/Strecke selektieren und löschen

---

### Wichtige Design-Entscheidungen & Kontext

- **Genre:** Pausierbares Endlos-Aufbauspiel (kein Game Over)
- **Bau-System:** Freies Platzieren, keine Kollisionsabfrage, keine Signale
- **Wirtschaft:** Einnahmen = Passagier × ticketPrice × Stops; Kosten = opCostPerKm × km × delta
- **Passagiere:** BFS-Wegfindung, Umsteigen möglich; Züge haben Kapazitätslimit, Stationen unbegrenzt
- **MVC-Pattern:** Schulpflicht – strikt eingehalten
- **Schulpflicht-Bestandteile (alle implementiert):**
  - ListView ✅ (Routen + Züge)
  - Property Binding ✅ (balance, satisfaction, netWorth, ticketPrice)
  - EventHandler ✅ (KeyEvents + MouseEvents auf Canvas)
  - JDBC/SQLite ✅
- **Tech-Stack:** Maven Multi-Module, JavaFX 23, SQLite via JDBC, Java 21
- **Paketstruktur:** `at.htl.afterfall.{model,view,controller,persistence,simulation,util}`
- **Keine Gradle** – immer Maven (`pom.xml`)
