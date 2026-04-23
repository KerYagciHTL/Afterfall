package com.metrobuilder.controller;

import com.metrobuilder.model.PlayerProfile;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import com.metrobuilder.model.Station;

public class MainController {

    @FXML private Label statusLabel;
    @FXML private Button buildButton;
    @FXML private TextField usernameField;
    @FXML private Label playtimeLabel;

    private Station currentStation;

    @FXML
    public void initialize() {
        statusLabel.setText("System bereit. Keine Station ausgewählt.");
        // Profile binding happens in setProfile(), called by Main after FXML load
    }

    /** Called by Main.java after FXMLLoader.load() with the loaded PlayerProfile. */
    public void setProfile(PlayerProfile profile) {
        // Bidirectional: TextField <-> profile.username
        usernameField.textProperty().bindBidirectional(profile.usernameProperty());

        // Format total_playtime_seconds as HH:MM:SS using a string binding
        playtimeLabel.textProperty().bind(
            Bindings.createStringBinding(() -> {
                long total = profile.getTotalPlaytimeSeconds();
                long hours = total / 3600;
                long minutes = (total % 3600) / 60;
                long seconds = total % 60;
                return String.format("Spielzeit: %02d:%02d:%02d", hours, minutes, seconds);
            }, profile.totalPlaytimeSecondsProperty())
        );
    }

    @FXML
    private void onBuildClicked() {
        statusLabel.setText("Baue Metro-Linie...");
    }
}
