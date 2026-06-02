package at.htl.afterfall.game.model;

import at.htl.afterfall.game.GameConfig;

import java.util.ArrayList;
import java.util.List;

public class ServerGameWorld {
    private final List<ServerStation>  stations   = new ArrayList<>();
    private final List<ServerTrack>    tracks     = new ArrayList<>();
    private final List<ServerRoute>    routes     = new ArrayList<>();
    private final List<ServerTrain>    trains     = new ArrayList<>();
    private final List<ServerPassenger> passengers = new ArrayList<>();
    private final ServerEconomy        economy;
    private double                     satisfaction;

    private int nextStationId   = 1;
    private int nextTrackId     = 1;
    private int nextTrainId     = 1;
    private int nextRouteId     = 1;
    private int nextPassengerId = 1;

    public ServerGameWorld() {
        GameConfig cfg = GameConfig.get();
        this.economy      = new ServerEconomy(cfg.startingBalance, cfg.startingNetWorth);
        this.satisfaction = cfg.satInitial;
    }

    public List<ServerStation>   getStations()    { return stations; }
    public List<ServerTrack>     getTracks()      { return tracks; }
    public List<ServerRoute>     getRoutes()      { return routes; }
    public List<ServerTrain>     getTrains()      { return trains; }
    public List<ServerPassenger> getPassengers()  { return passengers; }
    public ServerEconomy         getEconomy()     { return economy; }
    public double                getSatisfaction(){ return satisfaction; }
    public void                  setSatisfaction(double v) { this.satisfaction = v; }

    public int nextStationId()   { return nextStationId++; }
    public int nextTrackId()     { return nextTrackId++; }
    public int nextTrainId()     { return nextTrainId++; }
    public int nextRouteId()     { return nextRouteId++; }
    public int nextPassengerId() { return nextPassengerId++; }

    public void setNextStationId(int v)   { this.nextStationId   = v; }
    public void setNextTrackId(int v)     { this.nextTrackId     = v; }
    public void setNextTrainId(int v)     { this.nextTrainId     = v; }
    public void setNextRouteId(int v)     { this.nextRouteId     = v; }
    public void setNextPassengerId(int v) { this.nextPassengerId = v; }

    public ServerStation findStation(int id) {
        return stations.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    public ServerTrack findTrack(int id) {
        return tracks.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
    }

    public ServerRoute findRoute(int id) {
        return routes.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    public ServerTrain findTrain(int id) {
        return trains.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
    }

    public ServerTrack findTrackBetween(ServerStation a, ServerStation b) {
        return tracks.stream()
            .filter(t -> (t.getFrom() == a && t.getTo() == b)
                      || (t.getFrom() == b && t.getTo() == a))
            .findFirst().orElse(null);
    }
}
