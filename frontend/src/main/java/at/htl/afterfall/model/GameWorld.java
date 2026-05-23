package at.htl.afterfall.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GameWorld {
    private final ObservableList<Station>   stations    = FXCollections.observableArrayList();
    private final ObservableList<Track>     tracks      = FXCollections.observableArrayList();
    private final ObservableList<Train>     trains      = FXCollections.observableArrayList();
    private final ObservableList<Route>     routes      = FXCollections.observableArrayList();
    private final ObservableList<Passenger> passengers  = FXCollections.observableArrayList();
    private final Economy                   economy     = new Economy();
    private final Satisfaction              satisfaction = new Satisfaction();
    private SaveGame currentSave;

    private int nextStationId   = 1;
    private int nextTrackId     = 1;
    private int nextTrainId     = 1;
    private int nextRouteId     = 1;
    private int nextPassengerId = 1;

    public ObservableList<Station>   getStations()    { return stations; }
    public ObservableList<Track>     getTracks()      { return tracks; }
    public ObservableList<Train>     getTrains()      { return trains; }
    public ObservableList<Route>     getRoutes()      { return routes; }
    public ObservableList<Passenger> getPassengers()  { return passengers; }
    public Economy                   getEconomy()     { return economy; }
    public Satisfaction              getSatisfaction() { return satisfaction; }
    public SaveGame                  getCurrentSave() { return currentSave; }
    public void                      setCurrentSave(SaveGame sg) { this.currentSave = sg; }

    public int nextStationId()   { return nextStationId++; }
    public int nextTrackId()     { return nextTrackId++; }
    public int nextTrainId()     { return nextTrainId++; }
    public int nextRouteId()     { return nextRouteId++; }
    public int nextPassengerId() { return nextPassengerId++; }

    public void setNextStationId(int v)   { nextStationId   = v; }
    public void setNextTrackId(int v)     { nextTrackId     = v; }
    public void setNextTrainId(int v)     { nextTrainId     = v; }
    public void setNextRouteId(int v)     { nextRouteId     = v; }
    public void setNextPassengerId(int v) { nextPassengerId = v; }
}
