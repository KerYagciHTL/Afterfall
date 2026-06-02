package at.htl.afterfall.protocol.command;

public class CreateRouteCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public String colorHex;

    public CreateRouteCommand(String uuid, String colorHex) {
        super(uuid);
        this.colorHex = colorHex;
    }
}
