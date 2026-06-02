package at.htl.afterfall.protocol.command;

public class QuitGameCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public QuitGameCommand(String uuid) {
        super(uuid);
    }
}
