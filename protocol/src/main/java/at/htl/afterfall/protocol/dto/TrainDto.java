package at.htl.afterfall.protocol.dto;

import at.htl.afterfall.protocol.TrainType;

import java.io.Serializable;

public class TrainDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public int       id;
    public TrainType type;
    public int       routeId;
    public int       currentStopIndex;
    public double    position;
    public boolean   forward;
    public boolean   active;
    public int       passengerCount;

    public TrainDto() {}

    public TrainDto(int id, TrainType type, int routeId, int currentStopIndex,
                    double position, boolean forward, boolean active,
                    int passengerCount) {
        this.id               = id;
        this.type             = type;
        this.routeId          = routeId;
        this.currentStopIndex = currentStopIndex;
        this.position         = position;
        this.forward          = forward;
        this.active           = active;
        this.passengerCount   = passengerCount;
    }
}
