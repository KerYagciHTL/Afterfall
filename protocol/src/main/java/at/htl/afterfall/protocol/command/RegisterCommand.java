package at.htl.afterfall.protocol.command;

import at.htl.afterfall.protocol.Protocol;

public class RegisterCommand extends GameCommand {
    private static final long serialVersionUID = 2L;

    public String playerName;
    public String protocolVersion;

    public RegisterCommand(String uuid, String playerName) {
        super(uuid);
        this.playerName       = playerName;
        this.protocolVersion  = Protocol.VERSION;
    }
}
