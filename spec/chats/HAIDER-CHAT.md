# HAIDER-CHAT.md

**Zweck:** Diese Datei dient als fortlaufendes Gedächtnis (Context) für den KI-Assistenten über alle Chat-Sitzungen hinweg. 
**Anweisung an die KI:** Lies diese Datei **IMMER ZU BEGINN** jeder neuen Chat-Sitzung, um den aktuellen Stand des Projekts zu verstehen. Aktualisiere diese Datei kontinuierlich mit wichtigen neuen Erkenntnissen, Entscheidungen, Fortschritten und dem aktuellen Fokus.

---

## Projekt: Afterfall (U-Bahn Management & Aufbauspiel)

### Aktueller Status
* **Letztes Update:** 07. Mai 2026
* **Zuletzt erledigt:** 
  * Analyse der Spielmechaniken aus der `README.md` und Erstellung von `STORIES.md`.
  * Anlage dieser Context-Datei (`HAIDER-CHAT.md`).
  * Erstellung einer Root `pom.xml`, um das Projekt als Maven Multi-Module-Projekt für IntelliJ aufzusetzen (bestehend aus den bereits angelegten Modulen `backend` und `frontend`).
* **Aktueller Fokus / Nächste Schritte:** 
  * Entwicklung der Basis-Infrastruktur für Backend und Frontend.

### Wichtige Design-Entscheidungen & Kontext
* **Genre:** Pausierbares Endlos-Aufbauspiel (ohne klassischen "Game Over" State).
* **Bau-System:** Freies Platzieren, keine Kollisionsabfrage bei Schienen, keine Signale (Keep it simple).
* **Wirtschaft:** Einnahmen durch Passagiere (Ticketpreis pro Station), Ausgaben durch Bau und laufende Zug-Betriebskosten. Schulden verhindern Neubau.
* **Passagiere:** Intelligente Wegfindung (Shortest Path) inkl. Umsteigen.
* **Tech-Stack:** 
  * Build-Tool: Maven (Multi-Module mit Root-POM)
  * Backend: Spring Boot (Java 21)
  * Frontend: JavaFX (Java 21)
