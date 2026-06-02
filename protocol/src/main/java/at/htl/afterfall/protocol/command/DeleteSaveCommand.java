package at.htl.afterfall.protocol.command;

public class DeleteSaveCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public int saveId;

    public DeleteSaveCommand(String uuid, int saveId) {
        super(uuid);
        this.saveId = saveId;
    }
}
