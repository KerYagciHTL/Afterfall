package at.htl.afterfall.simulation;

import at.htl.afterfall.GameConfig;
import at.htl.afterfall.model.*;

public class SatisfactionEngine {
    private final GameWorld world;
    private double timer = 0;

    public SatisfactionEngine(GameWorld world) {
        this.world = world;
    }

    public void tick(double delta) {
        timer += delta;
        GameConfig cfg = GameConfig.get();
        if (timer < cfg.satUpdateInterval) return;
        double accum = timer;
        timer = 0;

        Satisfaction sat = world.getSatisfaction();

        int total = world.getStations().size();
        int connected = 0;
        for (Station s : world.getStations()) {
            boolean conn = isConnected(s);
            if (conn) {
                connected++;
                if (s.isDemandingConnection()) s.setDemandingConnection(false);
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
        double waitFactor = Math.min(avgWait / cfg.satMaxWait, 1.0);

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

        double target  = cfg.satBaseTarget
                       + cfg.satConnWeight     * connFactor
                       + cfg.satDeliveryWeight * deliveryBonus
                       - cfg.satWaitPenalty    * waitFactor
                       - cfg.satEmptyPenalty   * emptyFactor;
        double current = sat.getValue();
        double diff    = target - current;
        double rate    = diff > 0 ? cfg.satRiseRate : cfg.satFallRate;
        sat.setValue(current + diff * rate * accum);
    }

    private boolean isConnected(Station s) {
        for (Route r : world.getRoutes()) {
            if (r.isActive() && r.getStops().contains(s)) return true;
        }
        return false;
    }
}
