package at.htl.afterfall.model;

import java.time.LocalDateTime;

public class SaveGame {
    private int           id;
    private String        name;
    private LocalDateTime createdAt;
    private LocalDateTime lastSaved;

    public SaveGame(int id, String name, LocalDateTime createdAt, LocalDateTime lastSaved) {
        this.id        = id;
        this.name      = name;
        this.createdAt = createdAt;
        this.lastSaved = lastSaved;
    }

    public int           getId()        { return id; }
    public void          setId(int id)  { this.id = id; }
    public String        getName()      { return name; }
    public void          setName(String n) { this.name = n; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void          setCreatedAt(LocalDateTime dt) { this.createdAt = dt; }
    public LocalDateTime getLastSaved() { return lastSaved; }
    public void          setLastSaved(LocalDateTime dt) { this.lastSaved = dt; }

    @Override
    public String toString() { return name + " (" + lastSaved.toLocalDate() + ")"; }
}
