package at.htl.afterfall.persistence;

import at.htl.afterfall.model.Station;
import at.htl.afterfall.model.Track;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrackDao {
    public List<Track> findAll(int saveId, Map<Integer, Station> stationMap) {
        List<Track> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("SELECT * FROM tracks WHERE save_id = ?")) {
            ps.setInt(1, saveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Station from = stationMap.get(rs.getInt("from_station"));
                    Station to   = stationMap.get(rs.getInt("to_station"));
                    if (from != null && to != null) {
                        list.add(new Track(rs.getInt("id"), from, to));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public int insert(int saveId, Track t) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "INSERT INTO tracks (save_id, from_station, to_station, length) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, saveId);
            ps.setInt(2, t.getFrom().getId());
            ps.setInt(3, t.getTo().getId());
            ps.setDouble(4, t.getLength());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int trackId) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("DELETE FROM tracks WHERE id = ?")) {
            ps.setInt(1, trackId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAllBySaveId(int saveId) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("DELETE FROM tracks WHERE save_id = ?")) {
            ps.setInt(1, saveId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
