package at.htl.afterfall.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import java.util.ArrayList;
import java.util.List;

public class Train {
    private int           id;
    private TrainType     type;
    private Route         route;
    private final BooleanProperty active = new SimpleBooleanProperty(true);
    private double  position         = 0.0;
    private int     currentStopIndex = 0;
    private boolean forward          = true;
    private final List<Passenger> onboardPassengers = new ArrayList<>();

    public Train(int id, TrainType type) {
        this.id   = id;
        this.type = type;
    }

    public int          getId()       { return id; }
    public void         setId(int id) { this.id = id; }
    public TrainType    getType()     { return type; }
    public Route        getRoute()    { return route; }
    public void         setRoute(Route r) { this.route = r; }

    public BooleanProperty activeProperty()       { return active; }
    public boolean         isActive()             { return active.get(); }
    public void            setActive(boolean v)   { active.set(v); }

    public double  getPosition()             { return position; }
    public void    setPosition(double p)     { this.position = p; }
    public int     getCurrentStopIndex()     { return currentStopIndex; }
    public void    setCurrentStopIndex(int i){ this.currentStopIndex = i; }
    public boolean         isForward()               { return forward; }
    public void            setForward(boolean f)     { this.forward = f; }
    public List<Passenger> getOnboardPassengers()    { return onboardPassengers; }
    public int             getOnboardCount()         { return onboardPassengers.size(); }
}
