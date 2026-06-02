package at.htl.afterfall.protocol.command;

public class RemoveRouteStopCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int routeId;
    public int stationId;

    public RemoveRouteStopCommand(String uuid, int routeId, int stationId) {
        super(uuid);
        this.routeId   = routeId;
        this.stationId = stationId;
    }
}
