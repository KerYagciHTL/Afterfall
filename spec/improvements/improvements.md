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
