package at.htl.afterfall.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class Route {
    private int   id;
    private Color color;
    private final ObservableList<Station> stops  = FXCollections.observableArrayList();
    private final ObservableList<Train>   trains = FXCollections.observableArrayList();
    private final BooleanProperty active = new SimpleBooleanProperty(true);

    public Route(int id, Color color) {
        this.id    = id;
        this.color = color;
    }

    public int   getId()    { return id; }
    public void  setId(int id) { this.id = id; }
    public Color getColor() { return color; }
    public void  setColor(Color c) { this.color = c; }

    public ObservableList<Station> getStops()  { return stops; }
    public ObservableList<Train>   getTrains() { return trains; }

    public BooleanProperty activeProperty()       { return active; }
    public boolean         isActive()             { return active.get(); }
    public void            setActive(boolean v)   { active.set(v); }

    public String getColorHex() {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed()   * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue()  * 255));
    }

    @Override
    public String toString() {
        return "Linie " + id + " (" + stops.size() + " St.)";
    }
}
