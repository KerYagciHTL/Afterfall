package at.htl.afterfall.protocol.response;

import at.htl.afterfall.protocol.dto.*;

import java.io.Serializable;
import java.util.List;

public class GameStateSnapshot extends GameResponse {
    private static final long serialVersionUID = 1L;

    public List<StationDto> stations;
    public List<TrackDto>   tracks;
    public List<RouteDto>   routes;
    public List<TrainDto>   trains;
    public EconomyDto       economy;
    public double           satisfaction;

    public GameStateSnapshot() {}

    public GameStateSnapshot(List<StationDto> stations, List<TrackDto> tracks,
                             List<RouteDto> routes, List<TrainDto> trains,
                             EconomyDto economy, double satisfaction) {
        this.stations     = stations;
        this.tracks       = tracks;
        this.routes       = routes;
        this.trains       = trains;
        this.economy      = economy;
        this.satisfaction = satisfaction;
    }
}
