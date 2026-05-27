package at.htl.afterfall.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Economy {
    private final DoubleProperty balance    = new SimpleDoubleProperty(at.htl.afterfall.GameConfig.get().startingBalance);
    private final DoubleProperty netWorth   = new SimpleDoubleProperty(at.htl.afterfall.GameConfig.get().startingNetWorth);
    private final DoubleProperty incomeRate = new SimpleDoubleProperty(0.0);

    public DoubleProperty balanceProperty()  { return balance; }
    public DoubleProperty netWorthProperty() { return netWorth; }

    public double getBalance()  { return balance.get(); }
    public double getNetWorth() { return netWorth.get(); }

    public void setBalance(double v)  { balance.set(v); }
    public void setNetWorth(double v) { netWorth.set(v); }

    public DoubleProperty incomeRateProperty()    { return incomeRate; }
    public double         getIncomeRate()         { return incomeRate.get(); }
    public void           setIncomeRate(double v) { incomeRate.set(v); }

    public void addBalance(double delta)  { balance.set(balance.get() + delta); }
    public void addNetWorth(double delta) { netWorth.set(netWorth.get() + delta); }
}
