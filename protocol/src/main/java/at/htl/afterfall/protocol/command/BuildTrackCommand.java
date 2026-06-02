package at.htl.afterfall.protocol.command;

public class BuildTrackCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int fromStationId;
    public int toStationId;

    public BuildTrackCommand(String uuid, int fromStationId, int toStationId) {
        super(uuid);
        this.fromStationId = fromStationId;
        this.toStationId   = toStationId;
    }
}
