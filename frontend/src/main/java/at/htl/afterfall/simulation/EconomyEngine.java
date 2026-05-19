package at.htl.afterfall.simulation;

import at.htl.afterfall.model.*;
import java.util.List;

public class EconomyEngine {
    private final GameWorld world;

    public EconomyEngine(GameWorld world) {
        this.world = world;
    }

    public void tick(double delta) {
        Economy eco = world.getEconomy();
        double costs = 0;
        for (Train train : world.getTrains()) {
            if (!train.isActive() || train.getRoute() == null || !train.getRoute().isActive()) continue;
            double km = routeLengthKm(train.getRoute());
            costs += train.getType().opCostPerKm * km * delta;
        }
        eco.addBalance(-costs);
        eco.addNetWorth(-costs);
    }

    private double routeLengthKm(Route route) {
        List<Station> stops = route.getStops();
        double total = 0;
        for (int i = 0; i < stops.size() - 1; i++) {
            total += Math.hypot(
                stops.get(i + 1).getX() - stops.get(i).getX(),
                stops.get(i + 1).getY() - stops.get(i).getY()
            );
        }
        return total / 100.0;
    }
}
