package at.htl.afterfall.model;

public class Track {
    private int     id;
    private Station from;
    private Station to;
    private double  length;

    public Track(int id, Station from, Station to) {
        this.id     = id;
        this.from   = from;
        this.to     = to;
        this.length = Math.hypot(to.getX() - from.getX(), to.getY() - from.getY());
    }

    public int     getId()     { return id; }
    public void    setId(int id) { this.id = id; }
    public Station getFrom()   { return from; }
    public Station getTo()     { return to; }
    public double  getLength() { return length; }
}
