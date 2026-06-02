package at.htl.afterfall.protocol.command;

public class DeleteRouteCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int routeId;

    public DeleteRouteCommand(String uuid, int routeId) {
        super(uuid);
        this.routeId = routeId;
    }
}
