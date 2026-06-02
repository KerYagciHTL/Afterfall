package at.htl.afterfall.protocol.command;

public class ListSavesCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public ListSavesCommand(String uuid) {
        super(uuid);
    }
}
