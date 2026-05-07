package at.htl.afterfall.simulation;

import at.htl.afterfall.model.*;

public class SatisfactionEngine {
    private static final double MAX_WAIT   = 120_000.0;
    private static final double FAIR_PRICE = 1.5;

    private final GameWorld world;

    public SatisfactionEngine(GameWorld world) {
        this.world = world;
    }

    public void tick(double delta) {
        Satisfaction sat = world.getSatisfaction();
        Economy      eco = world.getEconomy();

        int total     = world.getStations().size();
        int connected = (int) world.getStations().stream().filter(this::isConnected).count();

        double avgWait = world.getPassengers().stream()
            .mapToLong(Passenger::getWaitTimeMs).average().orElse(0);

        int totalRoutes   = world.getRoutes().size();
        int crowded       = (int) world.getRoutes().stream().filter(r -> r.getTrains().size() == 0 && r.getStops().size() > 1).count();

        double connFactor  = total > 0 ? (double) connected / total : 0;
        double waitFactor  = Math.min(avgWait / MAX_WAIT, 1.0);
        double priceFactor = Math.min(Math.max(0, (eco.getTicketPricePerStop() - FAIR_PRICE) / FAIR_PRICE), 1.0);
        double crowdFactor = totalRoutes > 0 ? (double) crowded / totalRoutes : 0;

        double target = 50 + 30 * connFactor - 20 * waitFactor - 15 * priceFactor - 15 * crowdFactor;
        sat.setValue(sat.getValue() + (target - sat.getValue()) * 0.01 * delta);
    }

    private boolean isConnected(Station s) {
        return world.getRoutes().stream()
            .filter(Route::isActive)
            .anyMatch(r -> r.getStops().contains(s));
    }
}
