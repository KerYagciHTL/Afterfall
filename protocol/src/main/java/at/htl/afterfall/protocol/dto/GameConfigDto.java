package at.htl.afterfall.protocol.dto;

import java.io.Serializable;

public class GameConfigDto implements Serializable {
    private static final long serialVersionUID = 1L;

    // Economy
    public double stationBuildCost;
    public double stationNetWorthGain;
    public double trackBuildCost;
    public double trackNetWorthGain;

    // Passenger / Trains
    public double trainBaseSpeed;
    public double farePerPx;
    public double minFare;
    public double baseSpawnInterval;
    public double spawnChanceDivisor;
    public double minSatModifier;

    // Train specs: capacity, speedFactor, buyCost, opCostPerKm
    public double standardCapacity, standardSpeedFactor, standardBuyCost, standardOpCostPerKm;
    public double mediumCapacity,   mediumSpeedFactor,   mediumBuyCost,   mediumOpCostPerKm;
    public double superCapacity,    superSpeedFactor,    superBuyCost,    superOpCostPerKm;
    public double deluxeCapacity,   deluxeSpeedFactor,   deluxeBuyCost,   deluxeOpCostPerKm;

    // City Growth
    public double cityInitialDelay;
    public double cityMinInterval;
    public double cityMaxInterval;
    public double cityMinStationDist;
    public double citySpawnRadiusMin;
    public double citySpawnRadiusMax;

    // Satisfaction
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

    public GameConfigDto() {}
}
