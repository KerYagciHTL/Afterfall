package at.htl.afterfall.game.model;

import java.util.ArrayList;
import java.util.List;

public class ServerStation {
    private int    id;
    private String name;
    private double x;
    private double y;
    private boolean demandingConnection = false;
    private final List<ServerPassenger> waitingPassengers = new ArrayList<>();

    public ServerStation(int id, String name, double x, double y) {
        this.id   = id;
        this.name = name;
        this.x    = x;
        this.y    = y;
    }

    public int    getId()    { return id; }
    public void   setId(int id) { this.id = id; }
    public String getName()  { return name; }
    public void   setName(String n) { this.name = n; }
    public double getX()     { return x; }
    public double getY()     { return y; }

    public List<ServerPassenger> getWaitingPassengers() { return waitingPassengers; }

    public boolean isDemandingConnection()           { return demandingConnection; }
    public void    setDemandingConnection(boolean v) { this.demandingConnection = v; }
}
