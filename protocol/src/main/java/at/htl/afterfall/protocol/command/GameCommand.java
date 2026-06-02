package at.htl.afterfall.protocol.command;

import java.io.Serializable;

public abstract class GameCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    public String uuid;

    protected GameCommand(String uuid) {
        this.uuid = uuid;
    }
}
