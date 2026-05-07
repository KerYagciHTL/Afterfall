package at.htl.afterfall.model;

import java.util.ArrayList;
import java.util.List;

public class Passenger {
    private int           id;
    private Station       origin;
    private Station       destination;
    private List<Station> path      = new ArrayList<>();
    private long          spawnTime = System.currentTimeMillis();

    public Passenger(int id, Station origin, Station destination) {
        this.id          = id;
        this.origin      = origin;
        this.destination = destination;
    }

    public int           getId()            { return id; }
    public Station       getOrigin()        { return origin; }
    public Station       getDestination()   { return destination; }
    public List<Station> getPath()          { return path; }
    public void          setPath(List<Station> p) { this.path = p; }
    public long          getSpawnTime()     { return spawnTime; }
    public long          getWaitTimeMs()    { return System.currentTimeMillis() - spawnTime; }
}
