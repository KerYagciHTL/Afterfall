package at.htl.afterfall.protocol.command;

public class PauseCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public boolean paused;

    public PauseCommand(String uuid, boolean paused) {
        super(uuid);
        this.paused = paused;
    }
}
