package at.htl.afterfall.protocol.dto;

import java.io.Serializable;

public class StationDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public int    id;
    public String name;
    public double x;
    public double y;
    public int    waitingCount;

    public StationDto() {}

    public StationDto(int id, String name, double x, double y, int waitingCount) {
        this.id           = id;
        this.name         = name;
        this.x            = x;
        this.y            = y;
        this.waitingCount = waitingCount;
    }
}
