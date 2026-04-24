package com.metrobuilder.controller;

import com.metrobuilder.model.PlayerProfile;
import com.metrobuilder.view.GameSprites;
import com.metrobuilder.view.MapView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class GameController {

    @FXML private MapView mapView;
    @FXML private Label moneyLabel;
    @FXML private Label linesLabel;
    @FXML private Label buildToolLabel;

    private PlayerProfile profile;
    private Stage stage;

    public void setup(PlayerProfile profile, Stage stage, Parent root) {
        this.profile = profile;
        this.stage = stage;

        renderDemo();

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

    private void renderDemo() {
        GameSprites sprites = new GameSprites();
        double tileSize = 96;
        double startX = 40;
        double railY = 200;

        // Draw a row of 7 horizontal rail tiles
        for (int i = 0; i < 7; i++) {
            mapView.placeSprite(sprites.railHorizontal(), startX + i * tileSize, railY, tileSize);
        }

        // Place a blue train (front + wagon + rear) on the rail
        double trainY = railY - 4;
        mapView.placeSprite(sprites.trainLeftBlueFront(), startX,                trainY, tileSize);
        mapView.placeSprite(sprites.trainLeftBlueWagon(), startX + tileSize,     trainY, tileSize);
        mapView.placeSprite(sprites.trainLeftBlueRear(),  startX + tileSize * 2, trainY, tileSize);
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
