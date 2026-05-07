package at.htl.afterfall.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Economy {
    private final DoubleProperty balance            = new SimpleDoubleProperty(10_000);
    private final DoubleProperty netWorth           = new SimpleDoubleProperty(10_000);
    private final DoubleProperty ticketPricePerStop = new SimpleDoubleProperty(1.5);

    public DoubleProperty balanceProperty()            { return balance; }
    public DoubleProperty netWorthProperty()           { return netWorth; }
    public DoubleProperty ticketPricePerStopProperty() { return ticketPricePerStop; }

    public double getBalance()            { return balance.get(); }
    public double getNetWorth()           { return netWorth.get(); }
    public double getTicketPricePerStop() { return ticketPricePerStop.get(); }

    public void setBalance(double v)            { balance.set(v); }
    public void setNetWorth(double v)           { netWorth.set(v); }
    public void setTicketPricePerStop(double v) { ticketPricePerStop.set(v); }

    public void addBalance(double delta)  { balance.set(balance.get() + delta); }
    public void addNetWorth(double delta) { netWorth.set(netWorth.get() + delta); }
}
