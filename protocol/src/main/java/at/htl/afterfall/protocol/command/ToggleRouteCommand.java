package at.htl.afterfall.protocol.command;

public class ToggleRouteCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int routeId;

    public ToggleRouteCommand(String uuid, int routeId) {
        super(uuid);
        this.routeId = routeId;
    }
}
