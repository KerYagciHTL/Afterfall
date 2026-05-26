package at.htl.afterfall.simulation;

import at.htl.afterfall.model.*;

public class SatisfactionEngine {
    private static final double MAX_WAIT = 45_000.0; // 45s bis max Wartestrafe
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

        int total = world.getStations().size();
        int connected = 0;
        int demandingUnmet = 0;
        for (Station s : world.getStations()) {
            boolean conn = isConnected(s);
            if (conn) {
                connected++;
                if (s.isDemandingConnection()) s.setDemandingConnection(false);
            } else if (s.isDemandingConnection()) {
                demandingUnmet++;
            }
        }
        double connFactor = total > 0 ? (double) connected / total : 0;

        double avgWait = 0;
        int totalWaiting = 0;
        var passengers = world.getPassengers();
        if (!passengers.isEmpty()) {
            long sum = 0;
            for (Passenger p : passengers) sum += p.getWaitTimeMs();
            avgWait = (double) sum / passengers.size();
            totalWaiting = passengers.size();
        }
        double waitFactor = Math.min(avgWait / MAX_WAIT, 1.0);

        int totalOnboard = 0;
        for (Train t : world.getTrains()) totalOnboard += t.getOnboardCount();
        double deliveryBonus = (totalOnboard + totalWaiting) > 0
                               ? (double) totalOnboard / (totalOnboard + totalWaiting)
                               : 0;

        int totalRoutes = world.getRoutes().size();
        int empty = 0;
        for (Route r : world.getRoutes()) {
            if (r.getTrains().isEmpty() && r.getStops().size() > 1) empty++;
        }
        double emptyFactor = totalRoutes > 0 ? (double) empty / totalRoutes : 0;

        double target = 55 + 25 * connFactor + 20 * deliveryBonus - 25 * waitFactor - 10 * emptyFactor;
        double current = sat.getValue();
        double diff = target - current;
        double rate = diff > 0 ? 0.025 : 0.008;
        sat.setValue(current + diff * rate * accum);
    }

    private boolean isConnected(Station s) {
        for (Route r : world.getRoutes()) {
            if (r.isActive() && r.getStops().contains(s)) return true;
        }
        return false;
    }
}
