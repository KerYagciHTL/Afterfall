package at.htl.afterfall.game;

import at.htl.afterfall.game.model.*;
import at.htl.afterfall.game.simulation.*;
import at.htl.afterfall.protocol.TrainType;
import at.htl.afterfall.protocol.command.*;
import at.htl.afterfall.protocol.dto.*;
import at.htl.afterfall.protocol.response.*;

import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class GameSession {
    private static final double TICK_RATE      = 20.0;
    private static final double TICK_DELTA     = 1.0 / TICK_RATE;
    private static final double SMOOTH_TIME    = 2.0;
    private static final long   SAVE_INTERVAL  = 60_000;

    private final ServerGameWorld          world;
    private final ServerEconomyEngine      economyEngine;
    private final ServerPassengerSimulation passengerSim;
    private final ServerSatisfactionEngine  satisfactionEngine;
    private final ServerCityGrowthEngine    cityGrowthEngine;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?>             tickFuture;

    private final ObjectOutputStream out;
    private final Random             rng      = new Random();
    private boolean                  paused   = false;
    private int                      speed    = 1;
    private long                     lastSave = 0;
    private Consumer<GameSession>    onQuit;

    public GameSession(ObjectOutputStream out) {
        this.out                = out;
        this.world              = new ServerGameWorld();
        this.economyEngine      = new ServerEconomyEngine(world);
        this.passengerSim       = new ServerPassengerSimulation(world);
        this.satisfactionEngine = new ServerSatisfactionEngine(world);
        this.cityGrowthEngine   = new ServerCityGrowthEngine(world);
    }

    public void setOnQuit(Consumer<GameSession> cb) { this.onQuit = cb; }

    public ServerGameWorld getWorld() { return world; }

    public void startSimulation() {
        tickFuture = scheduler.scheduleAtFixedRate(this::tick, 0, 50, TimeUnit.MILLISECONDS);
    }

    public void stopSimulation() {
        if (tickFuture != null) tickFuture.cancel(false);
    }

    public void shutdown() {
        stopSimulation();
        scheduler.shutdownNow();
    }

    private void tick() {
        if (paused) return;
        double delta = TICK_DELTA * speed;

        double balanceBefore = world.getEconomy().getBalance();
        cityGrowthEngine.tick(delta);
        passengerSim.tick(delta);
        economyEngine.tick(delta);
        satisfactionEngine.tick(delta);

        double netChange   = world.getEconomy().getBalance() - balanceBefore;
        double instantRate = TICK_DELTA > 0 ? netChange / TICK_DELTA : 0;
        double alpha       = TICK_DELTA / (SMOOTH_TIME + TICK_DELTA);
        double smoothed    = world.getEconomy().getIncomeRate() * (1 - alpha) + instantRate * alpha;
        world.getEconomy().setIncomeRate(smoothed);

        sendUpdate();
    }

    private void sendUpdate() {
        try {
            List<GameStateUpdate.TrainPosition> trainPos = new ArrayList<>();
            for (ServerTrain t : world.getTrains()) {
                trainPos.add(new GameStateUpdate.TrainPosition(
                    t.getId(), t.getCurrentStopIndex(), t.getPosition(), t.isForward()));
            }
            List<GameStateUpdate.StationWaiting> waiting = new ArrayList<>();
            for (ServerStation s : world.getStations()) {
                waiting.add(new GameStateUpdate.StationWaiting(
                    s.getId(), s.getWaitingPassengers().size()));
            }
            ServerEconomy eco = world.getEconomy();
            synchronized (out) {
                out.writeObject(new GameStateUpdate(
                    new EconomyDto(eco.getBalance(), eco.getNetWorth(),
                                   eco.getTicketPrice(), eco.getIncomeRate()),
                    world.getSatisfaction(),
                    trainPos,
                    waiting
                ));
                out.reset();
                out.flush();
            }
        } catch (Exception e) {
            stopSimulation();
        }
    }

    public void sendSnapshot() {
        try {
            out.writeObject(buildSnapshot());
            out.reset();
        } catch (Exception e) {
            stopSimulation();
        }
    }

    public GameStateSnapshot buildSnapshot() {
        List<StationDto> stations = world.getStations().stream()
            .map(s -> new StationDto(s.getId(), s.getName(), s.getX(), s.getY(),
                                     s.getWaitingPassengers().size()))
            .toList();
        List<TrackDto> tracks = world.getTracks().stream()
            .map(t -> new TrackDto(t.getId(), t.getFrom().getId(), t.getTo().getId()))
            .toList();
        List<RouteDto> routes = world.getRoutes().stream()
            .map(r -> new RouteDto(
                r.getId(), r.getName(), r.getColorHex(), r.isActive(), r.isCircular(),
                r.getStops().stream().map(ServerStation::getId).toList(),
                r.getTrains().stream().map(ServerTrain::getId).toList()))
            .toList();
        List<TrainDto> trains = world.getTrains().stream()
            .map(t -> new TrainDto(
                t.getId(), t.getType(),
                t.getRoute() != null ? t.getRoute().getId() : -1,
                t.getCurrentStopIndex(), t.getPosition(), t.isForward(), t.isActive()))
            .toList();
        ServerEconomy eco = world.getEconomy();
        return new GameStateSnapshot(stations, tracks, routes, trains,
            new EconomyDto(eco.getBalance(), eco.getNetWorth(),
                           eco.getTicketPrice(), eco.getIncomeRate()),
            world.getSatisfaction());
    }

    // ── Command Handlers ──────────────────────────────────────────────────────

    private CommandResult run(Callable<CommandResult> task) {
        try {
            return scheduler.submit(task).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CommandResult.error("Unterbrochen.");
        } catch (ExecutionException e) {
            return CommandResult.error("Fehler: " + e.getCause().getMessage());
        }
    }

    public CommandResult handleBuildStation(BuildStationCommand cmd) {
        return run(() -> {
            GameConfig cfg = GameConfig.get();
            if (world.getEconomy().getBalance() < 0)
                return CommandResult.error("Im Minus können keine Stationen gebaut werden.");
            ServerStation s = new ServerStation(world.nextStationId(), cmd.name, cmd.x, cmd.y);
            world.getStations().add(s);
            world.getEconomy().addBalance(-cfg.stationBuildCost);
            world.getEconomy().addNetWorth(cfg.stationNetWorthGain);
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleBuildTrack(BuildTrackCommand cmd) {
        return run(() -> {
            GameConfig cfg = GameConfig.get();
            if (world.getEconomy().getBalance() < 0)
                return CommandResult.error("Im Minus können keine Strecken gebaut werden.");
            ServerStation from = world.findStation(cmd.fromStationId);
            ServerStation to   = world.findStation(cmd.toStationId);
            if (from == null || to == null) return CommandResult.error("Station nicht gefunden.");
            ServerTrack t = new ServerTrack(world.nextTrackId(), from, to);
            world.getTracks().add(t);
            world.getEconomy().addBalance(-cfg.trackBuildCost);
            world.getEconomy().addNetWorth(cfg.trackNetWorthGain);
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleDemolishTrack(DemolishTrackCommand cmd) {
        return run(() -> {
            ServerTrack track = world.findTrack(cmd.trackId);
            if (track == null) return CommandResult.error("Strecke nicht gefunden.");
            world.getTracks().remove(track);
            world.getEconomy().addNetWorth(-GameConfig.get().trackBuildCost);
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleDemolishStation(DemolishStationCommand cmd) {
        return run(() -> {
            ServerStation station = world.findStation(cmd.stationId);
            if (station == null) return CommandResult.error("Station nicht gefunden.");

            world.getPassengers().removeAll(station.getWaitingPassengers());
            station.getWaitingPassengers().clear();

            for (ServerRoute route : world.getRoutes()) {
                int idx = route.getStops().indexOf(station);
                if (idx >= 0) {
                    route.getStops().remove(station);
                    adjustTrainsAfterStopRemove(route, idx);
                }
            }
            List<ServerTrack> toRemove = world.getTracks().stream()
                .filter(t -> t.getFrom() == station || t.getTo() == station).toList();
            world.getTracks().removeAll(toRemove);

            world.getStations().remove(station);
            world.getEconomy().addNetWorth(-GameConfig.get().stationBuildCost);
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleBuyTrain(BuyTrainCommand cmd) {
        return run(() -> {
            GameConfig cfg  = GameConfig.get();
            double cost = cfg.trainSpec(cmd.type).buyCost();
            if (world.getEconomy().getBalance() < cost)
                return CommandResult.error("Kein Guthaben.");
            ServerTrain t = new ServerTrain(world.nextTrainId(), cmd.type);
            world.getTrains().add(t);
            world.getEconomy().addBalance(-cost);
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleSellTrain(SellTrainCommand cmd) {
        return run(() -> {
            ServerTrain train = world.findTrain(cmd.trainId);
            if (train == null) return CommandResult.error("Zug nicht gefunden.");
            if (train.getRoute() != null) {
                train.getRoute().getTrains().remove(train);
                train.setRoute(null);
            }
            world.getTrains().remove(train);
            double refund = GameConfig.get().trainSpec(train.getType()).buyCost() * 0.5;
            world.getEconomy().addBalance(refund);
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleAssignTrain(AssignTrainCommand cmd) {
        return run(() -> {
            ServerTrain train = world.findTrain(cmd.trainId);
            ServerRoute route = world.findRoute(cmd.routeId);
            if (train == null || route == null) return CommandResult.error("Zug oder Route nicht gefunden.");
            if (train.getRoute() != null) train.getRoute().getTrains().remove(train);
            train.setRoute(route);
            route.getTrains().add(train);
            spawnOnRoute(train, route);
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleUnassignTrain(UnassignTrainCommand cmd) {
        return run(() -> {
            ServerTrain train = world.findTrain(cmd.trainId);
            if (train == null) return CommandResult.error("Zug nicht gefunden.");
            if (train.getRoute() != null) {
                train.getRoute().getTrains().remove(train);
                train.setRoute(null);
            }
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleCreateRoute(CreateRouteCommand cmd) {
        return run(() -> {
            ServerRoute r = new ServerRoute(world.nextRouteId(), cmd.colorHex);
            r.setName("Linie " + (world.getRoutes().size() + 1));
            world.getRoutes().add(r);
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleAddRouteStop(AddRouteStopCommand cmd) {
        return run(() -> {
            ServerRoute route = world.findRoute(cmd.routeId);
            if (route == null) return CommandResult.error("Route nicht gefunden.");
            ServerStation stA = world.findStation(cmd.trackFromId);
            ServerStation stB = world.findStation(cmd.trackToId);
            if (stA == null || stB == null) return CommandResult.error("Station nicht gefunden.");

            List<ServerStation> stops = route.getStops();
            if (stops.isEmpty()) {
                stops.add(stA);
                stops.add(stB);
                return CommandResult.ok(buildSnapshot());
            }

            ServerStation lastStop = stops.getLast();
            ServerStation nextStop;
            if (stA == lastStop)       nextStop = stB;
            else if (stB == lastStop)  nextStop = stA;
            else return CommandResult.error("Strecke verbindet nicht mit letztem Halt.");

            if (nextStop == stops.getFirst() && stops.size() >= 2) {
                if (!route.isCircular()) route.setCircular(true);
                return CommandResult.ok(buildSnapshot());
            }
            if (stops.contains(nextStop))
                return CommandResult.error("Station bereits in Route.");
            stops.add(nextStop);
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleRemoveRouteStop(RemoveRouteStopCommand cmd) {
        return run(() -> {
            ServerRoute route = world.findRoute(cmd.routeId);
            ServerStation station = world.findStation(cmd.stationId);
            if (route == null || station == null) return CommandResult.error("Nicht gefunden.");
            int idx = route.getStops().indexOf(station);
            if (idx < 0) return CommandResult.error("Station nicht in Route.");
            route.getStops().remove(station);
            adjustTrainsAfterStopRemove(route, idx);
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleInsertRouteStop(InsertRouteStopCommand cmd) {
        return run(() -> {
            GameConfig cfg = GameConfig.get();
            ServerRoute route  = world.findRoute(cmd.routeId);
            ServerStation stopA = world.findStation(cmd.stopAId);
            ServerStation stopB = world.findStation(cmd.stopBId);
            ServerStation newSt = world.findStation(cmd.newStationId);
            if (route == null || stopA == null || stopB == null || newSt == null)
                return CommandResult.error("Nicht gefunden.");
            if (route.getStops().contains(newSt))
                return CommandResult.error("Station bereits in Route.");

            boolean needA = world.findTrackBetween(stopA, newSt) == null;
            boolean needB = world.findTrackBetween(stopB, newSt) == null;
            int needed = (needA ? 1 : 0) + (needB ? 1 : 0);
            double cost = needed * cfg.trackBuildCost;
            if (needed > 0 && world.getEconomy().getBalance() < cost)
                return CommandResult.error("Zu wenig Geld für " + needed + " Strecke(n).");

            if (needA) {
                world.getTracks().add(new ServerTrack(world.nextTrackId(), stopA, newSt));
                world.getEconomy().addBalance(-cfg.trackBuildCost);
                world.getEconomy().addNetWorth(cfg.trackNetWorthGain);
            }
            if (needB) {
                world.getTracks().add(new ServerTrack(world.nextTrackId(), stopB, newSt));
                world.getEconomy().addBalance(-cfg.trackBuildCost);
                world.getEconomy().addNetWorth(cfg.trackNetWorthGain);
            }
            route.getStops().add(cmd.insertAfterIndex + 1, newSt);
            adjustTrainsAfterStopInsert(route, cmd.insertAfterIndex + 1);
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleToggleRoute(ToggleRouteCommand cmd) {
        return run(() -> {
            ServerRoute route = world.findRoute(cmd.routeId);
            if (route == null) return CommandResult.error("Route nicht gefunden.");
            route.setActive(!route.isActive());
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleRenameRoute(RenameRouteCommand cmd) {
        return run(() -> {
            ServerRoute route = world.findRoute(cmd.routeId);
            if (route == null) return CommandResult.error("Route nicht gefunden.");
            route.setName(cmd.name);
            return CommandResult.ok(buildSnapshot());
        });
    }

    public CommandResult handleDeleteRoute(DeleteRouteCommand cmd) {
        return run(() -> {
            ServerRoute route = world.findRoute(cmd.routeId);
            if (route == null) return CommandResult.error("Route nicht gefunden.");
            for (ServerTrain t : route.getTrains()) t.setRoute(null);
            route.getTrains().clear();
            world.getRoutes().remove(route);
            return CommandResult.ok(buildSnapshot());
        });
    }

    public void handlePause(PauseCommand cmd) {
        scheduler.submit(() -> this.paused = cmd.paused);
    }

    public void handleSetSpeed(SetSpeedCommand cmd) {
        scheduler.submit(() -> this.speed = Math.max(1, Math.min(3, cmd.multiplier)));
    }

    public void handleSetTicketPrice(SetTicketPriceCommand cmd) {
        scheduler.submit(() -> world.getEconomy().setTicketPrice(cmd.price));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void spawnOnRoute(ServerTrain train, ServerRoute route) {
        List<ServerStation> stops = route.getStops();
        if (stops.size() < 2) { train.setCurrentStopIndex(0); return; }
        int S = stops.size();
        List<ServerTrain> others = route.getTrains().stream().filter(t -> t != train).toList();
        int bestIdx = others.isEmpty() ? rng.nextInt(S) : findBestSpawnIndex(others, S);
        boolean fwd = !(bestIdx == S - 1 && !route.isCircular());
        train.setCurrentStopIndex(bestIdx);
        train.setPosition(0.0);
        train.setForward(fwd);
    }

    private int findBestSpawnIndex(List<ServerTrain> others, int S) {
        int best = 0;
        double bestMin = -1;
        for (int i = 0; i < S; i++) {
            double minDist = Double.MAX_VALUE;
            for (ServerTrain t : others) {
                int diff = Math.abs(t.getCurrentStopIndex() - i);
                double d = Math.min(diff, S - diff);
                if (d < minDist) minDist = d;
            }
            if (minDist > bestMin) { bestMin = minDist; best = i; }
        }
        return best;
    }

    private void adjustTrainsAfterStopInsert(ServerRoute route, int insertedIdx) {
        for (ServerTrain t : route.getTrains()) {
            if (t.getCurrentStopIndex() >= insertedIdx)
                t.setCurrentStopIndex(t.getCurrentStopIndex() + 1);
        }
    }

    private void adjustTrainsAfterStopRemove(ServerRoute route, int removedIdx) {
        List<ServerStation> stops = route.getStops();
        if (stops.size() < 2) return;
        for (ServerTrain t : route.getTrains()) {
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
