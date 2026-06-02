package at.htl.afterfall.protocol.command;

public class SetTicketPriceCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public double price;

    public SetTicketPriceCommand(String uuid, double price) {
        super(uuid);
        this.price = price;
    }
}
