package at.htl.afterfall.protocol.command;

import at.htl.afterfall.protocol.TrainType;

public class BuyTrainCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public TrainType type;

    public BuyTrainCommand(String uuid, TrainType type) {
        super(uuid);
        this.type = type;
    }
}
