package at.htl.afterfall.protocol.command;

public class LoadGameCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int saveId;

    public LoadGameCommand(String uuid, int saveId) {
        super(uuid);
        this.saveId = saveId;
    }
}
