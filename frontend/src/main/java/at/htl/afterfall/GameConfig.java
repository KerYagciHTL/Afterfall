package at.htl.afterfall;

import at.htl.afterfall.model.TrainType;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

public class GameConfig {

    private static volatile GameConfig instance;

    // ── Economy ───────────────────────────────────────────────────────────────
    public final double startingBalance;
    public final double startingNetWorth;
    public final double stationBuildCost;
    public final double stationNetWorthGain;
    public final double trackBuildCost;
    public final double trackNetWorthGain;

    // ── Trains ────────────────────────────────────────────────────────────────
    public record TrainSpec(int capacity, double speedFactor, double buyCost, double opCostPerKm) {}
    private final Map<TrainType, TrainSpec> trainSpecs;

    // ── Passenger Simulation ──────────────────────────────────────────────────
    public final double baseSpawnInterval;
    public final double farePerPx;
    public final double minFare;
    public final double trainBaseSpeed;
    public final double spawnChanceDivisor;
    public final double minSatModifier;

    // ── City Growth ───────────────────────────────────────────────────────────
    public final double cityInitialDelay;
    public final double cityMinInterval;
    public final double cityMaxInterval;
    public final double cityMinStationDist;
    public final double citySpawnRadiusMin;
    public final double citySpawnRadiusMax;

    // ── Satisfaction ──────────────────────────────────────────────────────────
    public final double satInitial;
    public final double satMaxWait;
    public final double satUpdateInterval;
    public final double satBaseTarget;
    public final double satConnWeight;
    public final double satDeliveryWeight;
    public final double satWaitPenalty;
    public final double satEmptyPenalty;
    public final double satRiseRate;
    public final double satFallRate;

    // ── API ───────────────────────────────────────────────────────────────────

    public static GameConfig get() {
        if (instance == null) {
            synchronized (GameConfig.class) {
                if (instance == null) instance = load();
            }
        }
        return instance;
    }

    public TrainSpec trainSpec(TrainType t) { return trainSpecs.get(t); }

    // ── Loader ────────────────────────────────────────────────────────────────

    private static GameConfig load() {
        try {
            Path p = Path.of("config.json");
            if (Files.exists(p)) {
                JSONObject cfg = new JSONObject(Files.readString(p));
                System.out.println("[GameConfig] Geladen: " + p.toAbsolutePath());
                return new GameConfig(cfg);
            }
        } catch (Exception e) {
            System.err.println("[GameConfig] Ladefehler: " + e.getMessage() + " — Defaults aktiv.");
        }
        return new GameConfig(new JSONObject());
    }

    private GameConfig(JSONObject cfg) {
        JSONObject eco  = cfg.optJSONObject("economy");
        JSONObject pass = cfg.optJSONObject("passenger");
        JSONObject city = cfg.optJSONObject("cityGrowth");
        JSONObject sat  = cfg.optJSONObject("satisfaction");
        JSONObject tr   = cfg.optJSONObject("trains");

        startingBalance     = d(eco, "startingBalance",     10_000);
        startingNetWorth    = d(eco, "startingNetWorth",    10_000);
        stationBuildCost    = d(eco, "stationBuildCost",     8_000);
        stationNetWorthGain = d(eco, "stationNetWorthGain",  6_000);
        trackBuildCost      = d(eco, "trackBuildCost",       2_000);
        trackNetWorthGain   = d(eco, "trackNetWorthGain",    1_600);

        trainSpecs = new EnumMap<>(TrainType.class);
        trainSpecs.put(TrainType.STANDARD, spec(tr, "STANDARD",  50,  1.0,  20_000, 0.40));
        trainSpecs.put(TrainType.MEDIUM,   spec(tr, "MEDIUM",   120,  1.0,  48_000, 0.80));
        trainSpecs.put(TrainType.SUPER,    spec(tr, "SUPER",    300,  1.3, 120_000, 1.60));

        baseSpawnInterval  = d(pass, "baseSpawnInterval",  0.667);
        farePerPx          = d(pass, "farePerPx",           0.15);
        minFare            = d(pass, "minFare",            10.0);
        trainBaseSpeed     = d(pass, "trainBaseSpeed",     80.0);
        spawnChanceDivisor = d(pass, "spawnChanceDivisor", 250.0);
        minSatModifier     = d(pass, "minSatModifier",      0.5);

        cityInitialDelay   = d(city, "initialDelay",    60.0);
        cityMinInterval    = d(city, "minInterval",     50.0);
        cityMaxInterval    = d(city, "maxInterval",    120.0);
        cityMinStationDist = d(city, "minStationDist",  90.0);
        citySpawnRadiusMin = d(city, "spawnRadiusMin", 160.0);
        citySpawnRadiusMax = d(city, "spawnRadiusMax", 480.0);

        satInitial        = d(sat, "initial",         50.0);
        satMaxWait        = d(sat, "maxWait",      45_000.0);
        satUpdateInterval = d(sat, "updateInterval",   0.5);
        satBaseTarget     = d(sat, "baseTarget",       55.0);
        satConnWeight     = d(sat, "connWeight",       25.0);
        satDeliveryWeight = d(sat, "deliveryWeight",   20.0);
        satWaitPenalty    = d(sat, "waitPenalty",      25.0);
        satEmptyPenalty   = d(sat, "emptyPenalty",     10.0);
        satRiseRate       = d(sat, "riseRate",          0.025);
        satFallRate       = d(sat, "fallRate",          0.008);
    }

    private static double d(JSONObject o, String key, double def) {
        return o != null && o.has(key) ? o.getDouble(key) : def;
    }

    private static TrainSpec spec(JSONObject tr, String name,
                                  int defCap, double defSpeed, double defBuy, double defOp) {
        if (tr == null || !tr.has(name)) return new TrainSpec(defCap, defSpeed, defBuy, defOp);
        JSONObject t = tr.getJSONObject(name);
        return new TrainSpec(
            t.optInt("capacity",       defCap),
            t.optDouble("speedFactor", defSpeed),
            t.optDouble("buyCost",     defBuy),
            t.optDouble("opCostPerKm", defOp)
        );
    }
}
