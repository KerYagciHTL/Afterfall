package at.htl.afterfall.game.model;

import java.util.ArrayList;
import java.util.List;

public class ServerPassenger {
    private int                 id;
    private ServerStation       origin;
    private ServerStation       destination;
    private List<ServerStation> path      = new ArrayList<>();
    private long                spawnTime = System.currentTimeMillis();
    private double              fare      = 0.0;

    public ServerPassenger(int id, ServerStation origin, ServerStation destination) {
        this.id          = id;
        this.origin      = origin;
        this.destination = destination;
    }

    public int                 getId()          { return id; }
    public ServerStation       getOrigin()      { return origin; }
    public ServerStation       getDestination() { return destination; }
    public List<ServerStation> getPath()        { return path; }
    public void                setPath(List<ServerStation> p) { this.path = p; }
    public long                getSpawnTime()   { return spawnTime; }
    public long                getWaitTimeMs()  { return System.currentTimeMillis() - spawnTime; }
    public double              getFare()        { return fare; }
    public void                setFare(double f){ this.fare = f; }
}
