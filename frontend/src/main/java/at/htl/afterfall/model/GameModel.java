package at.htl.afterfall.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class GameModel {

    private final DoubleProperty balance = new SimpleDoubleProperty(10_000);
    private final DoubleProperty netWorth = new SimpleDoubleProperty(10_000);
    private final DoubleProperty satisfaction = new SimpleDoubleProperty(50);

    public DoubleProperty balanceProperty()     { return balance; }
    public DoubleProperty netWorthProperty()    { return netWorth; }
    public DoubleProperty satisfactionProperty(){ return satisfaction; }
}
