package com.metrobuilder.model;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PlayerProfile {
    private final StringProperty username = new SimpleStringProperty(this, "username", "Player");
    private final LongProperty totalPlaytimeSeconds = new SimpleLongProperty(this, "totalPlaytimeSeconds", 0L);

    public PlayerProfile(String username, long totalPlaytimeSeconds) {
        this.username.set(username);
        this.totalPlaytimeSeconds.set(totalPlaytimeSeconds);
    }

    public String getUsername() { return username.get(); }
    public void setUsername(String v) { username.set(v); }
    public StringProperty usernameProperty() { return username; }

    public long getTotalPlaytimeSeconds() { return totalPlaytimeSeconds.get(); }
    public void setTotalPlaytimeSeconds(long v) { totalPlaytimeSeconds.set(v); }
    public LongProperty totalPlaytimeSecondsProperty() { return totalPlaytimeSeconds; }
}
