package at.htl.afterfall.protocol.command;

public class InsertRouteStopCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int routeId;
    public int stopAId;
    public int stopBId;
    public int insertAfterIndex;
    public int newStationId;

    public InsertRouteStopCommand(String uuid, int routeId, int stopAId, int stopBId,
                                  int insertAfterIndex, int newStationId) {
        super(uuid);
        this.routeId          = routeId;
        this.stopAId          = stopAId;
        this.stopBId          = stopBId;
        this.insertAfterIndex = insertAfterIndex;
        this.newStationId     = newStationId;
    }
}
