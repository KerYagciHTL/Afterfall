package at.htl.afterfall.persistence;

import at.htl.afterfall.model.Station;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StationDao {
    public List<Station> findAll(int saveId) {
        List<Station> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("SELECT * FROM stations WHERE save_id = ?")) {
            ps.setInt(1, saveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Station(rs.getInt("id"), rs.getString("name"),
                            rs.getDouble("x"), rs.getDouble("y")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public int insert(int saveId, Station s) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "INSERT INTO stations (save_id, name, x, y) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, saveId);
            ps.setString(2, s.getName());
            ps.setDouble(3, s.getX());
            ps.setDouble(4, s.getY());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int stationId) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("DELETE FROM stations WHERE id = ?")) {
            ps.setInt(1, stationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAllBySaveId(int saveId) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("DELETE FROM stations WHERE save_id = ?")) {
            ps.setInt(1, saveId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
