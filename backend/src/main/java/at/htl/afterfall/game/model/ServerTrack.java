package at.htl.afterfall.game.model;

public class ServerTrack {
    private int           id;
    private ServerStation from;
    private ServerStation to;

    public ServerTrack(int id, ServerStation from, ServerStation to) {
        this.id   = id;
        this.from = from;
        this.to   = to;
    }

    public int           getId()   { return id; }
    public void          setId(int id) { this.id = id; }
    public ServerStation getFrom() { return from; }
    public ServerStation getTo()   { return to; }
}
