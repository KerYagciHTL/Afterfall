package at.htl.afterfall.game.simulation;

import at.htl.afterfall.game.GameConfig;
import at.htl.afterfall.game.model.*;

import java.util.List;

public class ServerEconomyEngine {
    private final ServerGameWorld world;

    public ServerEconomyEngine(ServerGameWorld world) {
        this.world = world;
    }

    public void tick(double delta) {
        ServerEconomy eco = world.getEconomy();
        double costs = 0;
        for (ServerTrain train : world.getTrains()) {
            if (!train.isActive() || train.getRoute() == null || !train.getRoute().isActive()) continue;
            double km = routeLengthKm(train.getRoute());
            costs += GameConfig.get().trainSpec(train.getType()).opCostPerKm() * km * delta;
        }
        eco.addBalance(-costs);
        eco.addNetWorth(-costs);
    }

    private double routeLengthKm(ServerRoute route) {
        List<ServerStation> stops = route.getStops();
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
