package at.htl.afterfall.tutorial;

import at.htl.afterfall.model.GameWorld;
import java.util.List;

public class TutorialManager {
    private final List<TutorialStep> steps;
    private int     currentIndex    = 0;
    private boolean active          = true;
    private boolean currentTaskDone = false;

    public TutorialManager() {
        steps = List.of(
            new TutorialStep(
                "Willkommen bei Afterfall!",
                "Du leitest ein städtisches U-Bahn-Unternehmen.\n" +
                "Zwei Stationen und ein Startzug sind bereits\n" +
                "vorhanden. Baue jetzt dein erstes Netz auf!",
                null
            ),
            new TutorialStep(
                "Strecke bauen",
                "Drücke [T] oder klicke auf '⟼ Strecke'.\n" +
                "Klicke dann auf Hauptbahnhof\nund danach auf Stadtzentrum.",
                w -> !w.getTracks().isEmpty()
            ),
            new TutorialStep(
                "Route erstellen",
                "Drücke [R] oder klicke auf '＋ Route'.\n" +
                "Klicke dann nacheinander auf beide Stationen.\n" +
                "ESC beendet den Route-Modus.",
                w -> w.getRoutes().stream().anyMatch(r -> r.getStops().size() >= 2)
            ),
            new TutorialStep(
                "Zug zuweisen",
                "Wähle den Zug in der Züge-Liste rechts aus.\n" +
                "Klicke dann auf 'Route zuweisen'.",
                w -> w.getTrains().stream().anyMatch(t -> t.getRoute() != null)
            ),
            new TutorialStep(
                "Neue Station bauen",
                "Drücke [S] oder klicke auf '⊕ Station'.\n" +
                "Platziere eine dritte Station\nirgendwo auf der Karte.",
                w -> w.getStations().size() >= 3
            ),
            new TutorialStep(
                "Route umleiten",
                "Klicke auf die farbige Routenlinie zwischen\n" +
                "zwei Stationen und ziehe sie zur neuen Station.\n" +
                "Sie wird automatisch eingefügt.",
                w -> w.getRoutes().stream().anyMatch(r -> r.getStops().size() >= 3)
            ),
            new TutorialStep(
                "Zufriedenheit & Wirtschaft",
                "Halte Wartezeiten kurz und verbinde alle\n" +
                "Stationen für hohe Zufriedenheit (Ziel: 90%+).\n\n" +
                "Züge verursachen laufende Betriebskosten –\n" +
                "plane dein Netz wirtschaftlich!\n\n" +
                "Strg+S speichert deinen Spielstand.",
                null
            ),
            new TutorialStep(
                "Los geht's!",
                "Du kennst die Grundlagen.\n" +
                "Baue dein Netz weiter aus und\n" +
                "bringe deine Stadt zum Laufen!",
                null
            )
        );
    }

    public boolean        isActive()          { return active; }
    public TutorialStep   current()           { return steps.get(currentIndex); }
    public int            getCurrentIndex()   { return currentIndex; }
    public int            getTotalSteps()     { return steps.size(); }
    public boolean        isCurrentTaskDone() { return currentTaskDone; }

    /** Prüft ob aktuelle Aufgabe erfüllt wurde. Gibt true zurück wenn gerade erledigt. */
    public boolean check(GameWorld world) {
        if (!active) return false;
        TutorialStep step = current();
        if (!step.isTask() || currentTaskDone) return false;
        if (step.completionCheck.test(world)) {
            currentTaskDone = true;
            return true;
        }
        return false;
    }

    public void advance() {
        currentIndex++;
        currentTaskDone = false;
        if (currentIndex >= steps.size()) active = false;
    }

    public void skip() { active = false; }
}
