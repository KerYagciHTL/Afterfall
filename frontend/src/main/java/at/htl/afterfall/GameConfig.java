package at.htl.afterfall;

import at.htl.afterfall.model.TrainType;
import at.htl.afterfall.protocol.dto.GameConfigDto;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Map;

public class GameConfig {

    private static volatile GameConfig instance;

    public final String gameServerHost;
    public final int    gameServerPort;

    public double startingBalance;
    public double startingNetWorth;

    public double stationBuildCost;
    public double stationNetWorthGain;
    public double trackBuildCost;
    public double trackNetWorthGain;

    public record TrainSpec(int capacity, double speedFactor, double buyCost, double opCostPerKm) {}
    private final Map<TrainType, TrainSpec> trainSpecs;

    public double baseSpawnInterval;
    public double farePerPx;
    public double minFare;
    public double trainBaseSpeed;
    public double spawnChanceDivisor;
    public double minSatModifier;

    public double cityInitialDelay;
    public double cityMinInterval;
    public double cityMaxInterval;
    public double cityMinStationDist;
    public double citySpawnRadiusMin;
    public double citySpawnRadiusMax;

    public double satInitial;
    public double satMaxWait;
    public double satUpdateInterval;
    public double satBaseTarget;
    public double satConnWeight;
    public double satDeliveryWeight;
    public double satWaitPenalty;
    public double satEmptyPenalty;
    public double satRiseRate;
    public double satFallRate;

    public static GameConfig get() {
        if (instance == null) {
            synchronized (GameConfig.class) {
                if (instance == null) instance = load();
            }
        }
        return instance;
    }

    public TrainSpec trainSpec(TrainType t) { return trainSpecs.get(t); }

    public static void applyFromServer(GameConfigDto dto) {
        GameConfig c = get();
        c.stationBuildCost    = dto.stationBuildCost;
        c.stationNetWorthGain = dto.stationNetWorthGain;
        c.trackBuildCost      = dto.trackBuildCost;
        c.trackNetWorthGain   = dto.trackNetWorthGain;
        c.trainBaseSpeed      = dto.trainBaseSpeed;
        c.farePerPx           = dto.farePerPx;
        c.minFare             = dto.minFare;
        c.baseSpawnInterval   = dto.baseSpawnInterval;
        c.spawnChanceDivisor  = dto.spawnChanceDivisor;
        c.minSatModifier      = dto.minSatModifier;
        c.cityInitialDelay    = dto.cityInitialDelay;
        c.cityMinInterval     = dto.cityMinInterval;
        c.cityMaxInterval     = dto.cityMaxInterval;
        c.cityMinStationDist  = dto.cityMinStationDist;
        c.citySpawnRadiusMin  = dto.citySpawnRadiusMin;
        c.citySpawnRadiusMax  = dto.citySpawnRadiusMax;
        c.satInitial          = dto.satInitial;
        c.satMaxWait          = dto.satMaxWait;
        c.satUpdateInterval   = dto.satUpdateInterval;
        c.satBaseTarget       = dto.satBaseTarget;
        c.satConnWeight       = dto.satConnWeight;
        c.satDeliveryWeight   = dto.satDeliveryWeight;
        c.satWaitPenalty      = dto.satWaitPenalty;
        c.satEmptyPenalty     = dto.satEmptyPenalty;
        c.satRiseRate         = dto.satRiseRate;
        c.satFallRate         = dto.satFallRate;
        c.trainSpecs.put(TrainType.STANDARD, new TrainSpec((int) dto.standardCapacity, dto.standardSpeedFactor, dto.standardBuyCost, dto.standardOpCostPerKm));
        c.trainSpecs.put(TrainType.MEDIUM,   new TrainSpec((int) dto.mediumCapacity,   dto.mediumSpeedFactor,   dto.mediumBuyCost,   dto.mediumOpCostPerKm));
        c.trainSpecs.put(TrainType.SUPER,    new TrainSpec((int) dto.superCapacity,    dto.superSpeedFactor,    dto.superBuyCost,    dto.superOpCostPerKm));
        c.trainSpecs.put(TrainType.DELUXE,   new TrainSpec((int) dto.deluxeCapacity,   dto.deluxeSpeedFactor,   dto.deluxeBuyCost,   dto.deluxeOpCostPerKm));
    }

    private static GameConfig load() {
        try (var in = GameConfig.class.getResourceAsStream("/at/htl/afterfall/config.json")) {
            if (in != null) {
                return new GameConfig(new JSONObject(new String(in.readAllBytes())));
            }
        } catch (Exception e) {
            System.err.println("[GameConfig] Ladefehler: " + e.getMessage() + " — Defaults aktiv.");
        }
        return new GameConfig(new JSONObject());
    }

    private GameConfig(JSONObject cfg) {
        JSONObject gs = cfg.optJSONObject("gameServer");
        gameServerHost = gs != null && gs.has("host") ? gs.getString("host") : "localhost";
        gameServerPort = gs != null && gs.has("port") ? gs.getInt("port") : 9090;

        // Offline-Modus defaults (server mode: overridden via applyFromServer)
        startingBalance     = 100_000;
        startingNetWorth    = 0;

        stationBuildCost    = 8_000;
        stationNetWorthGain = 6_000;
        trackBuildCost      = 2_000;
        trackNetWorthGain   = 1_600;

        trainSpecs = new EnumMap<>(TrainType.class);
        trainSpecs.put(TrainType.STANDARD, new TrainSpec( 50,  1.0,    20_000, 0.40));
        trainSpecs.put(TrainType.MEDIUM,   new TrainSpec(120,  1.0,    48_000, 0.80));
        trainSpecs.put(TrainType.SUPER,    new TrainSpec(300,  1.3,   120_000, 1.60));
        trainSpecs.put(TrainType.DELUXE,   new TrainSpec(600,  1.5, 6_000_000, 3.50));

        baseSpawnInterval  = 0.667;
        farePerPx          = 0.15;
        minFare            = 10.0;
        trainBaseSpeed     = 80.0;
        spawnChanceDivisor = 250.0;
        minSatModifier     = 0.5;

        cityInitialDelay   = 60.0;
        cityMinInterval    = 50.0;
        cityMaxInterval    = 120.0;
        cityMinStationDist = 90.0;
        citySpawnRadiusMin = 160.0;
        citySpawnRadiusMax = 480.0;

        satInitial        = 50.0;
        satMaxWait        = 45_000.0;
        satUpdateInterval = 0.5;
        satBaseTarget     = 55.0;
        satConnWeight     = 25.0;
        satDeliveryWeight = 20.0;
        satWaitPenalty    = 25.0;
        satEmptyPenalty   = 10.0;
        satRiseRate       = 0.025;
        satFallRate       = 0.008;
    }
}
