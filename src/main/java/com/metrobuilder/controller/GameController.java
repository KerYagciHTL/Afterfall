package com.metrobuilder.controller;

import com.metrobuilder.model.PlayerProfile;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class GameController {

    @FXML private Label moneyLabel;
    @FXML private Label linesLabel;
    @FXML private Label buildToolLabel;

    private PlayerProfile profile;
    private Stage stage;

    public void setup(PlayerProfile profile, Stage stage, Parent root) {
        this.profile = profile;
        this.stage = stage;

        // Attach ESC handler once this root is added to a scene
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        returnToLobby();
                    }
                });
            }
        });
    }

    private void returnToLobby() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lobby.fxml"));
            Parent lobbyRoot = loader.load();
            LobbyController lobbyController = loader.getController();
            lobbyController.setup(profile, stage);
            LobbyController.transitionTo(stage.getScene().getRoot(), lobbyRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
