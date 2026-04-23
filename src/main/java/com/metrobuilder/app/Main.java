package com.metrobuilder.app;

import com.metrobuilder.controller.MainController;
import com.metrobuilder.db.DatabaseManager;
import com.metrobuilder.model.PlayerProfile;
import com.metrobuilder.model.dao.PlayerProfileDao;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.time.Instant;

public class Main extends Application {

    private PlayerProfile profile;
    private PlayerProfileDao dao;
    private Instant sessionStart;

    @Override
    public void init() {
        DatabaseManager.initialize();
        dao = new PlayerProfileDao();
        profile = dao.load();
        sessionStart = Instant.now();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlLocation = getClass().getResource("/fxml/main.fxml");
        if (fxmlLocation == null) {
            System.err.println("Kritischer Fehler: main.fxml wurde nicht gefunden!");
            System.exit(1);
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setProfile(profile);

        primaryStage.setTitle("Metro Builder");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    @Override
    public void stop() {
        long elapsed = Instant.now().getEpochSecond() - sessionStart.getEpochSecond();
        profile.setTotalPlaytimeSeconds(profile.getTotalPlaytimeSeconds() + elapsed);
        dao.save(profile);
        DatabaseManager.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
