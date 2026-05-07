# Pflichtenheft – Afterfall

**Kerimcan Yagci, Nico Haider, Fridolino Dürk**  
Version 1.0.0 · 2026-05-07

---

## Inhaltsverzeichnis

1. [Ausgangssituation](#1-ausgangssituation)
2. [Istzustand](#2-istzustand)
   - 2.1 [Marktübersicht](#21-marktübersicht)
3. [Problemstellung](#3-problemstellung)
4. [Aufgabenstellung](#4-aufgabenstellung)
   - 4.1 [Funktionale Anforderungen](#41-funktionale-anforderungen)
   - 4.2 [Nicht-funktionale Anforderungen (NFA)](#42-nicht-funktionale-anforderungen-nfa)
5. [Ziele](#5-ziele)
6. [Mengengerüst](#6-mengengerüst)
7. [Rahmenbedingungen](#7-rahmenbedingungen)
   - 7.1 [Technisch](#71-technisch)
   - 7.2 [Zeitlich](#72-zeitlich)

---

## 1. Ausgangssituation

Simulationsspiele gehören zu den beliebtesten Genres im PC-Bereich. Besonders Aufbau- und Managementspiele sprechen ein breites Publikum an, da sie strategisches Denken, wirtschaftliches Planen und logistisches Geschick fördern. Der öffentliche Nahverkehr ist dabei ein Thema, das viele Menschen täglich betrifft und dessen Komplexität in Spielform gut vermittelt werden kann.

---

## 2. Istzustand

Derzeit gibt es einige bekannte Spiele in diesem Genre, darunter kommerzielle Titel wie *Mini Metro*, *OpenTTD* oder *Transport Fever*. Diese Spiele sind entweder kostenpflichtig, technisch sehr komplex oder für Gelegenheitsspieler schwer zugänglich. Eine einfache, kostenlose JavaFX-basierte Alternative mit überschaubarem Funktionsumfang existiert nicht.

### 2.1 Marktübersicht

| Spiel | Plattform | Preis | Komplexität |
|---|---|---|---|
| Mini Metro | PC, Mobile | kostenpflichtig | mittel |
| OpenTTD | PC | kostenlos | hoch |
| Transport Fever 2 | PC | kostenpflichtig | sehr hoch |

Keines der genannten Spiele ist als schlanke JavaFX-Desktopanwendung mit lokalem Persistenz-Backend verfügbar.

---

## 3. Problemstellung

Bestehende U-Bahn- und Nahverkehrssimulationen sind entweder zu komplex für einen schnellen Einstieg, mit hohen Kosten verbunden oder nicht als leichtgewichtige Desktop-App realisiert. Es fehlt eine einfache, kostenlose und technisch zugängliche Variante, die grundlegende Managementmechaniken spielerisch vermittelt und dabei mit modernen Java-Technologien (JavaFX, JDBC) realisiert ist.

---

## 4. Aufgabenstellung

Ziel des Projekts ist ein pausierbares U-Bahn-Management- und Aufbauspiel mit Fokus auf wirtschaftliches Wachstum, Netzoptimierung und steigende Passagiernachfrage. Der Spieler baut ein eigenes U-Bahnnetz auf, verbindet Orte innerhalb einer wachsenden Stadt und versucht langfristig ein profitables und effizientes Transportsystem zu betreiben. Es gibt keinen klassischen Game-Over-Zustand; das Spiel ist auf endloses Wachstum ausgelegt.

### 4.1 Funktionale Anforderungen

#### Kartenverwaltung
- Der Spieler kann auf einer zweidimensionalen Karte frei neue Stationen platzieren und diese mit Schienenstrecken verbinden.
- Stationen und Schienen kosten Spielgeld.
- Schienen können sich ohne Einschränkung kreuzen; es gibt kein Kollisionssystem, keine Signale und keine Ampelmechanik.
- Zu Beginn des Spiels existieren zwei vorgebaute Stationen.
- Infrastruktur (Stationen, Schienen) kann verkauft werden, um Liquidität zurückzugewinnen.

#### Tutorial
- Beim ersten Spielstart wird ein kurzes Tutorial angezeigt.
- Der Spieler muss die beiden vorgebauten Stationen mit einer Strecke verbinden, um die Grundmechaniken kennenzulernen.

#### Zugverwaltung
Es existieren drei Zugtypen:

| Zugtyp | Kapazität | Geschwindigkeit | Kosten |
|---|---|---|---|
| Standardzug | normal | normal | günstig |
| Mittlerer Zug | hoch | normal | mittel |
| Superzug | sehr hoch | etwas höher | teuer |

- Zu Beginn besitzt der Spieler einen kostenlosen Standardzug.
- Weitere Züge können käuflich erworben werden.
- Jeder Zug benötigt eine festgelegte Route aus mindestens zwei Stationen.
- Jede Route erhält automatisch eine zufällige, gut sichtbare Linienfarbe (keine schwarzen, weißen oder zu hellen/dunklen Farben).
- Züge fahren ihre Routen automatisch ab.
- Der Spieler kann Einsatzzeiten je Route konfigurieren.

#### Wirtschaftssystem
- Der Betrieb einer Route verursacht laufende Stromkosten pro gefahrenem Kilometer, abhängig vom Zugtyp.
- Das Ticketsystem basiert auf der Anzahl gefahrener Stationen; der Ticketpreis pro Station wird vom Spieler festgelegt.
- Niedrige Ticketpreise erhöhen die Attraktivität, hohe Ticketpreise senken die Zufriedenheit und reduzieren die Nachfrage.
- Der Spieler kann ins Minus geraten; in diesem Zustand können keine neuen Gebäude oder Strecken gekauft werden.
- Um die Liquidität wiederherzustellen, kann der Spieler verlustbringende Strecken deaktivieren, Infrastruktur verkaufen oder profitable Linien stärken.

#### Passagiersystem
- Einwohner erzeugen kontinuierlich Nachfrage zwischen Stationen.
- Jeder Passagier besitzt eine Zielstation; Passagiere sammeln sich an Stationen und werden dort nach Ziel sortiert dargestellt.
- Passagiere suchen automatisch den kürzesten verfügbaren Weg und können selbstständig zwischen Linien umsteigen.
- Stationen und Züge besitzen technisch unbegrenzte Kapazität; dauerhaft hohe Wartezeit durch überfüllte Linien senkt jedoch die Zufriedenheit.

#### Zufriedenheitssystem
- Die allgemeine Stadtszufriedenheit repräsentiert, wie gut das Verkehrsnetz die Bevölkerung versorgt.
- Zufriedenheit steigt, wenn viele Einwohner effizient transportiert werden.
- Zufriedenheit sinkt, wenn:
  - wichtige Orte nicht angebunden sind
  - Passagiere lange warten müssen
  - Strecken dauerhaft überfüllt sind
  - Ticketpreise zu hoch sind
- Selbst bei 0 % Zufriedenheit gibt es weiterhin Fahrgäste; niedrige Zufriedenheit erschwert jedoch wirtschaftliches Wachstum.

#### Stadtwachstum
- Die Stadt wächst automatisch: neue Einwohner kommen hinzu, neue Orte mit hohem Anbindungsinteresse entstehen zufällig.
- Neue Orte erwarten innerhalb einer gewissen Zeit eine U-Bahnanbindung; ohne Anbindung sinkt die Zufriedenheit schrittweise.

#### Online-Rangliste
- Ein Online-Ranking basiert auf dem Unternehmenswert (Net Worth) des Spielers.
- Verdientes Geld erhöht den Unternehmenswert.
- Gekaufte Infrastruktur reduziert den Unternehmenswert nicht direkt, da sie selbst als Vermögenswert gilt.
- Laufende Verluste durch unprofitable Routen können den Unternehmenswert senken.

#### Speichern & Laden
- Der Spielstand (Karte, Stationen, Strecken, Züge, Routen, Finanzen, Zufriedenheit) wird lokal via JDBC persistiert.
- Der Spieler kann mehrere Spielstände anlegen, laden und löschen; diese werden in einer ListView dargestellt.

### 4.2 Nicht-funktionale Anforderungen (NFA)

#### Bedienbarkeit
- Das Spiel soll ohne Handbuch intuitiv bedienbar sein.
- Wichtige Aktionen (Station setzen, Strecke bauen, Zug kaufen) sind in maximal 3 Interaktionsschritten ausführbar.
- Das Spiel ist pausierbar; im Pausezustand können Netzänderungen vorgenommen werden.

#### Technische Pflichtbestandteile (Schulanforderungen)
- **MVC-Pattern**: Strikte Trennung von Model, View und Controller.
- **ListView**: Darstellung von Spielständen und/oder Routen mit Reaktion auf Selektion, Änderungen sowie Sortier- und Filterfunktion.
- **Property Binding**: Uni- und bidirektionales Binding (z. B. Finanzen, Zufriedenheitswert, Routenstatus an UI-Elemente gebunden).
- **EventHandler**: Reaktion auf Tastatur-Events (z. B. Pause-Taste, Hotkeys für Baumodus) und Maus-Events (Stationsplatzierung, Streckenziehen).
- **Persistierung via JDBC**: Alle Spielstände werden in einer lokalen Datenbank gespeichert und geladen.

#### Performance
- Das Laden eines Spielstands dauert weniger als 2 Sekunden.
- Die Simulation läuft flüssig bei mindestens 30 Frames pro Sekunde mit bis zu 50 gleichzeitig aktiven Passagieren.

#### Kompatibilität
- Die JavaFX-App läuft auf Windows 10/11 und macOS 12+.
- Kein Internetzugang für den Spielbetrieb erforderlich (Ausnahme: Online-Rangliste).

---

## 5. Ziele

- **Spielspaß**: Ein zugängliches, pausierbares Managementspiel ohne steile Lernkurve bieten.
- **Kostenlos**: Alle Funktionen sind ohne Abonnement oder Einmalkauf nutzbar.
- **Technisches Lernen**: Einsatz moderner Java-Technologien (JavaFX, JDBC, MVC) in einem realen Anwendungskontext demonstrieren.
- **Motivation durch Wettbewerb**: Das Online-Ranking motiviert Spieler, ihr Netz langfristig zu optimieren.
- **Endloses Spielziel**: Kein Game Over – der Fokus liegt auf kontinuierlichem Wachstum und Optimierung.

---

## 6. Mengengerüst

| Entität | Erwartete Menge |
|---|---|
| Stationen pro Spielstand | bis zu 50 |
| Strecken pro Spielstand | bis zu 100 |
| Züge pro Spielstand | bis zu 20 |
| Gleichzeitig aktive Passagiere | bis zu 200 |
| Gespeicherte Spielstände | bis zu 10 |

Datenbestände: Spielstanddaten (Stationen, Strecken, Züge, Routen, Finanzen) als relationale Daten in lokaler SQLite-Datenbank via JDBC.

---

## 7. Rahmenbedingungen

### 7.1 Technisch

- **Sprache**: Java 21+
- **UI-Framework**: JavaFX
- **Persistenz**: JDBC (lokale SQLite-Datenbank)
- **Architektur**: MVC-Pattern
- **Build-Tool**: Maven
- **Online-Ranking-Backend**: Dockerisierter REST-Service auf privatem Server
- **Teamgröße**: 3 Personen (Kerimcan Yagci, Nico Haider, Fridolino Dürk)
- **KI-Einsatz**: Erlaubt, sofern Einsatz dokumentiert und Output erklärbar

### 7.2 Zeitlich

| Meilenstein | Datum |
|---|---|
| Teams & Themen eingetragen | 20.04.2026 |
| Finale Abgabe via GitHub Classroom | 26.05.2026 |
| Präsentation (5–10 min) | 28.05.2026 |
