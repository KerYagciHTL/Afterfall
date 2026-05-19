package at.htl.afterfall.simulation;

import at.htl.afterfall.model.*;

public class SatisfactionEngine {
    private static final double MAX_WAIT = 20_000.0; // 20s bis max Wartestrafe
    private static final double INTERVAL = 0.5;

    private final GameWorld world;
    private double timer = 0;

    public SatisfactionEngine(GameWorld world) {
        this.world = world;
    }

    public void tick(double delta) {
        timer += delta;
        if (timer < INTERVAL) return;
        double accum = timer;
        timer = 0;

        Satisfaction sat = world.getSatisfaction();

        int total     = world.getStations().size();
        int connected = 0;
        for (Station s : world.getStations()) {
            if (isConnected(s)) connected++;
        }

        double avgWait   = 0;
        int    totalWait = 0;
        var passengers = world.getPassengers();
        if (!passengers.isEmpty()) {
            long sum = 0;
            for (Passenger p : passengers) sum += p.getWaitTimeMs();
            avgWait = (double) sum / passengers.size();
            totalWait = passengers.size();
        }

        int totalOnboard = 0;
        for (Train t : world.getTrains()) totalOnboard += t.getOnboardCount();

        int totalRoutes = world.getRoutes().size();
        int crowded     = 0;
        for (Route r : world.getRoutes()) {
            if (r.getTrains().isEmpty() && r.getStops().size() > 1) crowded++;
        }

        double connFactor    = total > 0 ? (double) connected / total : 0;
        double waitFactor    = Math.min(avgWait / MAX_WAIT, 1.0);
        // Backlog: wie viele warten vs werden gerade transportiert
        double backlogFactor = (totalWait + totalOnboard) > 0
                               ? (double) totalWait / (totalWait + totalOnboard)
                               : 0;
        double crowdFactor   = totalRoutes > 0 ? (double) crowded / totalRoutes : 0;

        double target = 50 + 30 * connFactor - 20 * waitFactor - 20 * backlogFactor - 10 * crowdFactor;
        sat.setValue(sat.getValue() + (target - sat.getValue()) * 0.01 * accum);
    }

    private boolean isConnected(Station s) {
        for (Route r : world.getRoutes()) {
            if (r.isActive() && r.getStops().contains(s)) return true;
        }
        return false;
    }
}
