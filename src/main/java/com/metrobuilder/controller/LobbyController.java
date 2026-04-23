package com.metrobuilder.controller;

import com.metrobuilder.model.PlayerProfile;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LobbyController {

    @FXML private TextField usernameField;
    @FXML private Label playtimeLabel;

    private PlayerProfile profile;
    private Stage stage;

    public void setup(PlayerProfile profile, Stage stage) {
        this.profile = profile;
        this.stage = stage;

        usernameField.textProperty().bindBidirectional(profile.usernameProperty());
        playtimeLabel.textProperty().bind(
            Bindings.createStringBinding(() -> {
                long total = profile.getTotalPlaytimeSeconds();
                return String.format("%02d:%02d:%02d", total / 3600, (total % 3600) / 60, total % 60);
            }, profile.totalPlaytimeSecondsProperty())
        );
    }

    @FXML
    private void onStartGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
            Parent gameRoot = loader.load();
            GameController gameController = loader.getController();
            gameController.setup(profile, stage, gameRoot);
            transitionTo(stage.getScene().getRoot(), gameRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void transitionTo(Parent from, Parent to) {
        Stage stage = (Stage) from.getScene().getWindow();
        to.setOpacity(0.0);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), from);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            stage.getScene().setRoot(to);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), to);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }
}
