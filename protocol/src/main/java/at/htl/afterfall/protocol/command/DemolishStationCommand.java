package at.htl.afterfall.protocol.command;

public class DemolishStationCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int stationId;

    public DemolishStationCommand(String uuid, int stationId) {
        super(uuid);
        this.stationId = stationId;
    }
}
