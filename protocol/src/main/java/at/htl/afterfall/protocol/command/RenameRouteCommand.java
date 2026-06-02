package at.htl.afterfall.protocol.command;

public class RenameRouteCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int    routeId;
    public String name;

    public RenameRouteCommand(String uuid, int routeId, String name) {
        super(uuid);
        this.routeId = routeId;
        this.name    = name;
    }
}
