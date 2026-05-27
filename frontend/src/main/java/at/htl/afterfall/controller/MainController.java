package at.htl.afterfall.controller;

import at.htl.afterfall.MainApp;
import at.htl.afterfall.model.SaveGame;
import at.htl.afterfall.persistence.DatabaseManager;
import at.htl.afterfall.persistence.SaveGameDao;
import at.htl.afterfall.util.RankingClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class MainController {

    @FXML private ListView<SaveGame>              saveListView;
    @FXML private Button                          loadBtn;
    @FXML private Button                          deleteBtn;
    @FXML private Label                           statusLabel;
    @FXML private ListView<RankingClient.RankingEntry> rankingListView;
    @FXML private Label                           rankingStatusLabel;

    private final SaveGameDao saveGameDao = new SaveGameDao();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        DatabaseManager.initSchema();

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
                    setText(null);
                    setStyle("-fx-background-color: #13141f; -fx-border-width: 0;");
                    return;
                }

                Rectangle accent = new Rectangle(3, 38);
                accent.setFill(Color.web(isSelected() ? "#3d5af1" : "#252848"));
                accent.setArcWidth(2);
                accent.setArcHeight(2);

                VBox textBox = new VBox(3);
                textBox.setAlignment(Pos.CENTER_LEFT);

                Label nameLbl = new Label(sg.getName());
                nameLbl.setStyle("-fx-text-fill: " + (isSelected() ? "white" : "#c8cce8")
                        + "; -fx-font-size: 14; -fx-font-weight: bold;");
                Label dateLbl = new Label("Gespeichert: " + sg.getLastSaved().format(FMT));
                dateLbl.setStyle("-fx-text-fill: #546e7a; -fx-font-size: 11;");
                textBox.getChildren().addAll(nameLbl, dateLbl);

                HBox row = new HBox(12, accent, textBox);
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
                setStyle("-fx-padding: 10 16 10 12; -fx-background-color: "
                        + (isSelected() ? "#181e36" : "#13141f")
                        + "; -fx-border-color: transparent transparent #1a1d2e transparent;"
                        + " -fx-border-width: 0 0 1 0;");
            }
        });

        saveListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, o, n) -> saveListView.refresh()
        );

        Label savePlaceholder = new Label("Noch keine Spielstände vorhanden.");
        savePlaceholder.setStyle("-fx-text-fill: #37474f; -fx-font-size: 13;");
        saveListView.setPlaceholder(savePlaceholder);

        refreshList();
        loadRanking();
    }

    private void loadRanking() {
        rankingStatusLabel.setText("Lade Rangliste...");

        Label rankPlaceholder = new Label("Noch keine Einträge vorhanden.");
        rankPlaceholder.setStyle("-fx-text-fill: #37474f; -fx-font-size: 12;");
        rankingListView.setPlaceholder(rankPlaceholder);

        rankingListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(RankingClient.RankingEntry e, boolean empty) {
                super.updateItem(e, empty);
                if (empty || e == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }

                String rankColor = switch (e.rank()) {
                    case 1 -> "#ffd700";
                    case 2 -> "#c0c0c0";
                    case 3 -> "#cd7f32";
                    default -> "#3d5af1";
                };
                String rankText = switch (e.rank()) {
                    case 1 -> "🥇";
                    case 2 -> "🥈";
                    case 3 -> "🥉";
                    default -> "#" + e.rank();
                };

                Label rankLbl = new Label(rankText);
                rankLbl.setStyle("-fx-text-fill: " + rankColor
                        + "; -fx-font-size: 13; -fx-font-weight: bold; -fx-min-width: 36;");

                Label nameLbl = new Label(e.playerName());
                nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label worthLbl = new Label(formatNetWorth(e.netWorth()));
                worthLbl.setStyle("-fx-text-fill: #43d494; -fx-font-size: 13; -fx-font-weight: bold;");

                HBox row = new HBox(10, rankLbl, nameLbl, spacer, worthLbl);
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
                setStyle("-fx-background-color: transparent; -fx-padding: 6 16 6 16;");
            }
        });

        CompletableFuture.supplyAsync(RankingClient::fetchRanking)
            .thenAcceptAsync(entries -> Platform.runLater(() -> {
                if (entries == null) {
                    rankingStatusLabel.setText("Rangliste nicht erreichbar.");
                    return;
                }
                rankingStatusLabel.setText("");
                rankingListView.getItems().setAll(entries);
            }));
    }

    private static String formatNetWorth(double amount) {
        if (amount >= 1_000_000) return String.format("%.1f Mio €", amount / 1_000_000.0);
        if (amount >= 1_000)     return String.format("%.1fk €", amount / 1_000.0);
        return String.format("%.0f €", amount);
    }

    private void refreshList() {
        saveListView.getItems().setAll(saveGameDao.findAll());
        statusLabel.setText("");
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
            stage.setMaximized(true);
        } catch (IOException e) {
            statusLabel.setText("Fehler: " + e.getMessage());
        }
    }
}
