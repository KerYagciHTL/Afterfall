package at.htl.afterfall.simulation;

import at.htl.afterfall.model.*;
import java.util.*;
import java.util.function.Consumer;

public class CityGrowthEngine {
    private static final double INITIAL_DELAY    = 60.0;
    private static final double MIN_INTERVAL     = 50.0;
    private static final double MAX_INTERVAL     = 120.0;
    private static final double MIN_STATION_DIST = 90.0;
    private static final double SPAWN_RADIUS_MIN = 160.0;
    private static final double SPAWN_RADIUS_MAX = 480.0;

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
    private double                nextSpawn = INITIAL_DELAY;
    private int                   nameIndex = 0;

    public CityGrowthEngine(GameWorld world) { this.world = world; }

    public void setOnNewStation(Consumer<Station> cb) { this.onNewStation = cb; }

    public void tick(double delta) {
        timer += delta;
        if (timer < nextSpawn) return;
        nextSpawn = timer + MIN_INTERVAL + rng.nextDouble() * (MAX_INTERVAL - MIN_INTERVAL);
        spawnStation();
    }

    private void spawnStation() {
        double x = 0, y = 0;
        boolean placed = false;
        for (int attempt = 0; attempt < 30; attempt++) {
            double angle  = rng.nextDouble() * 2 * Math.PI;
            double radius = SPAWN_RADIUS_MIN + rng.nextDouble() * (SPAWN_RADIUS_MAX - SPAWN_RADIUS_MIN);
            x = Math.cos(angle) * radius;
            y = Math.sin(angle) * radius;
            if (farFromAll(x, y)) { placed = true; break; }
        }
        if (!placed) return;

        String  name = NAMES.get(nameIndex % NAMES.size());
        nameIndex++;
        Station s = new Station(world.nextStationId(), name, x, y);
        s.setDemandingConnection(true);
        world.getStations().add(s);
        if (onNewStation != null) onNewStation.accept(s);
    }

    private boolean farFromAll(double x, double y) {
        for (Station s : world.getStations()) {
            if (Math.hypot(s.getX() - x, s.getY() - y) < MIN_STATION_DIST) return false;
        }
        return true;
    }
}
