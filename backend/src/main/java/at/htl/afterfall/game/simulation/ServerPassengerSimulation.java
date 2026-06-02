package at.htl.afterfall.game.simulation;

import at.htl.afterfall.game.GameConfig;
import at.htl.afterfall.game.model.*;

import java.util.*;

public class ServerPassengerSimulation {
    private final ServerGameWorld world;
    private final ServerPathFinder pathFinder;
    private final Random           rng        = new Random();
    private double                 spawnTimer = 0;
    private int                    nextId     = 1;

    public ServerPassengerSimulation(ServerGameWorld world) {
        this.world      = world;
        this.pathFinder = new ServerPathFinder(world);
    }

    public void tick(double delta) {
        moveTrains(delta);
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval()) {
            spawnTimer = 0;
            spawnPassengers();
        }
    }

    private double spawnInterval() {
        GameConfig cfg = GameConfig.get();
        double sat      = world.getSatisfaction() / 100.0;
        double modifier = Math.max(cfg.minSatModifier, 1.0 + sat);
        return cfg.baseSpawnInterval / modifier;
    }

    private void spawnPassengers() {
        List<ServerStation> stations = world.getStations();
        if (stations.size() < 2) return;
        for (ServerStation origin : stations) {
            spawnFrom(origin, stations);
        }
    }

    private void spawnFrom(ServerStation origin, List<ServerStation> allStations) {
        List<ServerStation> reachable = new ArrayList<>();
        for (ServerRoute r : world.getRoutes()) {
            if (!r.isActive() || !r.getStops().contains(origin)) continue;
            for (ServerStation s : r.getStops()) {
                if (s != origin && !reachable.contains(s)) reachable.add(s);
            }
        }

        double avgDist = 0;
        for (ServerStation s : allStations) {
            if (s == origin) continue;
            avgDist += Math.hypot(s.getX() - origin.getX(), s.getY() - origin.getY());
        }
        if (allStations.size() > 1) avgDist /= (allStations.size() - 1);
        double spawnChance = Math.min(avgDist / GameConfig.get().spawnChanceDivisor, 1.0);
        if (rng.nextDouble() > spawnChance) return;

        if (reachable.isEmpty()) {
            List<ServerStation> others = allStations.stream().filter(s -> s != origin).toList();
            if (others.isEmpty()) return;
            ServerStation dest = others.get(rng.nextInt(others.size()));
            ServerPassenger p = new ServerPassenger(nextId++, origin, dest);
            p.setFare(calcFare(List.of(origin, dest)));
            origin.getWaitingPassengers().add(p);
            world.getPassengers().add(p);
            return;
        }

        double[] weights = new double[reachable.size()];
        double totalWeight = 0;
        for (int i = 0; i < reachable.size(); i++) {
            ServerStation s = reachable.get(i);
            double dist = Math.hypot(s.getX() - origin.getX(), s.getY() - origin.getY());
            weights[i] = Math.max(dist, 1.0);
            totalWeight += weights[i];
        }
        double pick = rng.nextDouble() * totalWeight;
        ServerStation dest = reachable.get(reachable.size() - 1);
        for (int i = 0; i < reachable.size(); i++) {
            pick -= weights[i];
            if (pick <= 0) { dest = reachable.get(i); break; }
        }

        List<ServerStation> path = pathFinder.findPath(origin, dest);
        if (path.isEmpty()) return;

        ServerPassenger p = new ServerPassenger(nextId++, origin, dest);
        p.setPath(path);
        p.setFare(calcFare(path));
        origin.getWaitingPassengers().add(p);
        world.getPassengers().add(p);
    }

    private void moveTrains(double delta) {
        for (ServerTrain train : world.getTrains()) {
            if (!train.isActive() || train.getRoute() == null || !train.getRoute().isActive()) continue;
            List<ServerStation> stops = train.getRoute().getStops();
            if (stops.size() < 2) continue;

            double speed = GameConfig.get().trainBaseSpeed
                         * GameConfig.get().trainSpec(train.getType()).speedFactor();
            int idx  = train.getCurrentStopIndex();
            int next = train.isForward() ? idx + 1 : idx - 1;

            if (train.getRoute().isCircular()) {
                if (next < 0)                  next = stops.size() - 1;
                else if (next >= stops.size()) next = 0;
            } else {
                if (next < 0 || next >= stops.size()) {
                    train.setForward(!train.isForward());
                    next = train.isForward() ? idx + 1 : idx - 1;
                    if (next < 0 || next >= stops.size()) continue;
                }
            }

            ServerStation from   = stops.get(idx);
            ServerStation to     = stops.get(next);
            double        segLen = Math.hypot(to.getX() - from.getX(), to.getY() - from.getY());
            if (segLen < 1) { train.setCurrentStopIndex(next); continue; }

            double progress = train.getPosition() + speed * delta / segLen;
            if (progress >= 1.0) {
                train.setPosition(0.0);
                train.setCurrentStopIndex(next);
                boardAndAlightPassengers(train, to);
            } else {
                train.setPosition(progress);
            }
        }
    }

    private void boardAndAlightPassengers(ServerTrain train, ServerStation station) {
        List<ServerPassenger> alighting = new ArrayList<>();
        for (ServerPassenger p : train.getOnboardPassengers()) {
            if (p.getDestination() == station) alighting.add(p);
        }
        for (ServerPassenger p : alighting) {
            train.getOnboardPassengers().remove(p);
            world.getPassengers().remove(p);
            world.getEconomy().addBalance(p.getFare());
            world.getEconomy().addNetWorth(p.getFare());
        }

        int available = GameConfig.get().trainSpec(train.getType()).capacity() - train.getOnboardCount();
        List<ServerPassenger> waiting = new ArrayList<>(station.getWaitingPassengers());
        for (ServerPassenger p : waiting) {
            if (available <= 0) break;
            if (!train.getRoute().getStops().contains(p.getDestination())) continue;
            List<ServerStation> path = p.getPath();
            if (path.isEmpty()) {
                path = pathFinder.findPath(station, p.getDestination());
                if (path.isEmpty()) continue;
                p.setPath(path);
                p.setFare(calcFare(path));
            }
            int idx = path.indexOf(station);
            if (idx >= 0 && idx < path.size() - 1) {
                station.getWaitingPassengers().remove(p);
                train.getOnboardPassengers().add(p);
                available--;
            }
        }
    }

    private double calcFare(List<ServerStation> path) {
        double dist = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            ServerStation a = path.get(i), b = path.get(i + 1);
            dist += Math.hypot(b.getX() - a.getX(), b.getY() - a.getY());
        }
        GameConfig cfg = GameConfig.get();
        return Math.max(cfg.minFare, dist * cfg.farePerPx);
    }
}
