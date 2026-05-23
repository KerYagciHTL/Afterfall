package at.htl.afterfall.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Satisfaction {
    private final DoubleProperty value = new SimpleDoubleProperty(50.0);

    public DoubleProperty valueProperty() { return value; }
    public double getValue()              { return value.get(); }
    public void   setValue(double v)      { value.set(Math.max(0, Math.min(100, v))); }
}
