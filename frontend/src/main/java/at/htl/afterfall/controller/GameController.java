package at.htl.afterfall.controller;

import at.htl.afterfall.GameConfig;
import at.htl.afterfall.MainApp;
import at.htl.afterfall.model.*;
import at.htl.afterfall.persistence.*;
import at.htl.afterfall.util.RankingClient;
import at.htl.afterfall.simulation.GameLoop;
import at.htl.afterfall.tutorial.TutorialManager;
import at.htl.afterfall.tutorial.TutorialOverlay;
import at.htl.afterfall.util.ColorGenerator;
import at.htl.afterfall.view.GameView;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class GameController {

    @FXML
    private Label balanceLabel;
    @FXML
    private Label incomeRateLabel;
    @FXML
    private Label satisfactionLabel;
    @FXML
    private Label netWorthLabel;
    @FXML
    private Button pauseButton;
    @FXML
    private Button speedButton;
    @FXML
    private ToggleButton buildStationBtn;
    @FXML
    private ToggleButton buildTrackBtn;
    @FXML
    private StackPane canvasContainer;
    @FXML
    private ListView<Route> routeListView;
    @FXML
    private ListView<Train> trainListView;
    @FXML
    private Label editStatusLabel;

    private GameWorld world;
    private GameView gameView;
    private GameLoop gameLoop;
    private ColorGenerator colorGen;
    private BuildMode buildMode = BuildMode.NONE;
    private Station trackStart;
    private Route activeRoute;
    private VBox toastContainer;
    private Region toastSpacer;
    private VBox trainShopOverlay = null;
    private long lastRateUiUpdate = 0;
    private final Random rng = new Random();
    private TutorialManager tutorialManager;
    private TutorialOverlay tutorialOverlay;
    private Timeline        tutorialTimer;

    private static double TRACK_BUILD_COST()      { return GameConfig.get().trackBuildCost; }
    private static double TRACK_NET_WORTH_GAIN() { return GameConfig.get().trackNetWorthGain; }
    private static double STATION_BUILD_COST()   { return GameConfig.get().stationBuildCost; }
    private static double STATION_NET_WORTH_GAIN(){ return GameConfig.get().stationNetWorthGain; }

    private final SaveGameDao saveGameDao = new SaveGameDao();
    private final StationDao stationDao = new StationDao();
    private final TrackDao trackDao = new TrackDao();
    private final RouteDao routeDao = new RouteDao();
    private final TrainDao trainDao = new TrainDao();
    private final EconomyDao economyDao = new EconomyDao();

    @FXML
    public void initialize() {
        DatabaseManager.initSchema();
        world = new GameWorld();
        colorGen = new ColorGenerator();
        gameView = new GameView(world);

        gameView.widthProperty().bind(canvasContainer.widthProperty());
        gameView.heightProperty().bind(canvasContainer.heightProperty());
        gameView.widthProperty().addListener((obs, o, n) -> gameView.render());
        gameView.heightProperty().addListener((obs, o, n) -> gameView.render());
        canvasContainer.getChildren().add(gameView);

        // Callbacks für Track-Interaktion
        gameView.setTrackInteractionEnabled(true);
        gameView.setTrackDemolishCb(this::handleTrackDemolish);
        gameView.setRouteSegmentRedirectCb(this::handleRouteSegmentRedirect);
        gameView.setRouteSegmentClickCb(this::handleRouteSegmentClick);

        // --- HUD Bindings mit Währungsformatierung ---
        balanceLabel.textProperty().bind(Bindings.createStringBinding(
                () -> formatCurrency(world.getEconomy().getBalance()),
                world.getEconomy().balanceProperty()
        ));
        satisfactionLabel.textProperty().bind(
                world.getSatisfaction().valueProperty().asString("%.0f%%")
        );
        netWorthLabel.textProperty().bind(Bindings.createStringBinding(
                () -> formatCurrency(world.getEconomy().getNetWorth()),
                world.getEconomy().netWorthProperty()
        ));

        // €/s Anzeige: throttled auf 2× pro Sekunde (EMA aus GameLoop)
        world.getEconomy().incomeRateProperty().addListener((obs, oldV, newV) -> {
            long now = System.currentTimeMillis();
            if (now - lastRateUiUpdate < 500) return;
            lastRateUiUpdate = now;
            updateIncomeRateLabel(newV.doubleValue());
        });
        // --- Routen-Liste: Selektion → Karte Highlight ---
        routeListView.getSelectionModel().selectedItemProperty().addListener((obs, old, r) -> {
            gameView.setFocusedRoute(r);
            gameView.render();
        });

        // --- Routen-Liste: erweitertes Layout ---
        routeListView.setItems(world.getRoutes());
        routeListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                updateItem(getItem(), isEmpty());
            }

            @Override
            protected void updateItem(Route r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                    return;
                }

                VBox box = new VBox(3);
                box.setStyle("-fx-background-color: transparent;");

                String circIcon = r.isCircular() ? " ↺" : "";
                String status = r.isActive() ? "" : "  [inaktiv]";
                Label header = new Label("● " + r + circIcon + status);
                header.setStyle("-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold;");
                box.getChildren().add(header);

                List<Station> stops = r.getStops();
                for (int i = 0; i < stops.size(); i++) {
                    String prefix = (i == stops.size() - 1) ? "  └ " : "  ├ ";
                    Label sl = new Label(prefix + stops.get(i).getName());
                    sl.setStyle("-fx-text-fill: #90a4ae; -fx-font-size: 11;");
                    box.getChildren().add(sl);
                }
                if (r.isCircular() && !stops.isEmpty()) {
                    Label cl = new Label("  ↺ → " + stops.getFirst().getName());
                    cl.setStyle("-fx-text-fill: #78909c; -fx-font-size: 10;");
                    box.getChildren().add(cl);
                }

                for (Train t : r.getTrains()) {
                    String dir = t.isForward() ? "→" : "←";
                    Label tl = new Label("  🚇 " + t.getType().name() + " " + dir);
                    tl.setStyle("-fx-text-fill: #80cbc4; -fx-font-size: 11;");
                    box.getChildren().add(tl);
                }

                setGraphic(box);
                setText(null);
                boolean sel = isSelected();
                String bgHex = r.getColorHex() + (sel ? "44" : "22");
                String border = sel ? " -fx-border-color: transparent transparent transparent #6366f1;"
                                      + " -fx-border-width: 0 0 0 3;" : "";
                setStyle("-fx-background-color: " + bgHex + "; -fx-padding: 6 4 6 4;" + border);
            }
        });

        // --- Züge-Liste: Selektion → Karte Highlight ---
        trainListView.getSelectionModel().selectedItemProperty().addListener((obs, old, t) -> {
            gameView.setSelectedTrain(t);
            gameView.render();
        });

        // --- Züge-Liste: erweitertes Layout ---
        trainListView.setItems(world.getTrains());
        trainListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                updateItem(getItem(), isEmpty());
            }

            @Override
            protected void updateItem(Train t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                    return;
                }

                String icon = switch (t.getType()) {
                    case STANDARD -> "🚃";
                    case MEDIUM   -> "🚆";
                    case SUPER    -> "🚄";
                    case DELUXE   -> "🚅";
                };
                VBox box = new VBox(3);
                box.setStyle("-fx-background-color: transparent;");

                Label typeLabel = new Label(icon + "  " + t.getType().name()
                        + "  –  " + t.getType().capacity() + " Pl.");
                typeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold;");

                boolean hasRoute = t.getRoute() != null;
                String routeTxt = hasRoute ? "  Route: " + t.getRoute() : "  ⚠ Keine Route zugewiesen";
                Label routeLabel = new Label(routeTxt);
                routeLabel.setStyle("-fx-text-fill: " + (hasRoute ? "#90a4ae" : "#ff7043")
                        + "; -fx-font-size: 11;");

                box.getChildren().addAll(typeLabel, routeLabel);
                setGraphic(box);
                setText(null);
                boolean sel = isSelected();
                String bg = hasRoute
                        ? t.getRoute().getColorHex() + (sel ? "44" : "22")
                        : (sel ? "#3d4060" : "#2a2d38");
                String border = sel ? " -fx-border-color: transparent transparent transparent #6366f1;"
                                      + " -fx-border-width: 0 0 0 3;" : "";
                setStyle("-fx-background-color: " + bg + "; -fx-padding: 6 4 6 4;" + border);
            }
        });

        initToastSystem();

        canvasContainer.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                scene.setOnKeyPressed(e -> {
                    switch (e.getCode()) {
                        case SPACE -> togglePause();
                        case ESCAPE -> cancelBuildMode();
                        case DELETE -> deleteSelected();
                        case S -> {
                            if (e.isControlDown()) saveGame();
                            else activateBuildStation();
                        }
                        case T -> activateBuildTrack();
                        case R -> onNewRoute();
                    }
                });
            }
        });

        gameView.setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            if (gameView.popLastClickConsumed()) return;
            double wx = gameView.toWorldX(e.getX());
            double wy = gameView.toWorldY(e.getY());
            handleCanvasClick(wx, wy);
        });

        gameLoop = new GameLoop(world, gameView);
        gameLoop.setOnNewStation(s -> {
            if (world.getCurrentSave() != null) {
                int dbId = stationDao.insert(world.getCurrentSave().getId(), s);
                s.setId(dbId);
            }
            showToast("Neuer Stadtteil: " + s.getName() + " fordert Anbindung!", true);
        });
        gameLoop.start();

        // Initial satisfaction color
        updateSatisfactionColor(world.getSatisfaction().getValue());
        world.getSatisfaction().valueProperty().addListener((obs, o, n) ->
            updateSatisfactionColor(n.doubleValue())
        );
    }

    // ─── Track-Interaktion ────────────────────────────────────────────────────

    private void handleTrackDemolish(Track track) {
        if (buildMode != BuildMode.NONE) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Strecke abreißen");
        confirm.setHeaderText("Strecke abreißen?");
        confirm.setContentText(
                "Strecke " + track.getFrom().getName() + " ↔ " + track.getTo().getName() +
                        " abreißen?\n\nKein Geld wird erstattet.\n" +
                        "Unternehmenswert sinkt um " + formatCurrency(TRACK_BUILD_COST()) + "."
        );
        confirm.showAndWait()
                .filter(r -> r == ButtonType.OK)
                .ifPresent(r -> {
                    world.getTracks().remove(track);
                    world.getEconomy().addNetWorth(-TRACK_BUILD_COST());
                    if (world.getCurrentSave() != null) trackDao.delete(track.getId());
                    showToast("Strecke abgerissen.", false);
                    gameView.render();
                });
    }

    private void handleStationDemolish(Station station) {
        // Verbundene Strecken zählen für Info-Text
        long trackCount = world.getTracks().stream()
                .filter(t -> t.getFrom() == station || t.getTo() == station).count();
        String extra = trackCount > 0
                ? "\n" + trackCount + " verbundene Strecke(n) werden ebenfalls entfernt." : "";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Station abreißen");
        confirm.setHeaderText("Station \"" + station.getName() + "\" abreißen?");
        confirm.setContentText(
                "Kein Geld wird erstattet.\n" +
                "Unternehmenswert sinkt um " + formatCurrency(STATION_BUILD_COST()) + "." + extra
        );
        confirm.showAndWait()
                .filter(r -> r == ButtonType.OK)
                .ifPresent(r -> {
                    // Wartende Passagiere entfernen
                    world.getPassengers().removeAll(station.getWaitingPassengers());
                    station.getWaitingPassengers().clear();

                    // Station aus allen Routen entfernen, Zug-Indizes anpassen
                    for (Route route : world.getRoutes()) {
                        int removeIdx = route.getStops().indexOf(station);
                        if (removeIdx >= 0) {
                            route.getStops().remove(station);
                            adjustTrainsAfterStopRemove(route, removeIdx);
                        }
                    }

                    // Verbundene Strecken entfernen
                    List<Track> toRemove = world.getTracks().stream()
                            .filter(t -> t.getFrom() == station || t.getTo() == station)
                            .toList();
                    for (Track t : toRemove) {
                        world.getTracks().remove(t);
                        if (world.getCurrentSave() != null) trackDao.delete(t.getId());
                    }

                    world.getStations().remove(station);
                    world.getEconomy().addNetWorth(-STATION_BUILD_COST());
                    if (world.getCurrentSave() != null) stationDao.delete(station.getId());

                    routeListView.refresh();
                    trainListView.refresh();
                    showToast("Station abgerissen.", false);
                    gameView.render();
                });
    }

    private void handleRouteSegmentClick(Route route, Station nearest, Station other) {
        if (buildMode != BuildMode.NONE) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Verbindung trennen");
        confirm.setHeaderText("Stop aus Route entfernen?");
        confirm.setContentText(
                "Station \"" + nearest.getName() + "\" aus Route entfernen?\n" +
                        "Die Verbindung zu \"" + other.getName() + "\" wird getrennt."
        );
        confirm.showAndWait()
                .filter(r -> r == ButtonType.OK)
                .ifPresent(r -> {
                    int removeIdx = route.getStops().indexOf(nearest);
                    route.getStops().remove(nearest);
                    adjustTrainsAfterStopRemove(route, removeIdx);
                    routeListView.refresh();
                    gameView.render();
                    showToast("Verbindung getrennt.", false);
                });
    }

    private void handleRouteSegmentRedirect(Route route, Station stopA, Station stopB,
                                            int insertAfterIndex, Station newStation) {
        if (buildMode != BuildMode.NONE) return;

        List<Station> stops = route.getStops();

        // Ziel bereits in Route → abbrechen
        if (stops.contains(newStation)) {
            showToast("Diese Station ist bereits Teil der Route.", true);
            return;
        }

        // Strecken bauen falls nötig: stopA↔new UND stopB↔new
        boolean needA = !hasTrackBetween(stopA, newStation);
        boolean needB = !hasTrackBetween(stopB, newStation);
        if ((needA || needB) && world.getEconomy().getBalance() < 0) {
            showToast("Im Minus kann keine neue Strecke gebaut werden.", true);
            return;
        }
        boolean builtAny = needA || needB;
        if (needA) buildTrackSilent(stopA, newStation);
        if (needB) buildTrackSilent(stopB, newStation);

        // Station zwischen stopA und stopB einfügen (beide bleiben erhalten)
        stops.add(insertAfterIndex + 1, newStation);

        // Zug-Indizes anpassen – kein Respawn
        adjustTrainsAfterStopInsert(route, insertAfterIndex + 1);

        routeListView.refresh();
        gameView.render();
        showToast("Station eingefügt" + (builtAny ? " – neue Strecken gebaut." : "."), false);
    }

    private boolean hasTrackBetween(Station a, Station b) {
        for (Track t : world.getTracks()) {
            if ((t.getFrom() == a && t.getTo() == b) ||
                    (t.getFrom() == b && t.getTo() == a)) return true;
        }
        return false;
    }

    /**
     * Baut eine Strecke ohne Gleichgewichtsprüfung (intern für Route-Redirect).
     */
    private void buildTrackSilent(Station from, Station to) {
        Track t = new Track(world.nextTrackId(), from, to);
        world.getTracks().add(t);
        world.getEconomy().addBalance(-TRACK_BUILD_COST());
        world.getEconomy().addNetWorth(TRACK_NET_WORTH_GAIN());
        if (world.getCurrentSave() != null) {
            int dbId = trackDao.insert(world.getCurrentSave().getId(), t);
            t.setId(dbId);
        }
    }

    /**
     * Verteilt alle Züge einer Route gleichmäßig auf zufällig versetzte Stops,
     * abwechselnd vor-/rückwärts, um Überlappungen zu vermeiden.
     */
    private void spawnNewTrainOnRoute(Train newTrain, Route route) {
        List<Station> stops = route.getStops();
        if (stops.size() < 2) {
            newTrain.setCurrentStopIndex(0);
            newTrain.setPosition(0.0);
            newTrain.setForward(true);
            return;
        }
        int S = stops.size();
        List<Train> others = route.getTrains().stream()
                .filter(t -> t != newTrain).toList();

        int bestIdx;
        if (others.isEmpty()) {
            bestIdx = rng.nextInt(S);
        } else {
            bestIdx = 0;
            double bestMinDist = -1;
            for (int i = 0; i < S; i++) {
                double minDist = Double.MAX_VALUE;
                for (Train t : others) {
                    int diff = Math.abs(t.getCurrentStopIndex() - i);
                    double d = Math.min(diff, S - diff);
                    if (d < minDist) minDist = d;
                }
                if (minDist > bestMinDist) {
                    bestMinDist = minDist;
                    bestIdx = i;
                }
            }
        }

        boolean fwd = !(bestIdx == S - 1 && !route.isCircular());
        newTrain.setCurrentStopIndex(bestIdx);
        newTrain.setPosition(0.0);
        newTrain.setForward(fwd);
    }

    private void distributeTrainsOnRoute(Route route) {
        List<Train> trains = route.getTrains();
        List<Station> stops = route.getStops();
        if (trains.isEmpty() || stops.size() < 2) return;

        int S = stops.size();
        int N = trains.size();
        int offset = rng.nextInt(S);

        for (int i = 0; i < N; i++) {
            Train t = trains.get(i);
            int idx = (offset + i * Math.max(1, S / N)) % S;
            boolean fwd = (i % 2 == 0);
            if (!route.isCircular()) {
                if (idx == S - 1) fwd = false;
                else if (idx == 0) fwd = true;
            }
            t.setCurrentStopIndex(idx);
            t.setPosition(0.0);
            t.setForward(fwd);
        }
    }

    // ─── BuildMode-Verwaltung ─────────────────────────────────────────────────

    private void setBuildMode(BuildMode mode) {
        buildMode = mode;
        gameView.setTrackInteractionEnabled(mode == BuildMode.NONE);
    }

    // ─── Toast-System ────────────────────────────────────────────────────────

    private void initToastSystem() {
        toastContainer = new VBox(8);
        toastContainer.setPickOnBounds(false);
        toastContainer.setMouseTransparent(true);
        toastContainer.setMaxWidth(320);
        toastContainer.setPadding(new Insets(0, 16, 16, 0));
        StackPane.setAlignment(toastContainer, Pos.BOTTOM_RIGHT);

        toastSpacer = new Region();
        VBox.setVgrow(toastSpacer, Priority.ALWAYS);
        toastContainer.getChildren().add(toastSpacer);

        canvasContainer.getChildren().add(toastContainer);
    }

    private void showToast(String message, boolean isError) {
        Label toast = new Label((isError ? "⚠  " : "ℹ  ") + message);
        toast.setWrapText(true);
        toast.setMaxWidth(290);
        String bg = isError ? "#c62828" : "#0d6efd";
        toast.setStyle(
                "-fx-background-color: " + bg + "; -fx-text-fill: white;" +
                        " -fx-font-size: 13; -fx-padding: 10 16 10 16;" +
                        " -fx-background-radius: 8;" +
                        " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 0, 2);"
        );
        toastContainer.getChildren().add(toast);

        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        pause.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(500), toast);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(f -> toastContainer.getChildren().remove(toast));
            fade.play();
        });
        pause.play();
    }

    // ─── Zug-Shop Overlay ────────────────────────────────────────────────────

    @FXML
    public void onBuyTrain() {
        showTrainShop();
    }

    private void showTrainShop() {
        if (trainShopOverlay != null) return;

        trainShopOverlay = new VBox(28);
        trainShopOverlay.setAlignment(Pos.CENTER);
        trainShopOverlay.setStyle("-fx-background-color: rgba(10,12,22,0.90);");
        trainShopOverlay.setOnMouseClicked(Event::consume);

        Label title = new Label("🚇  Zug kaufen");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 26; -fx-font-weight: bold;");

        HBox cards = new HBox(22);
        cards.setAlignment(Pos.CENTER);
        for (TrainType type : TrainType.values()) {
            cards.getChildren().add(buildTrainCard(type));
        }

        Button closeBtn = new Button("✕  Abbrechen");
        closeBtn.setStyle(
                "-fx-background-color: #37474f; -fx-text-fill: white;" +
                        " -fx-font-size: 13; -fx-cursor: hand; -fx-padding: 9 22 9 22; -fx-background-radius: 8;"
        );
        closeBtn.setOnAction(e -> closeTrainShop());

        trainShopOverlay.getChildren().addAll(title, cards, closeBtn);
        canvasContainer.getChildren().add(trainShopOverlay);
    }

    private VBox buildTrainCard(TrainType type) {
        String icon = switch (type) {
            case STANDARD -> "🚃";
            case MEDIUM   -> "🚆";
            case SUPER    -> "🚄";
            case DELUXE   -> "🚅";
        };
        String baseStyle = "-fx-background-color: #2a2d38; -fx-background-radius: 14;" +
                " -fx-padding: 26 22 26 22;" +
                " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 12, 0, 0, 4);";
        String hoverStyle = "-fx-background-color: #353a4d; -fx-background-radius: 14;" +
                " -fx-padding: 26 22 26 22;" +
                " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 16, 0, 0, 5);";

        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(175);
        card.setStyle(baseStyle);
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 44;");
        Label nameLbl = new Label(type.name());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 17; -fx-font-weight: bold;");
        Label capLbl = new Label(type.capacity() + " Plätze");
        capLbl.setStyle("-fx-text-fill: #90a4ae; -fx-font-size: 13;");
        Label priceLbl = new Label(formatCurrency(type.buyCost()));
        priceLbl.setStyle("-fx-text-fill: #4fc3f7; -fx-font-size: 15; -fx-font-weight: bold;");

        boolean canAfford = world.getEconomy().getBalance() >= type.buyCost();
        Button buyBtn = new Button(canAfford ? "Kaufen" : "Kein Guthaben");
        buyBtn.setMaxWidth(Double.MAX_VALUE);
        buyBtn.setDisable(!canAfford);
        buyBtn.setStyle(
                "-fx-background-color: " + (canAfford ? "#2e7d32" : "#424242") + "; -fx-text-fill: white;" +
                        " -fx-font-size: 13; -fx-padding: 8 0 8 0; -fx-background-radius: 8;" +
                        (canAfford ? " -fx-cursor: hand;" : "")
        );
        buyBtn.setOnAction(e -> {
            closeTrainShop();
            Train train = new Train(world.nextTrainId(), type);
            world.getTrains().add(train);
            world.getEconomy().addBalance(-type.buyCost());
            showToast(type.name() + " gekauft! Im Züge-Tab Route zuweisen.", false);
        });

        card.getChildren().addAll(iconLbl, nameLbl, capLbl, priceLbl, buyBtn);
        return card;
    }

    private void closeTrainShop() {
        if (trainShopOverlay != null) {
            canvasContainer.getChildren().remove(trainShopOverlay);
            trainShopOverlay = null;
        }
    }

    // ─── Satisfaction Color ───────────────────────────────────────────────────

    private void updateSatisfactionColor(double v) {
        String color = v >= 70 ? "#69f0ae" : v >= 40 ? "#ffd54f" : "#ff5252";
        satisfactionLabel.setStyle("-fx-text-fill: " + color + ";");
    }

    // ─── €/s Label Update ─────────────────────────────────────────────────────

    private void updateIncomeRateLabel(double rate) {
        if (Math.abs(rate) < 0.05) {
            incomeRateLabel.setText("±0 €/s");
            incomeRateLabel.getStyleClass().setAll("income-neutral");
        } else if (rate > 0) {
            incomeRateLabel.setText("+" + formatRate(rate) + " €/s");
            incomeRateLabel.getStyleClass().setAll("income-positive");
        } else {
            incomeRateLabel.setText(formatRate(rate) + " €/s");
            incomeRateLabel.getStyleClass().setAll("income-negative");
        }
    }

    private static String formatRate(double rate) {
        double abs = Math.abs(rate);
        String sign = rate < 0 ? "-" : "";
        if (abs >= 1_000) return sign + String.format("%.1fk", abs / 1_000);
        if (abs >= 10) return sign + String.format("%.0f", abs);
        return sign + String.format("%.1f", abs);
    }

    // ─── Währungsformatierung ─────────────────────────────────────────────────

    private static String formatCurrency(double amount) {
        double abs = Math.abs(amount);
        if (abs >= 1_000_000) {
            double v = amount / 1_000_000.0;
            return (v == Math.floor(v))
                    ? String.format("%.0f Mio €", v)
                    : String.format("%.1f Mio €", v);
        } else if (abs >= 1_000) {
            double v = amount / 1_000.0;
            return (v == Math.floor(v))
                    ? String.format("%.0fk €", v)
                    : String.format("%.1fk €", v);
        }
        return String.format("%.2f €", amount);
    }

    // ─── Tutorial ─────────────────────────────────────────────────────────────

    private void setupTutorial() {
        Station s1 = new Station(world.nextStationId(), "Hauptbahnhof", -200, 0);
        Station s2 = new Station(world.nextStationId(), "Stadtzentrum", 200, 0);
        world.getStations().addAll(s1, s2);
        Train startTrain = new Train(world.nextTrainId(), TrainType.STANDARD);
        world.getTrains().add(startTrain);

        tutorialManager = new TutorialManager();
        tutorialOverlay = new TutorialOverlay(tutorialManager, this::advanceTutorial, this::skipTutorial);
        canvasContainer.getChildren().add(tutorialOverlay);

        tutorialTimer = new Timeline(new KeyFrame(Duration.millis(500), e -> checkTutorial()));
        tutorialTimer.setCycleCount(Timeline.INDEFINITE);
        tutorialTimer.play();

        gameView.render();
    }

    private void checkTutorial() {
        if (tutorialManager == null || !tutorialManager.isActive()) return;
        if (tutorialManager.check(world)) {
            tutorialOverlay.refresh();
            // Kurz zeigen dann automatisch weiter
            PauseTransition delay = new PauseTransition(Duration.millis(900));
            delay.setOnFinished(e -> advanceTutorial());
            delay.play();
        }
    }

    private void advanceTutorial() {
        tutorialManager.advance();
        tutorialOverlay.refresh();
        if (!tutorialManager.isActive()) tutorialTimer.stop();
    }

    private void skipTutorial() {
        tutorialManager.skip();
        tutorialOverlay.refresh();
        tutorialTimer.stop();
    }

    // ─── Canvas-Klick ─────────────────────────────────────────────────────────

    private void handleCanvasClick(double wx, double wy) {
        switch (buildMode) {
            case BUILD_STATION -> placeStation(wx, wy);
            case BUILD_TRACK -> selectStationForTrack(wx, wy);
            case BUILD_ROUTE -> addStationToRoute(wx, wy);
            case NONE -> {
                Station clicked = gameView.findStationAt(wx, wy);
                if (clicked != null) handleStationDemolish(clicked);
            }
        }
    }

    private void placeStation(double wx, double wy) {
        if (world.getEconomy().getBalance() < 0) {
            showToast("Im Minus können keine Stationen gebaut werden.", true);
            return;
        }
        String name = askInput("Station benennen", "Name:",
                "Station " + (world.getStations().size() + 1));
        if (name == null || name.isBlank()) return;
        Station s = new Station(world.nextStationId(), name, wx, wy);
        world.getStations().add(s);
        world.getEconomy().addBalance(-STATION_BUILD_COST());
        world.getEconomy().addNetWorth(STATION_NET_WORTH_GAIN());
        if (world.getCurrentSave() != null) {
            int dbId = stationDao.insert(world.getCurrentSave().getId(), s);
            s.setId(dbId);
        }
        gameView.render();
    }

    private void selectStationForTrack(double wx, double wy) {
        Station clicked = gameView.findStationAt(wx, wy);
        if (clicked == null) return;
        if (trackStart == null) {
            trackStart = clicked;
            gameView.setHighlightStation(clicked);
            gameView.render();
        } else {
            if (trackStart != clicked) buildTrack(trackStart, clicked);
            trackStart = null;
            gameView.setHighlightStation(null);
            cancelBuildMode();
        }
    }

    private void buildTrack(Station from, Station to) {
        if (world.getEconomy().getBalance() < 0) {
            showToast("Im Minus können keine Strecken gebaut werden.", true);
            return;
        }
        Track t = new Track(world.nextTrackId(), from, to);
        world.getTracks().add(t);
        world.getEconomy().addBalance(-TRACK_BUILD_COST());
        world.getEconomy().addNetWorth(TRACK_NET_WORTH_GAIN());
        if (world.getCurrentSave() != null) {
            int dbId = trackDao.insert(world.getCurrentSave().getId(), t);
            t.setId(dbId);
        }
        gameView.render();
    }

    private void addStationToRoute(double wx, double wy) {
        if (activeRoute == null) return;
        Station clicked = gameView.findStationAt(wx, wy);
        if (clicked == null) return;

        List<Station> stops = activeRoute.getStops();

        // Erste Station nochmal → Kreis schließen
        if (!stops.isEmpty() && clicked == stops.getFirst() && stops.size() >= 2) {
            if (!activeRoute.isCircular()) {
                activeRoute.setCircular(true);
                routeListView.refresh();
                gameView.render();
                showToast("Route als Kreis geschlossen! ESC zum Fertigstellen.", false);
                setStatus("Kreis-Route aktiv. ESC = Fertig.");
            } else {
                showToast("Route ist bereits als Kreis angelegt.", false);
                cancelBuildMode();
            }
            return;
        }

        // Doppelte Station verhindern
        if (stops.contains(clicked)) {
            showToast("Diese Station ist bereits in der Route.", true);
            return;
        }

        stops.add(clicked);

        if (stops.size() == 2 && !activeRoute.isCircular()) {
            setStatus("Tipp: Erste Station nochmal klicken = Kreis ↺  |  ESC = Fertig");
        }

        routeListView.refresh();
        gameView.render();
    }

    // ─── Toolbar-Aktionen ─────────────────────────────────────────────────────

    @FXML
    public void onPause() {
        togglePause();
    }

    private void togglePause() {
        gameLoop.togglePause();
        pauseButton.setText(gameLoop.isPaused() ? "▶  Weiter" : "⏸  Pause");
    }

    @FXML
    public void onSpeedToggle() {
        int next = gameLoop.getSpeedMultiplier() % 3 + 1;  // 1→2→3→1
        gameLoop.setSpeedMultiplier(next);
        speedButton.setText("▶▶  " + next + "×");
        speedButton.setStyle(next > 1
                ? "-fx-background-color: #7c3aed; -fx-text-fill: white;"
                  + " -fx-font-size: 13; -fx-cursor: hand; -fx-background-radius: 8;"
                  + " -fx-padding: 7 16 7 16;"
                : "");
    }

    @FXML
    public void onBuildStation() {
        if (buildStationBtn.isSelected()) {
            setBuildMode(BuildMode.BUILD_STATION);
            buildTrackBtn.setSelected(false);
            showToast("Klicke auf die Karte, um eine Station zu platzieren.", false);
        } else {
            setBuildMode(BuildMode.NONE);
        }
    }

    @FXML
    public void onBuildTrack() {
        if (buildTrackBtn.isSelected()) {
            setBuildMode(BuildMode.BUILD_TRACK);
            trackStart = null;
            buildStationBtn.setSelected(false);
            showToast("Erste Station anklicken, dann zweite Station anklicken.", false);
        } else {
            setBuildMode(BuildMode.NONE);
        }
    }

    @FXML
    public void onNewRoute() {
        Color color = colorGen.generateRouteColor();
        Route route = new Route(world.nextRouteId(), color);
        world.getRoutes().add(route);
        route.setName("Linie " + world.getRoutes().size());
        activeRoute = route;
        setBuildMode(BuildMode.BUILD_ROUTE);
        buildStationBtn.setSelected(false);
        buildTrackBtn.setSelected(false);
        setStatus("Stationen anklicken → Route aufbauen.  ESC = Fertig.");
        gameView.setActiveRouteHighlight(route);
        showToast("Klicke Stationen an, um die Route aufzubauen. ESC zum Abschließen.", false);
    }

    @FXML
    public void onEditRoute() {
        Route selected = routeListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showToast("Bitte zuerst eine Route in der Liste auswählen.", true);
            return;
        }
        activeRoute = selected;
        setBuildMode(BuildMode.BUILD_ROUTE);
        buildStationBtn.setSelected(false);
        buildTrackBtn.setSelected(false);
        setStatus("Bearbeite: " + selected + "  |  ESC = Fertig.");
        gameView.setActiveRouteHighlight(selected);
    }

    @FXML
    public void onRenameRoute() {
        Route selected = routeListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showToast("Bitte zuerst eine Route auswählen.", true);
            return;
        }
        TextInputDialog dlg = new TextInputDialog(selected.getName());
        dlg.setTitle("Route umbenennen");
        dlg.setHeaderText("Neuer Name:");
        dlg.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                selected.setName(name.strip());
                if (world.getCurrentSave() != null)
                    routeDao.updateName(selected.getId(), name.strip());
                routeListView.refresh();
            }
        });
    }

    @FXML
    public void onToggleRoute() {
        Route selected = routeListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        selected.setActive(!selected.isActive());
        routeListView.refresh();
        gameView.render();
    }

    @FXML
    public void onDeleteRoute() {
        Route selected = routeListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        for (Train t : selected.getTrains()) t.setRoute(null);
        selected.getTrains().clear();
        world.getRoutes().remove(selected);
        if (world.getCurrentSave() != null) routeDao.delete(selected.getId());
        trainListView.refresh();
        gameView.render();
    }

    @FXML
    public void onAssignTrain() {
        Train selected = trainListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (world.getRoutes().isEmpty()) {
            showToast("Bitte zuerst eine Route erstellen.", true);
            return;
        }
        ChoiceDialog<Route> dlg = new ChoiceDialog<>(world.getRoutes().getFirst(), world.getRoutes());
        dlg.setTitle("Route zuweisen");
        dlg.setHeaderText("Route für diesen Zug:");
        dlg.showAndWait().ifPresent(route -> {
            if (selected.getRoute() != null) selected.getRoute().getTrains().remove(selected);
            selected.setRoute(route);
            route.getTrains().add(selected);
            spawnNewTrainOnRoute(selected, route);
            trainListView.refresh();
            routeListView.refresh();
        });
    }

    @FXML
    public void onUnassignTrain() {
        Train selected = trainListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (selected.getRoute() != null) {
            selected.getRoute().getTrains().remove(selected);
            selected.setRoute(null);
        }
        trainListView.refresh();
        routeListView.refresh();
    }

    @FXML
    public void onSellTrain() {
        Train selected = trainListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (selected.getRoute() != null) {
            selected.getRoute().getTrains().remove(selected);
            selected.setRoute(null);
        }
        world.getTrains().remove(selected);
        double refund = selected.getType().buyCost() * 0.5;
        world.getEconomy().addBalance(refund);
        if (world.getCurrentSave() != null) trainDao.delete(selected.getId());
        showToast("Zug verkauft. Rückgabe: " + formatCurrency(refund), false);
    }

    // ─── Hilfsmethoden ───────────────────────────────────────────────────────

    private void activateBuildStation() {
        setBuildMode(BuildMode.BUILD_STATION);
        buildStationBtn.setSelected(true);
        buildTrackBtn.setSelected(false);
    }

    private void activateBuildTrack() {
        setBuildMode(BuildMode.BUILD_TRACK);
        trackStart = null;
        buildTrackBtn.setSelected(true);
        buildStationBtn.setSelected(false);
    }

    private void cancelBuildMode() {
        if (trainShopOverlay != null) {
            closeTrainShop();
            return;
        }
        setBuildMode(BuildMode.NONE);
        activeRoute = null;
        trackStart = null;
        gameView.setHighlightStation(null);
        gameView.setActiveRouteHighlight(null);
        gameView.setFocusedRoute(null);
        gameView.setSelectedTrain(null);
        routeListView.getSelectionModel().clearSelection();
        trainListView.getSelectionModel().clearSelection();
        buildStationBtn.setSelected(false);
        buildTrackBtn.setSelected(false);
        setStatus(null);
        gameView.render();
    }

    private void deleteSelected() {
        // TODO: selection + delete implementieren
    }

    // ─── Öffentliche Einstiegspunkte (aufgerufen von MainController) ─────────

    public void startNewGame(SaveGame save) {
        world.setCurrentSave(save);
        setupTutorial();
    }

    public void loadGame(SaveGame save) {
        world.setCurrentSave(save);
        int id = save.getId();

        List<Station> stations = stationDao.findAll(id);
        world.getStations().addAll(stations);
        Map<Integer, Station> stationMap = stations.stream()
                .collect(Collectors.toMap(Station::getId, s -> s));

        world.getTracks().addAll(trackDao.findAll(id, stationMap));

        List<Route> routes = routeDao.findAll(id, stationMap);
        world.getRoutes().addAll(routes);
        Map<Integer, Route> routeMap = routes.stream()
                .collect(Collectors.toMap(Route::getId, r -> r));

        List<Train> trains = trainDao.findAll(id, routeMap);
        world.getTrains().addAll(trains);
        for (Train t : trains) {
            if (t.getRoute() != null) t.getRoute().getTrains().add(t);
        }
        for (Route r : world.getRoutes()) distributeTrainsOnRoute(r);

        economyDao.load(id, world.getEconomy(), world.getSatisfaction());

        stations.stream().mapToInt(Station::getId).max()
                .ifPresent(max -> world.setNextStationId(max + 1));
        world.getTracks().stream().mapToInt(Track::getId).max()
                .ifPresent(max -> world.setNextTrackId(max + 1));
        routes.stream().mapToInt(Route::getId).max()
                .ifPresent(max -> world.setNextRouteId(max + 1));
        trains.stream().mapToInt(Train::getId).max()
                .ifPresent(max -> world.setNextTrainId(max + 1));

        gameView.render();
    }

    // ─── Speichern ───────────────────────────────────────────────────────────

    private void saveGame() {
        if (world.getCurrentSave() == null) return;
        int id = world.getCurrentSave().getId();

        // Vollständiges Neuschreiben: löschen in Abhängigkeitsreihenfolge, dann re-insert
        trainDao.deleteAllBySaveId(id);
        routeDao.deleteAllBySaveId(id);
        trackDao.deleteAllBySaveId(id);
        stationDao.deleteAllBySaveId(id);

        for (Station s : world.getStations()) { int dbId = stationDao.insert(id, s); s.setId(dbId); }
        for (Track t   : world.getTracks())   { int dbId = trackDao.insert(id, t);   t.setId(dbId); }
        for (Route r   : world.getRoutes())   routeDao.insert(id, r);
        for (Train t   : world.getTrains()) { int dbId = trainDao.insert(id, t); t.setId(dbId); }

        economyDao.save(id, world.getEconomy(), world.getSatisfaction());
        RankingClient.submitScore(
            world.getEconomy().getNetWorth(),
            rank -> showToast("Rang #" + rank + " in der Rangliste! 🏆", false),
            () -> showToast("Rangliste nicht erreichbar – Score nicht übermittelt.", true)
        );
        saveGameDao.updateLastSaved(id);
        showToast("Spielstand gespeichert.", false);
    }

    // ─── Zurück zum Hauptmenü ────────────────────────────────────────────────

    @FXML
    public void onMainMenu() {
        saveGame();
        gameLoop.stop();
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/main.fxml"));
            Scene menuScene = new Scene(loader.load(), 900, 700);
            Stage stage = (Stage) canvasContainer.getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(menuScene);
        } catch (IOException e) {
            showToast("Fehler beim Öffnen des Menüs.", true);
        }
    }

    private void setStatus(String msg) {
        if (msg == null || msg.isBlank()) {
            editStatusLabel.setVisible(false);
            editStatusLabel.setManaged(false);
        } else {
            editStatusLabel.setText(msg);
            editStatusLabel.setVisible(true);
            editStatusLabel.setManaged(true);
        }
    }

    private String askInput(String title, String header, String defaultVal) {
        TextInputDialog dlg = new TextInputDialog(defaultVal);
        dlg.setTitle(title);
        dlg.setHeaderText(header);
        return dlg.showAndWait().orElse(null);
    }

    // ─── Zug-Index-Anpassung bei Route-Änderungen ────────────────────────────

    private void adjustTrainsAfterStopInsert(Route route, int insertedIdx) {
        for (Train t : route.getTrains()) {
            if (t.getCurrentStopIndex() >= insertedIdx) {
                t.setCurrentStopIndex(t.getCurrentStopIndex() + 1);
            }
        }
    }

    private void adjustTrainsAfterStopRemove(Route route, int removedIdx) {
        List<Station> stops = route.getStops(); // bereits ohne den entfernten Stop
        if (stops.size() < 2) return;           // moveTrains() überspringt diese Route ohnehin
        for (Train t : route.getTrains()) {
            int idx = t.getCurrentStopIndex();
            if (idx == removedIdx) {
                t.setCurrentStopIndex(Math.max(0, removedIdx - 1));
                t.setPosition(0.0);
            } else if (idx > removedIdx) {
                t.setCurrentStopIndex(idx - 1);
            }
            if (t.getCurrentStopIndex() >= stops.size()) {
                t.setCurrentStopIndex(stops.size() - 1);
                t.setPosition(0.0);
            }
        }
    }
}
