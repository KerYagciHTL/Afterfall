package at.htl.afterfall.protocol.command;

public class SetSpeedCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int multiplier;

    public SetSpeedCommand(String uuid, int multiplier) {
        super(uuid);
        this.multiplier = multiplier;
    }
}
