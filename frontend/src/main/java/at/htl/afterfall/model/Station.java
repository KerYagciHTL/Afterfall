package at.htl.afterfall.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Station {
    private int    id;
    private String name;
    private final DoubleProperty x = new SimpleDoubleProperty();
    private final DoubleProperty y = new SimpleDoubleProperty();
    private final ObservableList<Passenger> waitingPassengers = FXCollections.observableArrayList();

    public Station(int id, String name, double x, double y) {
        this.id   = id;
        this.name = name;
        this.x.set(x);
        this.y.set(y);
    }

    public int    getId()    { return id; }
    public void   setId(int id) { this.id = id; }
    public String getName()  { return name; }
    public void   setName(String n) { this.name = n; }

    public DoubleProperty xProperty() { return x; }
    public DoubleProperty yProperty() { return y; }
    public double getX() { return x.get(); }
    public double getY() { return y.get(); }

    public ObservableList<Passenger> getWaitingPassengers() { return waitingPassengers; }

    @Override
    public String toString() { return name; }
}
