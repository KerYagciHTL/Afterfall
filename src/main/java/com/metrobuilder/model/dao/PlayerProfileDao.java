package com.metrobuilder.model.dao;

import com.metrobuilder.db.DatabaseManager;
import com.metrobuilder.model.PlayerProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PlayerProfileDao {

    /** Ensures the single row exists, then loads and returns it. */
    public PlayerProfile load() {
        try (Connection conn = DatabaseManager.getDataSource().getConnection()) {
            // Guarantee the singleton row
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT OR IGNORE INTO player_profile (id, username, total_playtime_seconds) VALUES (1, 'Player', 0)")) {
                ins.executeUpdate();
            }
            try (PreparedStatement sel = conn.prepareStatement(
                    "SELECT username, total_playtime_seconds FROM player_profile WHERE id = 1");
                 ResultSet rs = sel.executeQuery()) {
                if (rs.next()) {
                    return new PlayerProfile(rs.getString("username"), rs.getLong("total_playtime_seconds"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load player profile", e);
        }
        return new PlayerProfile("Player", 0L); // fallback (should never happen)
    }

    /** Persists the current state of the profile back to the single row. */
    public void save(PlayerProfile profile) {
        try (Connection conn = DatabaseManager.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE player_profile SET username = ?, total_playtime_seconds = ? WHERE id = 1")) {
            ps.setString(1, profile.getUsername());
            ps.setLong(2, profile.getTotalPlaytimeSeconds());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save player profile", e);
        }
    }
}
