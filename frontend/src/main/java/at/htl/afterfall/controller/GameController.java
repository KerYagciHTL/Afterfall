package at.htl.afterfall.controller;

import at.htl.afterfall.model.*;
import at.htl.afterfall.persistence.*;
import at.htl.afterfall.simulation.GameLoop;
import at.htl.afterfall.util.ColorGenerator;
import at.htl.afterfall.view.GameView;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class GameController {

    @FXML private Label        balanceLabel;
    @FXML private Label        satisfactionLabel;
    @FXML private Label        netWorthLabel;
    @FXML private Slider       ticketSlider;
    @FXML private Label        ticketPriceLabel;
    @FXML private Button       pauseButton;
    @FXML private ToggleButton buildStationBtn;
    @FXML private ToggleButton buildTrackBtn;
    @FXML private StackPane    canvasContainer;
    @FXML private ListView<Route> routeListView;
    @FXML private ListView<Train> trainListView;
    @FXML private Label           editStatusLabel;

    private GameWorld      world;
    private GameView       gameView;
    private GameLoop       gameLoop;
    private ColorGenerator colorGen;
    private BuildMode      buildMode   = BuildMode.NONE;
    private Station        trackStart;
    private Route          activeRoute;

    private final SaveGameDao saveGameDao = new SaveGameDao();
    private final StationDao  stationDao  = new StationDao();
    private final TrackDao    trackDao    = new TrackDao();
    private final RouteDao    routeDao    = new RouteDao();
    private final TrainDao    trainDao    = new TrainDao();
    private final EconomyDao  economyDao  = new EconomyDao();

    @FXML
    public void initialize() {
        DatabaseManager.initSchema();
        world    = new GameWorld();
        colorGen = new ColorGenerator();
        gameView = new GameView(world);

        gameView.widthProperty().bind(canvasContainer.widthProperty());
        gameView.heightProperty().bind(canvasContainer.heightProperty());
        gameView.widthProperty().addListener((obs, o, n) -> gameView.render());
        gameView.heightProperty().addListener((obs, o, n) -> gameView.render());
        canvasContainer.getChildren().add(gameView);

        balanceLabel.textProperty().bind(
            world.getEconomy().balanceProperty().asString("%.2f €")
        );
        satisfactionLabel.textProperty().bind(
            world.getSatisfaction().valueProperty().asString("%.0f%%")
        );
        netWorthLabel.textProperty().bind(
            world.getEconomy().netWorthProperty().asString("NW: %.0f")
        );
        ticketSlider.valueProperty().bindBidirectional(
            world.getEconomy().ticketPricePerStopProperty()
        );
        ticketPriceLabel.textProperty().bind(
            world.getEconomy().ticketPricePerStopProperty().asString("%.2f €")
        );

        routeListView.setItems(world.getRoutes());
        routeListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Route r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) { setText(null); setStyle(""); return; }
                String status = r.isActive() ? "" : " [inaktiv]";
                setText(r + status);
                setStyle("-fx-background-color: " + r.getColorHex() + "33; -fx-text-fill: white;");
            }
        });

        trainListView.setItems(world.getTrains());
        trainListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Train t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) { setText(null); setStyle(""); return; }
                String route = t.getRoute() != null ? t.getRoute().toString() : "⚠ Keine Route";
                String color = t.getRoute() != null ? t.getRoute().getColorHex() + "44" : "#44444433";
                setText(t.getType().name() + "  →  " + route);
                setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
            }
        });

        setupTutorial();

        canvasContainer.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                scene.setOnKeyPressed(e -> {
                    switch (e.getCode()) {
                        case SPACE  -> togglePause();
                        case ESCAPE -> cancelBuildMode();
                        case DELETE -> deleteSelected();
                        case S      -> { if (e.isControlDown()) saveGame(); else activateBuildStation(); }
                        case T      -> activateBuildTrack();
                    }
                });
            }
        });

        gameView.setOnMouseClicked(e -> {
            if (e.isSecondaryButtonDown()) return;
            double wx = gameView.toWorldX(e.getX());
            double wy = gameView.toWorldY(e.getY());
            handleCanvasClick(wx, wy);
        });

        gameLoop = new GameLoop(world, gameView);
        gameLoop.start();
    }

    private void setupTutorial() {
        Station s1 = new Station(world.nextStationId(), "Hauptbahnhof", -150, 0);
        Station s2 = new Station(world.nextStationId(), "Stadtzentrum",   150, 0);
        world.getStations().addAll(s1, s2);
        Train startTrain = new Train(world.nextTrainId(), TrainType.STANDARD);
        world.getTrains().add(startTrain);
        gameView.render();
    }

    private void handleCanvasClick(double wx, double wy) {
        switch (buildMode) {
            case BUILD_STATION -> placeStation(wx, wy);
            case BUILD_TRACK   -> selectStationForTrack(wx, wy);
            case BUILD_ROUTE   -> addStationToRoute(wx, wy);
            case NONE          -> { /* selection - future */ }
        }
    }

    private void placeStation(double wx, double wy) {
        if (world.getEconomy().getBalance() < 0) {
            alert("Kein Geld", "Im Minus können keine Stationen gebaut werden.");
            return;
        }
        String name = askInput("Station benennen", "Name:", "Station " + (world.getStations().size() + 1));
        if (name == null || name.isBlank()) return;
        Station s = new Station(world.nextStationId(), name, wx, wy);
        world.getStations().add(s);
        world.getEconomy().addBalance(-2_000);
        world.getEconomy().addNetWorth(1_500);
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
            alert("Kein Geld", "Im Minus können keine Strecken gebaut werden.");
            return;
        }
        Track t = new Track(world.nextTrackId(), from, to);
        world.getTracks().add(t);
        world.getEconomy().addBalance(-500);
        world.getEconomy().addNetWorth(400);
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
        activeRoute.getStops().add(clicked);
        routeListView.refresh();
        gameView.render();
    }

    @FXML public void onPause() { togglePause(); }

    private void togglePause() {
        gameLoop.togglePause();
        pauseButton.setText(gameLoop.isPaused() ? "▶ Weiter" : "⏸ Pause");
    }

    @FXML public void onBuildStation() {
        if (buildStationBtn.isSelected()) {
            buildMode = BuildMode.BUILD_STATION;
            buildTrackBtn.setSelected(false);
        } else {
            buildMode = BuildMode.NONE;
        }
    }

    @FXML public void onBuildTrack() {
        if (buildTrackBtn.isSelected()) {
            buildMode  = BuildMode.BUILD_TRACK;
            trackStart = null;
            buildStationBtn.setSelected(false);
        } else {
            buildMode = BuildMode.NONE;
        }
    }

    @FXML public void onNewRoute() {
        Color color = colorGen.generateRouteColor();
        Route route = new Route(world.nextRouteId(), color);
        world.getRoutes().add(route);
        activeRoute = route;
        buildMode   = BuildMode.BUILD_ROUTE;
        buildStationBtn.setSelected(false);
        buildTrackBtn.setSelected(false);
        info("Route erstellen", "Klicke Stationen nacheinander an. ESC zum Abschließen.");
    }

    @FXML public void onBuyTrain() {
        List<String> types = List.of(
            "STANDARD  – 50 Pl. | 5.000 €",
            "MEDIUM   – 120 Pl. | 12.000 €",
            "SUPER    – 300 Pl. | 30.000 €"
        );
        ChoiceDialog<String> dlg = new ChoiceDialog<>(types.get(0), types);
        dlg.setTitle("Zug kaufen");
        dlg.setHeaderText("Zugtyp auswählen:");
        Optional<String> res = dlg.showAndWait();
        res.ifPresent(choice -> {
            TrainType type = choice.startsWith("STANDARD") ? TrainType.STANDARD
                           : choice.startsWith("MEDIUM")   ? TrainType.MEDIUM
                                                           : TrainType.SUPER;
            if (world.getEconomy().getBalance() < type.buyCost) {
                alert("Kein Geld", "Nicht genug Guthaben für diesen Zugtyp.");
                return;
            }
            if (world.getRoutes().isEmpty()) {
                alert("Keine Route", "Erstelle zuerst eine Route.");
                return;
            }
            ChoiceDialog<Route> routeDlg = new ChoiceDialog<>(world.getRoutes().get(0), world.getRoutes());
            routeDlg.setTitle("Route zuweisen");
            routeDlg.setHeaderText("Route für den Zug:");
            routeDlg.showAndWait().ifPresent(route -> {
                Train train = new Train(world.nextTrainId(), type);
                train.setRoute(route);
                route.getTrains().add(train);
                world.getTrains().add(train);
                world.getEconomy().addBalance(-type.buyCost);
                trainListView.refresh();
            });
        });
    }

    @FXML public void onEditRoute() {
        Route selected = routeListView.getSelectionModel().getSelectedItem();
        if (selected == null) { alert("Keine Auswahl", "Zuerst Route in der Liste auswählen."); return; }
        activeRoute = selected;
        buildMode   = BuildMode.BUILD_ROUTE;
        buildStationBtn.setSelected(false);
        buildTrackBtn.setSelected(false);
        editStatusLabel.setText("Bearbeite: " + selected + "\nStationen anklicken zum Erweitern. ESC = Fertig.");
    }

    @FXML public void onToggleRoute() {
        Route selected = routeListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        selected.setActive(!selected.isActive());
        routeListView.refresh();
        gameView.render();
    }

    @FXML public void onDeleteRoute() {
        Route selected = routeListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        // Züge bleiben erhalten, Route wird nur entfernt
        for (Train t : selected.getTrains()) t.setRoute(null);
        selected.getTrains().clear();
        world.getRoutes().remove(selected);
        if (world.getCurrentSave() != null) routeDao.delete(selected.getId());
        trainListView.refresh();
        gameView.render();
    }

    @FXML public void onAssignTrain() {
        Train selected = trainListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (world.getRoutes().isEmpty()) { alert("Keine Routen", "Erstelle zuerst eine Route."); return; }
        ChoiceDialog<Route> dlg = new ChoiceDialog<>(world.getRoutes().get(0), world.getRoutes());
        dlg.setTitle("Route zuweisen");
        dlg.setHeaderText("Route für diesen Zug:");
        dlg.showAndWait().ifPresent(route -> {
            if (selected.getRoute() != null) selected.getRoute().getTrains().remove(selected);
            selected.setRoute(route);
            route.getTrains().add(selected);
            trainListView.refresh();
            routeListView.refresh();
        });
    }

    @FXML public void onUnassignTrain() {
        Train selected = trainListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (selected.getRoute() != null) {
            selected.getRoute().getTrains().remove(selected);
            selected.setRoute(null);
        }
        trainListView.refresh();
        routeListView.refresh();
    }

    @FXML public void onSellTrain() {
        Train selected = trainListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (selected.getRoute() != null) {
            selected.getRoute().getTrains().remove(selected);
            selected.setRoute(null);
        }
        world.getTrains().remove(selected);
        // Rückgabewert = 50% des Kaufpreises
        world.getEconomy().addBalance(selected.getType().buyCost * 0.5);
        if (world.getCurrentSave() != null) trainDao.delete(selected.getId());
    }

    private void activateBuildStation() {
        buildMode = BuildMode.BUILD_STATION;
        buildStationBtn.setSelected(true);
        buildTrackBtn.setSelected(false);
    }

    private void activateBuildTrack() {
        buildMode  = BuildMode.BUILD_TRACK;
        trackStart = null;
        buildTrackBtn.setSelected(true);
        buildStationBtn.setSelected(false);
    }

    private void cancelBuildMode() {
        buildMode   = BuildMode.NONE;
        activeRoute = null;
        trackStart  = null;
        gameView.setHighlightStation(null);
        buildStationBtn.setSelected(false);
        buildTrackBtn.setSelected(false);
        editStatusLabel.setText("");
        gameView.render();
    }

    private void deleteSelected() {
        // TODO: implement selection + delete
    }

    private void saveGame() {
        if (world.getCurrentSave() == null) {
            String name = askInput("Spielstand speichern", "Name:", "Mein Spielstand");
            if (name == null || name.isBlank()) return;
            int id  = saveGameDao.insert(name);
            SaveGame sg = new SaveGame(id, name, LocalDateTime.now(), LocalDateTime.now());
            world.setCurrentSave(sg);
            // persist all stations & tracks for new save
            for (Station s : world.getStations()) {
                int dbId = stationDao.insert(id, s);
                s.setId(dbId);
            }
            for (Track t : world.getTracks()) {
                int dbId = trackDao.insert(id, t);
                t.setId(dbId);
            }
            for (Route r : world.getRoutes()) routeDao.insert(id, r);
            for (Train t : world.getTrains()) trainDao.insert(id, t);
        } else {
            saveGameDao.updateLastSaved(world.getCurrentSave().getId());
        }
        economyDao.save(world.getCurrentSave().getId(), world.getEconomy(), world.getSatisfaction());
    }

    private String askInput(String title, String header, String defaultVal) {
        TextInputDialog dlg = new TextInputDialog(defaultVal);
        dlg.setTitle(title);
        dlg.setHeaderText(header);
        return dlg.showAndWait().orElse(null);
    }

    private void alert(String title, String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private void info(String title, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
