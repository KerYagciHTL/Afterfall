package at.htl.afterfall.controller;

import at.htl.afterfall.MainApp;
import at.htl.afterfall.model.SaveGame;
import at.htl.afterfall.persistence.DatabaseManager;
import at.htl.afterfall.persistence.SaveGameDao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML private ListView<SaveGame> saveListView;
    @FXML private Button             loadBtn;
    @FXML private Button             deleteBtn;
    @FXML private Label              statusLabel;

    private final SaveGameDao saveGameDao = new SaveGameDao();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        DatabaseManager.initSchema();
        refreshList();

        loadBtn.disableProperty().bind(
            saveListView.getSelectionModel().selectedItemProperty().isNull()
        );
        deleteBtn.disableProperty().bind(
            saveListView.getSelectionModel().selectedItemProperty().isNull()
        );

        saveListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SaveGame sg, boolean empty) {
                super.updateItem(sg, empty);
                if (empty || sg == null) {
                    setGraphic(null);
                    setStyle("");
                    return;
                }
                VBox box = new VBox(4);
                Label nameLbl = new Label(sg.getName());
                nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold;");
                Label dateLbl = new Label(
                    "Erstellt: " + sg.getCreatedAt().format(FMT) +
                    "  |  Zuletzt gespeichert: " + sg.getLastSaved().format(FMT)
                );
                dateLbl.setStyle("-fx-text-fill: #78909c; -fx-font-size: 11;");
                box.getChildren().addAll(nameLbl, dateLbl);
                setGraphic(box);
                String bg = isSelected() ? "#1a2540" : "#13141f";
                setStyle("-fx-padding: 10 16 10 16; -fx-background-color: " + bg + ";");
            }
        });

        saveListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, o, n) -> saveListView.refresh()
        );
    }

    private void refreshList() {
        saveListView.getItems().setAll(saveGameDao.findAll());
        if (saveListView.getItems().isEmpty()) {
            statusLabel.setText("Noch keine Spielstände vorhanden.");
        } else {
            statusLabel.setText("");
        }
    }

    @FXML
    public void onNewGame() {
        TextInputDialog dlg = new TextInputDialog("Mein Spielstand");
        dlg.setTitle("Neues Spiel");
        dlg.setHeaderText("Name des neuen Spielstands:");
        dlg.setContentText("Name:");
        dlg.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                int id   = saveGameDao.insert(name);
                SaveGame sg = new SaveGame(id, name, LocalDateTime.now(), LocalDateTime.now());
                openGame(sg, true);
            }
        });
    }

    @FXML
    public void onLoad() {
        SaveGame selected = saveListView.getSelectionModel().getSelectedItem();
        if (selected != null) openGame(selected, false);
    }

    @FXML
    public void onDelete() {
        SaveGame selected = saveListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Spielstand löschen");
        confirm.setHeaderText("\"" + selected.getName() + "\" wirklich löschen?");
        confirm.setContentText("Dieser Vorgang kann nicht rückgängig gemacht werden.");
        confirm.showAndWait()
               .filter(r -> r == ButtonType.OK)
               .ifPresent(r -> {
                   saveGameDao.delete(selected.getId());
                   refreshList();
                   statusLabel.setText("\"" + selected.getName() + "\" wurde gelöscht.");
               });
    }

    private void openGame(SaveGame save, boolean isNewGame) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/game.fxml"));
            loader.load();
            GameController ctrl = loader.getController();

            if (isNewGame) {
                ctrl.startNewGame(save);
            } else {
                ctrl.loadGame(save);
            }

            Stage stage = (Stage) saveListView.getScene().getWindow();
            stage.setScene(new Scene(loader.getRoot()));
        } catch (IOException e) {
            statusLabel.setText("Fehler: " + e.getMessage());
        }
    }
}
