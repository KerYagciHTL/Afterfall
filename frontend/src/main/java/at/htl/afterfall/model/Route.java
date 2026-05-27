package at.htl.afterfall.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class Route {
    private int   id;
    private String name;
    private Color color;
    private final ObservableList<Station> stops  = FXCollections.observableArrayList();
    private final ObservableList<Train>   trains = FXCollections.observableArrayList();
    private final BooleanProperty active   = new SimpleBooleanProperty(true);
    private boolean               circular = false;

    public Route(int id, Color color) {
        this.id    = id;
        this.color = color;
        this.name  = "";
    }

    public int   getId()    { return id; }
    public void  setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void   setName(String n) { this.name = n != null ? n : ""; }
    public Color getColor() { return color; }
    public void  setColor(Color c) { this.color = c; }

    public ObservableList<Station> getStops()  { return stops; }
    public ObservableList<Train>   getTrains() { return trains; }

    public BooleanProperty activeProperty()       { return active; }
    public boolean         isActive()             { return active.get(); }
    public void            setActive(boolean v)   { active.set(v); }

    public boolean isCircular()            { return circular; }
    public void    setCircular(boolean c)  { this.circular = c; }

    public String getColorHex() {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed()   * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue()  * 255));
    }

    @Override
    public String toString() {
        String display = (name != null && !name.isBlank()) ? name : ("Linie " + id);
        return display + " (" + stops.size() + " St.)";
    }
}
