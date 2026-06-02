package at.htl.afterfall.protocol.response;

public class CommandResult extends GameResponse {
    private static final long serialVersionUID = 1L;

    public boolean           success;
    public String            errorMessage;
    public GameStateSnapshot state;

    public CommandResult() {}

    public static CommandResult ok(GameStateSnapshot state) {
        CommandResult r = new CommandResult();
        r.success = true;
        r.state   = state;
        return r;
    }

    public static CommandResult error(String message) {
        CommandResult r = new CommandResult();
        r.success      = false;
        r.errorMessage = message;
        return r;
    }
}
