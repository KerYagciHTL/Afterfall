package at.htl.afterfall.tutorial;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

public class TutorialOverlay extends VBox {

    private static final String STYLE_BASE =
        "-fx-background-color: rgba(13,14,27,0.95);" +
        "-fx-background-radius: 12;" +
        "-fx-border-color: #252848;" +
        "-fx-border-radius: 12;" +
        "-fx-border-width: 1;" +
        "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.75),18,0,0,4);";

    private static final String STYLE_DONE =
        "-fx-background-color: rgba(13,14,27,0.95);" +
        "-fx-background-radius: 12;" +
        "-fx-border-color: #43d494;" +
        "-fx-border-radius: 12;" +
        "-fx-border-width: 1;" +
        "-fx-effect: dropshadow(gaussian,rgba(67,212,148,0.35),14,0,0,0);";

    private final TutorialManager manager;
    private final Label  stepLabel;
    private final Label  titleLabel;
    private final Label  descLabel;
    private final Label  statusLabel;
    private final Button nextBtn;

    public TutorialOverlay(TutorialManager manager, Runnable onAdvance, Runnable onSkip) {
        this.manager = manager;

        setSpacing(8);
        setPadding(new Insets(14, 18, 14, 18));
        setMaxWidth(315);
        setStyle(STYLE_BASE);
        StackPane.setAlignment(this, Pos.BOTTOM_LEFT);
        StackPane.setMargin(this, new Insets(0, 0, 22, 16));

        // Klicks nicht an Canvas durchreichen
        setOnMouseClicked(Event::consume);
        setOnMousePressed(Event::consume);

        stepLabel = new Label();
        stepLabel.setStyle("-fx-text-fill: #6068a0; -fx-font-size: 10; -fx-font-weight: bold;");

        titleLabel = new Label();
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(279);

        descLabel = new Label();
        descLabel.setStyle("-fx-text-fill: #aab0d8; -fx-font-size: 12;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(279);

        statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(279);

        Separator sep = new Separator();

        // Buttons
        Button skipBtn = new Button("Überspringen");
        skipBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #505880;" +
            "-fx-font-size: 11; -fx-cursor: hand; -fx-padding: 5 8 5 0;"
        );
        skipBtn.setOnAction(e -> onSkip.run());
        skipBtn.setOnMouseEntered(e -> skipBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #8890c8;" +
            "-fx-font-size: 11; -fx-cursor: hand; -fx-padding: 5 8 5 0;"
        ));
        skipBtn.setOnMouseExited(e -> skipBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #505880;" +
            "-fx-font-size: 11; -fx-cursor: hand; -fx-padding: 5 8 5 0;"
        ));

        nextBtn = new Button("Weiter →");
        nextBtn.setStyle(
            "-fx-background-color: #3d5af1; -fx-text-fill: white;" +
            "-fx-font-size: 12; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 7 16 7 16;"
        );
        nextBtn.setOnAction(e -> onAdvance.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.getChildren().addAll(skipBtn, spacer, nextBtn);

        getChildren().addAll(stepLabel, titleLabel, descLabel, statusLabel, sep, buttons);
        refresh();
    }

    public void refresh() {
        if (!manager.isActive()) {
            setVisible(false);
            setManaged(false);
            return;
        }
        setVisible(true);
        setManaged(true);

        TutorialStep step  = manager.current();
        int          idx   = manager.getCurrentIndex();
        int          total = manager.getTotalSteps();

        stepLabel.setText("Schritt " + (idx + 1) + " / " + total);
        titleLabel.setText(step.title);
        descLabel.setText(step.description);

        if (step.isTask()) {
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
            if (manager.isCurrentTaskDone()) {
                statusLabel.setText("✓  Erledigt!");
                statusLabel.setStyle(
                    "-fx-text-fill: #43d494; -fx-font-size: 12; -fx-font-weight: bold;"
                );
                nextBtn.setVisible(true);
                setStyle(STYLE_DONE);
            } else {
                statusLabel.setText("⏳  Warte auf Aktion ...");
                statusLabel.setStyle("-fx-text-fill: #e8c468; -fx-font-size: 11;");
                nextBtn.setVisible(false);
                setStyle(STYLE_BASE);
            }
        } else {
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
            nextBtn.setVisible(true);
            setStyle(STYLE_BASE);
        }

        boolean isLast = idx == total - 1;
        nextBtn.setText(isLast ? "Fertig!" : "Weiter →");
    }
}
