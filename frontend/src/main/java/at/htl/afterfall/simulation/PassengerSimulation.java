package at.htl.afterfall.simulation;

import at.htl.afterfall.model.*;
import java.util.*;

public class PassengerSimulation {
    private static final double BASE_SPAWN_INTERVAL = 5.0;
    private static final double FARE_PER_PX         = 0.15;

    private final GameWorld  world;
    private final PathFinder pathFinder;
    private final Random     rng         = new Random();
    private double           spawnTimer  = 0;
    private int              nextId      = 1;

    public PassengerSimulation(GameWorld world) {
        this.world      = world;
        this.pathFinder = new PathFinder(world);
    }

    public void tick(double delta) {
        moveTrains(delta);
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval()) {
            spawnTimer = 0;
            spawnPassenger();
        }
    }

    private double spawnInterval() {
        double sat      = world.getSatisfaction().getValue() / 100.0;
        double modifier = Math.max(0.5, 1.0 + sat);
        return BASE_SPAWN_INTERVAL / modifier;
    }

    private void spawnPassenger() {
        List<Station> stations = world.getStations();
        if (stations.size() < 2) return;
        Station origin = stations.get(rng.nextInt(stations.size()));
        Station dest;
        do { dest = stations.get(rng.nextInt(stations.size())); } while (dest == origin);

        List<Station> path = pathFinder.findPath(origin, dest);
        if (path.isEmpty()) return;

        Passenger p = new Passenger(nextId++, origin, dest);
        p.setPath(path);
        p.setFare(calcFare(path));
        origin.getWaitingPassengers().add(p);
        world.getPassengers().add(p);
    }

    private void moveTrains(double delta) {
        for (Train train : world.getTrains()) {
            if (!train.isActive() || train.getRoute() == null || !train.getRoute().isActive()) continue;
            List<Station> stops = train.getRoute().getStops();
            if (stops.size() < 2) continue;

            double speed = 80.0 * train.getType().speedFactor;
            int    idx   = train.getCurrentStopIndex();
            int    next  = train.isForward() ? idx + 1 : idx - 1;

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

            Station from   = stops.get(idx);
            Station to     = stops.get(next);
            double  segLen = Math.hypot(to.getX() - from.getX(), to.getY() - from.getY());
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

    private void boardAndAlightPassengers(Train train, Station station) {
        List<Passenger> waiting = new ArrayList<>(station.getWaitingPassengers());
        int capacity = train.getType().capacity;
        int boarded  = 0;
        for (Passenger p : waiting) {
            if (boarded >= capacity) break;
            List<Station> path = p.getPath();
            int stationIdx = path.indexOf(station);
            if (stationIdx >= 0 && stationIdx < path.size() - 1) {
                station.getWaitingPassengers().remove(p);
                world.getPassengers().remove(p);
                boarded++;
                world.getEconomy().addBalance(p.getFare());
                world.getEconomy().addNetWorth(p.getFare());
            }
        }
    }

    private double calcFare(List<Station> path) {
        double dist = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Station a = path.get(i), b = path.get(i + 1);
            dist += Math.hypot(b.getX() - a.getX(), b.getY() - a.getY());
        }
        return Math.max(10.0, dist * FARE_PER_PX);
    }
}
