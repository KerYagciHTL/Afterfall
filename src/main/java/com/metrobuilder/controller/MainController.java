package com.metrobuilder.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.metrobuilder.model.Station;

public class MainController {

    @FXML
    private Label statusLabel;

    @FXML
    private Button buildButton;

    private Station currentStation;

    @FXML
    public void initialize() {
        // Init-Logik nach dem Laden der FXML-Datei
        statusLabel.setText("System bereit. Keine Station ausgewählt.");
    }

    @FXML
    private void onBuildClicked() {
        // Beispiel für einen Event-Handler
        statusLabel.setText("Baue Metro-Linie...");
    }
}
