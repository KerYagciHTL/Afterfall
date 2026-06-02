package at.htl.afterfall.protocol.command;

public class BuildStationCommand extends GameCommand {
    private static final long serialVersionUID = 1L;

    public double x;
    public double y;
    public String name;

    public BuildStationCommand(String uuid, double x, double y, String name) {
        super(uuid);
        this.x    = x;
        this.y    = y;
        this.name = name;
    }
}
