package at.htl.afterfall.protocol.command;

public class AssignTrainCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int trainId;
    public int routeId;

    public AssignTrainCommand(String uuid, int trainId, int routeId) {
        super(uuid);
        this.trainId = trainId;
        this.routeId = routeId;
    }
}
