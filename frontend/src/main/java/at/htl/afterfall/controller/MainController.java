package at.htl.afterfall.controller;

import at.htl.afterfall.MainApp;
import at.htl.afterfall.model.SaveGame;
import at.htl.afterfall.persistence.DatabaseManager;
import at.htl.afterfall.protocol.command.ListSavesCommand;
import at.htl.afterfall.protocol.command.NewGameCommand;
import at.htl.afterfall.protocol.command.LoadGameCommand;
import at.htl.afterfall.protocol.dto.SaveInfoDto;
import at.htl.afterfall.util.GameClient;
import at.htl.afterfall.util.RankingClient;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainController {

    @FXML private ListView<SaveGame>              saveListView;
    @FXML private Button                          loadBtn;
    @FXML private Button                          deleteBtn;
    @FXML private Label                           statusLabel;
    @FXML private ListView<RankingClient.RankingEntry> rankingListView;
    @FXML private Label                           rankingStatusLabel;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private Timeline rankingRefreshTimer;

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

        loadRanking();

        Platform.runLater(() -> {
            RankingClient.ensurePlayerName();
            connectGameServer();
        });
    }

    private void loadRanking() {
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

        fetchRanking();

        rankingRefreshTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> fetchRanking()));
        rankingRefreshTimer.setCycleCount(Timeline.INDEFINITE);
        rankingRefreshTimer.play();
    }

    private void fetchRanking() {
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

    private void connectGameServer() {
        GameClient client = GameClient.get();
        client.setOnSaveList(saves -> updateSaveList(saves));
        if (client.isConnected()) { refreshServerSaves(); return; }

        CompletableFuture.runAsync(() -> {
            if (!client.connect()) {
                Platform.runLater(() -> statusLabel.setText("Game Server nicht erreichbar."));
                return;
            }
            try {
                String uuid = GameClient.loadOrCreateUuid();
                String name = RankingClient.getPlayerName();
                client.register(uuid, name);
                client.send(new ListSavesCommand(uuid));
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Registrierung fehlgeschlagen."));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void updateSaveList(List<?> saves) {
        saveListView.getItems().clear();
        for (Object o : saves) {
            if (o instanceof SaveInfoDto dto) {
                SaveGame sg = new SaveGame(dto.id, dto.name,
                    parseDateTime(dto.createdAt), parseDateTime(dto.lastSavedAt));
                saveListView.getItems().add(sg);
            }
        }
        statusLabel.setText("");
    }

    private java.time.LocalDateTime parseDateTime(String s) {
        try { return java.time.LocalDateTime.parse(s.replace(" ", "T")); }
        catch (Exception e) { return LocalDateTime.now(); }
    }

    private void refreshServerSaves() {
        try {
            GameClient.get().send(new ListSavesCommand(GameClient.get().getPlayerUuid()));
        } catch (Exception e) {
            statusLabel.setText("Fehler beim Laden der Spielstände.");
        }
    }

    @FXML
    public void onNewGame() {
        if (!GameClient.get().isConnected()) {
            statusLabel.setText("Kein Server verbunden.");
            return;
        }
        TextInputDialog dlg = new TextInputDialog("Mein Spielstand");
        dlg.setTitle("Neues Spiel");
        dlg.setHeaderText("Name des neuen Spielstands:");
        dlg.setContentText("Name:");
        dlg.showAndWait().ifPresent(name -> {
            if (name.isBlank()) return;
            try {
                GameClient client = GameClient.get();
                client.setOnSnapshot(snapshot -> openGameWithSnapshot(snapshot, true));
                client.send(new NewGameCommand(client.getPlayerUuid(), name));
            } catch (Exception e) {
                statusLabel.setText("Fehler: " + e.getMessage());
            }
        });
    }

    @FXML
    public void onLoad() {
        SaveGame selected = saveListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!GameClient.get().isConnected()) {
            statusLabel.setText("Kein Server verbunden.");
            return;
        }
        try {
            GameClient client = GameClient.get();
            client.setOnSnapshot(snapshot -> openGameWithSnapshot(snapshot, false));
            client.send(new LoadGameCommand(client.getPlayerUuid(), selected.getId()));
        } catch (Exception e) {
            statusLabel.setText("Fehler: " + e.getMessage());
        }
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
                   try {
                       GameClient.get().send(new at.htl.afterfall.protocol.command.DeleteSaveCommand(
                           GameClient.get().getPlayerUuid(), selected.getId()));
                   } catch (Exception ignored) {}
                   refreshServerSaves();
                   statusLabel.setText("\"" + selected.getName() + "\" wurde gelöscht.");
               });
    }

    private void openGameWithSnapshot(at.htl.afterfall.protocol.response.GameStateSnapshot snapshot, boolean isNewGame) {
        if (rankingRefreshTimer != null) rankingRefreshTimer.stop();
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/game.fxml"));
                loader.load();
                GameController ctrl = loader.getController();
                ctrl.initFromSnapshot(snapshot, isNewGame);
                Stage stage = (Stage) saveListView.getScene().getWindow();
                stage.setScene(new Scene(loader.getRoot()));
                Platform.runLater(() -> stage.setMaximized(true));
            } catch (IOException e) {
                statusLabel.setText("Fehler beim Öffnen des Spiels: " + e.getMessage());
            }
        });
    }
}
