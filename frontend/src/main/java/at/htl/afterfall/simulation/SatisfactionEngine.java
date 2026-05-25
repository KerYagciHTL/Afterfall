package at.htl.afterfall.simulation;

import at.htl.afterfall.model.*;

public class SatisfactionEngine {
    private static final double MAX_WAIT  = 120_000.0;
    private static final double FAIR_PRICE = 1.5;
    private static final double INTERVAL  = 0.5;   // compute 2× / sec statt 60×

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
        Economy      eco = world.getEconomy();

        // Connectivity: clear demandingConnection flag as soon as a station is served
        int total     = world.getStations().size();
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

        // Average passenger wait time
        double avgWait = 0;
        var passengers = world.getPassengers();
        if (!passengers.isEmpty()) {
            long sum = 0;
            for (Passenger p : passengers) sum += p.getWaitTimeMs();
            avgWait = (double) sum / passengers.size();
        }

        // Crowding: active routes with no trains OR where waiting load exceeds total capacity
        int activeRoutes = 0;
        int crowded      = 0;
        for (Route r : world.getRoutes()) {
            if (!r.isActive() || r.getStops().size() < 2) continue;
            activeRoutes++;
            if (r.getTrains().isEmpty()) { crowded++; continue; }
            int cap     = 0;
            for (Train t : r.getTrains()) cap += t.getType().capacity;
            int waiting = 0;
            for (Station s : r.getStops()) waiting += s.getWaitingPassengers().size();
            if (waiting > cap) crowded++;
        }

        double connFactor    = total > 0 ? (double) connected / total : 0;
        double waitFactor    = Math.min(avgWait / MAX_WAIT, 1.0);
        double priceFactor   = Math.min(Math.max(0, (eco.getTicketPricePerStop() - FAIR_PRICE) / FAIR_PRICE), 1.0);
        double crowdFactor   = activeRoutes > 0 ? (double) crowded / activeRoutes : 0;
        double demandPenalty = total > 0 ? (double) demandingUnmet / total * 20 : 0;

        double target = 50 + 30 * connFactor - 20 * waitFactor - 15 * priceFactor - 15 * crowdFactor - demandPenalty;
        sat.setValue(sat.getValue() + (target - sat.getValue()) * 0.01 * accum);
    }

    private boolean isConnected(Station s) {
        for (Route r : world.getRoutes()) {
            if (r.isActive() && r.getStops().contains(s)) return true;
        }
        return false;
    }
}
