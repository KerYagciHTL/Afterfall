package at.htl.afterfall.tutorial;

import at.htl.afterfall.model.GameWorld;
import java.util.function.Predicate;

public class TutorialStep {
    public final String               title;
    public final String               description;
    public final Predicate<GameWorld> completionCheck; // null = Erklärung, kein Aufgabenschritt

    public TutorialStep(String title, String description, Predicate<GameWorld> completionCheck) {
        this.title           = title;
        this.description     = description;
        this.completionCheck = completionCheck;
    }

    public boolean isTask() { return completionCheck != null; }
}
