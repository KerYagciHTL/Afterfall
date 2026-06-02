package at.htl.afterfall.protocol.response;

import at.htl.afterfall.protocol.dto.StationDto;

public class NewStationEvent extends GameResponse {
    private static final long serialVersionUID = 1L;

    public StationDto station;

    public NewStationEvent() {}

    public NewStationEvent(StationDto station) {
        this.station = station;
    }
}
