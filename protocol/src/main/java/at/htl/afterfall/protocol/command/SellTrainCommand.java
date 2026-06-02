package at.htl.afterfall.protocol.command;

public class SellTrainCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int trainId;

    public SellTrainCommand(String uuid, int trainId) {
        super(uuid);
        this.trainId = trainId;
    }
}
