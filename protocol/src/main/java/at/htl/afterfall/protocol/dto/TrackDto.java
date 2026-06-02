package at.htl.afterfall.protocol.dto;

import java.io.Serializable;

public class TrackDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public int fromStationId;
    public int id;
    public int toStationId;

    public TrackDto() {}

    public TrackDto(int id, int fromStationId, int toStationId) {
        this.id            = id;
        this.fromStationId = fromStationId;
        this.toStationId   = toStationId;
    }
}
