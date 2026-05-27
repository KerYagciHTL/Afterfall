package at.htl.afterfall.simulation;

import at.htl.afterfall.GameConfig;
import at.htl.afterfall.model.*;
import java.util.*;
import java.util.function.Consumer;

public class CityGrowthEngine {
    private static final List<String> NAMES = List.of(
        "Westpark", "Nordbrücke", "Südmarkt", "Hafen", "Flugfeld",
        "Bergdorf", "Seetor", "Altstadt", "Industriepark", "Botanika",
        "Arena", "Messe", "Campus", "Schlossberg", "Lindenplatz",
        "Marktplatz", "Neustadt", "Bahnhof West", "Ostplatz", "Seeblick"
    );

    private final GameWorld       world;
    private final Random          rng       = new Random();
    private Consumer<Station>     onNewStation;
    private double                timer     = 0;
    private double                nextSpawn;
    private int                   nameIndex = 0;

    public CityGrowthEngine(GameWorld world) {
        this.world     = world;
        this.nextSpawn = GameConfig.get().cityInitialDelay;
    }

    public void setOnNewStation(Consumer<Station> cb) { this.onNewStation = cb; }

    public void tick(double delta) {
        timer += delta;
        if (timer < nextSpawn) return;
        GameConfig cfg = GameConfig.get();
        nextSpawn = timer + cfg.cityMinInterval + rng.nextDouble() * (cfg.cityMaxInterval - cfg.cityMinInterval);
        spawnStation();
    }

    private void spawnStation() {
        GameConfig cfg = GameConfig.get();
        double x = 0, y = 0;
        boolean placed = false;
        for (int attempt = 0; attempt < 30; attempt++) {
            double angle  = rng.nextDouble() * 2 * Math.PI;
            double radius = cfg.citySpawnRadiusMin + rng.nextDouble() * (cfg.citySpawnRadiusMax - cfg.citySpawnRadiusMin);
            x = Math.cos(angle) * radius;
            y = Math.sin(angle) * radius;
            if (farFromAll(x, y, cfg.cityMinStationDist)) { placed = true; break; }
        }
        if (!placed) return;

        String  name = NAMES.get(nameIndex % NAMES.size());
        nameIndex++;
        Station s = new Station(world.nextStationId(), name, x, y);
        s.setDemandingConnection(true);
        world.getStations().add(s);
        if (onNewStation != null) onNewStation.accept(s);
    }

    private boolean farFromAll(double x, double y, double minDist) {
        for (Station s : world.getStations()) {
            if (Math.hypot(s.getX() - x, s.getY() - y) < minDist) return false;
        }
        return true;
    }
}
