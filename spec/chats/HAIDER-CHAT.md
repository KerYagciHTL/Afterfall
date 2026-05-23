# HAIDER-CHAT.md

**Zweck:** Diese Datei dient als fortlaufendes Gedächtnis (Context) für den KI-Assistenten über alle Chat-Sitzungen hinweg. 
**Anweisung an die KI:** Lies diese Datei **IMMER ZU BEGINN** jeder neuen Chat-Sitzung, um den aktuellen Stand des Projekts zu verstehen. Aktualisiere diese Datei kontinuierlich mit wichtigen neuen Erkenntnissen, Entscheidungen, Fortschritten und dem aktuellen Fokus.

---

## Projekt: Afterfall (U-Bahn Management & Aufbauspiel)

### Aktueller Status
* **Letztes Update:** 23. Mai 2026
* **Branch:** `frontend/core-implementation`
* **Abgabe:** 26. Mai 2026 | **Präsentation:** 28. Mai 2026

---

### Zuletzt implementiert (diese Session)

#### Session 4 – Simulation, Bugfixes, Tutorial & Stories

**Zuletzt**: 23. Mai 2026 – Session 4

**SatisfactionEngine Redesign** (`simulation/SatisfactionEngine.java`)
- `backlogFactor` entfernt (hat gute Netze bestraft)
- Neue Formel: `target = 55 + 25*connFactor + 20*deliveryBonus - 25*waitFactor - 10*emptyFactor`
- `deliveryBonus = onboard/(onboard+waiting)` – belohnt aktiv fahrende Züge
- `MAX_WAIT` 20s → 45s; 90%+ erfordert aktives Management (kurze Wartezeiten + alle Routen besetzt)
- Rate: rise 0.025, fall 0.008 (stabil wenn Netz läuft, kein stetiger Verfall)
- Max erreichbar: 100% (war 80%)

**PassengerSimulation – distanzbasiertes Spawning** (`simulation/PassengerSimulation.java`)
- Ziel-Auswahl: gewichtete Zufallsauswahl statt shuffle – Gewicht = Euklidische Distanz origin→dest (entfernte Stationen attraktiver)
- Spawn-Rate pro Station: `spawnChance = min(avgDistToOthers / 250px, 1.0)` – isolierte Stationen spawnen öfter, dichte Cluster weniger

**Zoom-Fix** (`view/GameView.java`)
- Cursor-Anchor: Weltpunkt unter Maus bleibt beim Zoomen fixiert
- Symmetrischer Faktor: `1.12` / `1/1.12` (war `1.1` / `0.9`, nicht symmetrisch)
- Fallback auf `getDeltaX()` wenn `getDeltaY() == 0` (Trackpad-Kompatibilität)
- Range 0.2–5.0 (war 0.3–3.0)

**CSS-Bugfixes** (`view/game.css`)
- `:hover:!selected` → `:hover` vor `:selected` (JavaFX kennt weder `!selected` noch `:not()`)
- Kaskade nutzen: hover früher definiert, selected überschreibt – betrifft `.toggle-build` und `.sidebar-tab-pane .tab`

**Tutorial-System** (`tutorial/TutorialStep.java`, `tutorial/TutorialManager.java`, `tutorial/TutorialOverlay.java`, `GameController`)
- Neues Package `at.htl.afterfall.tutorial`
- 8 Schritte: Willkommen → Strecke bauen → Route erstellen → Zug zuweisen → Neue Station → Route per Drag umleiten → Zufriedenheit-Erklärung → Los geht's
- Aufgaben-Schritte: 500ms-Timeline prüft Completion-Predicate auf GameWorld → "✓ Erledigt!" + 900ms → auto-advance
- Erklärungs-Schritte: nur "Weiter →" Button
- Overlay: VBox, bottom-left, grüner Rahmen bei Erledigt; `setOnMouseClicked(Event::consume)` → kein Click-Through
- "Überspringen" beendet Tutorial komplett, Timer wird gestoppt

**Station abreißen** (`GameController`)
- Linksklick auf Station im NONE-Modus → `handleStationDemolish()`
- Alert.CONFIRMATION, Info über Anzahl betroffener Strecken
- Entfernt: wartende Passagiere, Station aus allen Routen (Züge auf Index 0), verbundene Strecken + DB-Einträge, Station selbst
- NetWorth sinkt um 8.000€ (`STATION_BUILD_COST`); kein Geld-Erstattung
- US 2.4 damit vollständig ✅

**STORIES.md aktualisiert** (`spec/stories/STORIES.md`)
- 21/24 Stories ✅
- Offen: US 4.2 (Umsteigen), US 6.1 (Stadtwachstum), US 7.1 Frontend, US 8.1+8.2 (neu hinzugefügt)
- Neues Epic 8: Hauptmenü/Lobby – Spielstand laden/neu starten/löschen + Rangliste-Anzeige

---

#### Selektion, Route-Segment-Klick & Zug-Highlight

**Zuletzt**: 23. Mai 2026 – Session 3 (Teil 2)

**Route-ListView Selektion** (`GameController`, `GameView`)
- Klick auf Route in ListView → `gameView.setFocusedRoute(r)` + render
- Fokussierte Route: weiße Outline (13px, alpha 0.38) vor normalen Route-Linien gezeichnet
- Hit-Detection `findRouteSegmentAt` prüft fokussierte Route ZUERST, danach andere
- Alle Buttons (Ein/Aus, Bearbeiten, Löschen) nutzen bereits `getSelectedItem()` → funktionieren automatisch auf selektierter Route
- Drag + Klick-Verbindung-Trennen bevorzugen fokussierte Route

**Route-Segment Linksklick → Verbindung trennen** (`GameView`, `GameController`)
- Linksklick (kein Drag) auf Route-Segment → `handleRouteSegmentClick(route, nearest, other)`
- Alert.CONFIRMATION: „Station [X] aus Route entfernen? Verbindung zu [Y] wird getrennt."
- `route.getStops().remove(nearest)` → Züge der Route auf Index 0 zurückgesetzt
- Nächste Station zum Cursor ist der entfernte Stop (konsistent mit Drag-Logik)

**Zug-ListView Selektion** (`GameController`, `GameView`)
- Klick auf Zug in ListView → `gameView.setSelectedTrain(t)` + render
- Ausgewählter Zug: goldener Extra-Glow (radius 3.8×), goldene Kontur (#FFD54F, 2.5px)
- Alle Buttons (Zuweisen, Ablösen, Verkaufen) nutzen bereits `getSelectedItem()` → automatisch korrekt

---

#### Route-Segment Drag & Track-Demolish

**Zuletzt**: 23. Mai 2026 – Session 3 (Teil 1)

**Route-Segment Drag** (`GameView`, `GameController`)
- Im NONE-Modus: farbige Route-Linie zwischen 2 Stops anklicken + ziehen → Route-Stop ersetzen
- Nächstgelegener Endpunkt zum Cursor wird als „movable" gewählt, der andere bleibt „fixed"
- Snap-Radius 50 World-Units; gelber Glow auf Ziel-Station beim Snappen
- Gestrichelte weiße Vorschaulinie während des Drags
- Auto-Track-Bau: falls zwischen fixed und Ziel-Station noch keine Strecke existiert → neue wird automatisch gebaut (Kosten: -2.000€ Balance, +1.600€ NetWorth); Abbruch wenn Balance < 0
- Duplikat-Stop-Check: Ziel bereits in Route → Toast-Fehler, Abbruch
- Züge auf der Route werden nach Redirect auf Index 0 zurückgesetzt (Stop-Index könnte ungültig sein)
- Bestehende alte Strecke bleibt erhalten (wird NICHT abgerissen)
- Kreisrouten: Schlusskante (last→first) ebenfalls draggable

**Strecke abreißen** (`GameView`, `GameController`)
- Im NONE-Modus: graue Infrastruktur-Strecke (kein Route-Overlay) linksklicken → Alert.CONFIRMATION
- Kein Geld-Erstattung; Unternehmenswert sinkt um Kaufpreis (2.000€): `addNetWorth(-2_000)`
- DB-Eintrag wird ebenfalls gelöscht falls CurrentSave vorhanden

**Architektur-Änderungen**
- `GameView`: neue Inner-Types `RouteSegmentHit`, `RouteSegmentRedirectCallback`; Callbacks `trackDemolishCb`, `routeSegmentRedirectCb`; `popLastClickConsumed()` verhindert doppelte Klick-Verarbeitung
- `GameController`: `setBuildMode(BuildMode)` helper → toggle `trackInteractionEnabled` auf gameView; Konstanten `TRACK_BUILD_COST = 2_000`, `TRACK_NET_WORTH_GAIN = 1_600`; `buildTrackSilent()` für internen Auto-Bau
- `Track`: neue Setter `setFrom()`, `setTo()` + privates `recalcLength()`
- `setOnMouseClicked` prüft jetzt `e.getButton() == PRIMARY` + `popLastClickConsumed()`

---

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

- Speichern (Strg+S) persistiert Routen/Züge/Route-Stops nicht vollständig für erneutes Laden
- ChoiceDialog für Route-Zuweisung noch System-Style (nicht custom)
- Kein Hauptmenü/Lobby (US 8.1 – bewusst auf nächste Session verschoben)

---

### Nächste Schritte (Priorität)

1. **Lobby/Hauptmenü (US 8.1+8.2)** – `lobby.fxml` + `LobbyController`, Spielstand laden/neu/löschen, Rangliste-Platz
2. **Rangliste Frontend (US 7.1)** – in Lobby eingebaut, `HttpClient` → Backend
3. **Stadtwachstum (US 6.1)** – Stationen spawnen automatisch über Zeit
4. **Umsteigen (US 4.2)** – linienübergreifende Wegfindung in PathFinder

---

### Wichtige Design-Entscheidungen & Kontext

- **Genre:** Pausierbares Endlos-Aufbauspiel (kein Game Over)
- **Bau-System:** Freies Platzieren, keine Kollisionsabfrage, keine Signale
- **Wirtschaft:** Einnahmen = Passagier × ticketPrice × Stops; Kosten = opCostPerKm × km × delta (TrainType-Kosten erhöht: STANDARD 20k/0.40€, MEDIUM 48k/0.80€, SUPER 120k/1.60€; Stationskosten 8k€, Streckenkosten 2k€)
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
