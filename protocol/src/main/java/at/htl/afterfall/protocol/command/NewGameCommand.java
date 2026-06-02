package at.htl.afterfall.protocol.command;

public class NewGameCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public String saveName;

    public NewGameCommand(String uuid, String saveName) {
        super(uuid);
        this.saveName = saveName;
    }
}
