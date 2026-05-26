package at.htl.afterfall.persistence;

import at.htl.afterfall.model.Route;
import at.htl.afterfall.model.Train;
import at.htl.afterfall.model.TrainType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrainDao {
    public List<Train> findAll(int saveId, Map<Integer, Route> routeMap) {
        List<Train> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("SELECT * FROM trains WHERE save_id = ?")) {
            ps.setInt(1, saveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Train t = new Train(rs.getInt("id"), TrainType.valueOf(rs.getString("type")));
                    t.setActive(rs.getInt("active") == 1);
                    int routeId = rs.getInt("route_id");
                    if (!rs.wasNull()) t.setRoute(routeMap.get(routeId));
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public int insert(int saveId, Train t) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "INSERT INTO trains (save_id, type, route_id, active) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, saveId);
            ps.setString(2, t.getType().name());
            if (t.getRoute() != null) ps.setInt(3, t.getRoute().getId());
            else                      ps.setNull(3, Types.INTEGER);
            ps.setInt(4, t.isActive() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int trainId) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("DELETE FROM trains WHERE id = ?")) {
            ps.setInt(1, trainId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAllBySaveId(int saveId) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("DELETE FROM trains WHERE save_id = ?")) {
            ps.setInt(1, saveId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
