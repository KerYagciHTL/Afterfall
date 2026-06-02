package at.htl.afterfall.protocol.command;

public class AddRouteStopCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int routeId;
    public int trackFromId;
    public int trackToId;

    public AddRouteStopCommand(String uuid, int routeId, int trackFromId, int trackToId) {
        super(uuid);
        this.routeId     = routeId;
        this.trackFromId = trackFromId;
        this.trackToId   = trackToId;
    }
}
