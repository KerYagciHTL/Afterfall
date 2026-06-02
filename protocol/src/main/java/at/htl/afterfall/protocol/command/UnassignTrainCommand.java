package at.htl.afterfall.protocol.command;

public class UnassignTrainCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int trainId;

    public UnassignTrainCommand(String uuid, int trainId) {
        super(uuid);
        this.trainId = trainId;
    }
}
