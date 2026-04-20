package com.metrobuilder.app;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Der Haupteinstiegspunkt für die Metro Builder Anwendung.
 * Startet das JavaFX-Framework und initialisiert das MVC-Setup.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO: FXML laden, Model initialisieren, Controller verknüpfen
        primaryStage.setTitle("Metro Builder");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
