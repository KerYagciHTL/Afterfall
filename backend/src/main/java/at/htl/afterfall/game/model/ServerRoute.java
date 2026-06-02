package at.htl.afterfall.game.model;

import java.util.ArrayList;
import java.util.List;

public class ServerRoute {
    private int    id;
    private String name;
    private String colorHex;
    private boolean active   = true;
    private boolean circular = false;
    private final List<ServerStation> stops  = new ArrayList<>();
    private final List<ServerTrain>   trains = new ArrayList<>();

    public ServerRoute(int id, String colorHex) {
        this.id       = id;
        this.colorHex = colorHex;
        this.name     = "";
    }

    public int    getId()       { return id; }
    public void   setId(int id) { this.id = id; }
    public String getName()     { return name; }
    public void   setName(String n) { this.name = n != null ? n : ""; }
    public String getColorHex() { return colorHex; }

    public List<ServerStation> getStops()  { return stops; }
    public List<ServerTrain>   getTrains() { return trains; }

    public boolean isActive()            { return active; }
    public void    setActive(boolean v)  { this.active = v; }
    public boolean isCircular()          { return circular; }
    public void    setCircular(boolean c){ this.circular = c; }

    @Override
    public String toString() {
        String display = (name != null && !name.isBlank()) ? name : ("Linie " + id);
        return display + " (" + stops.size() + " St.)";
    }
}
