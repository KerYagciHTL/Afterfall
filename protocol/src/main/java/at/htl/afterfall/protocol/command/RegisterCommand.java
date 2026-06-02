package at.htl.afterfall.protocol.command;

public class RegisterCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public String playerName;

    public RegisterCommand(String uuid, String playerName) {
        super(uuid);
        this.playerName = playerName;
    }
}
