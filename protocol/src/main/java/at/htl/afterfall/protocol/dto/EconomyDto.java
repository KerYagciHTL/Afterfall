package at.htl.afterfall.protocol.dto;

import java.io.Serializable;

public class EconomyDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public double balance;
    public double netWorth;
    public double ticketPrice;
    public double incomeRate;

    public EconomyDto() {}

    public EconomyDto(double balance, double netWorth, double ticketPrice, double incomeRate) {
        this.balance     = balance;
        this.netWorth    = netWorth;
        this.ticketPrice = ticketPrice;
        this.incomeRate  = incomeRate;
    }
}
