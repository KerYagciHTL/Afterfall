package at.htl.afterfall.game.model;

public class ServerEconomy {
    private double balance;
    private double netWorth;
    private double ticketPrice = 1.0;
    private double incomeRate  = 0.0;

    public ServerEconomy(double balance, double netWorth) {
        this.balance  = balance;
        this.netWorth = netWorth;
    }

    public double getBalance()              { return balance; }
    public void   setBalance(double v)      { this.balance = v; }
    public void   addBalance(double delta)  { this.balance += delta; }

    public double getNetWorth()             { return netWorth; }
    public void   setNetWorth(double v)     { this.netWorth = v; }
    public void   addNetWorth(double delta) { this.netWorth += delta; }

    public double getTicketPrice()          { return ticketPrice; }
    public void   setTicketPrice(double v)  { this.ticketPrice = v; }

    public double getIncomeRate()           { return incomeRate; }
    public void   setIncomeRate(double v)   { this.incomeRate = v; }
}
