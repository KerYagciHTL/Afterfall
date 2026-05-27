package at.htl.afterfall.model;

import at.htl.afterfall.GameConfig;

public enum TrainType {
    STANDARD, MEDIUM, SUPER, DELUXE;

    public int    capacity()    { return GameConfig.get().trainSpec(this).capacity(); }
    public double speedFactor() { return GameConfig.get().trainSpec(this).speedFactor(); }
    public double buyCost()     { return GameConfig.get().trainSpec(this).buyCost(); }
    public double opCostPerKm() { return GameConfig.get().trainSpec(this).opCostPerKm(); }
}
