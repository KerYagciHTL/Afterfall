package at.htl.afterfall.model;

public enum TrainType {
    STANDARD(50,  1.0, 20_000,  0.40),
    MEDIUM  (120, 1.0, 48_000,  0.80),
    SUPER   (300, 1.3, 120_000, 1.60);

    public final int    capacity;
    public final double speedFactor;
    public final double buyCost;
    public final double opCostPerKm;

    TrainType(int capacity, double speedFactor, double buyCost, double opCostPerKm) {
        this.capacity    = capacity;
        this.speedFactor = speedFactor;
        this.buyCost     = buyCost;
        this.opCostPerKm = opCostPerKm;
    }
}
