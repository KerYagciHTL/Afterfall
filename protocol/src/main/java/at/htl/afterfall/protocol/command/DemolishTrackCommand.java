package at.htl.afterfall.protocol.command;

public class DemolishTrackCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int trackId;

    public DemolishTrackCommand(String uuid, int trackId) {
        super(uuid);
        this.trackId = trackId;
    }
}
