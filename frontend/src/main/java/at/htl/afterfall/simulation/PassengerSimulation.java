package at.htl.afterfall.simulation;

import at.htl.afterfall.model.*;
import java.util.*;

public class PassengerSimulation {
    private static final double BASE_SPAWN_INTERVAL = 5.0;

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
        double price    = world.getEconomy().getTicketPricePerStop();
        double modifier = Math.max(0.5, 1.5 + sat * 0.5 - Math.max(0, (price - 1.5) / 1.5));
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

            if (next < 0 || next >= stops.size()) {
                train.setForward(!train.isForward());
                next = train.isForward() ? idx + 1 : idx - 1;
                if (next < 0 || next >= stops.size()) continue;
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
        // alight passengers whose next path stop matches this station
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
                // simplified: deliver immediately, earn revenue
                int stops = path.size() - 1;
                world.getEconomy().addBalance(world.getEconomy().getTicketPricePerStop() * stops);
                world.getEconomy().addNetWorth(world.getEconomy().getTicketPricePerStop() * stops);
            }
        }
    }
}
