package at.htl.afterfall.persistence;

import at.htl.afterfall.model.Route;
import at.htl.afterfall.model.Station;
import javafx.scene.paint.Color;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteDao {
    public List<Route> findAll(int saveId, Map<Integer, Station> stationMap) {
        List<Route> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("SELECT * FROM routes WHERE save_id = ?")) {
            ps.setInt(1, saveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Route route = new Route(rs.getInt("id"), Color.web(rs.getString("color_hex")));
                    route.setActive(rs.getInt("active") == 1);
                    route.setCircular(rs.getInt("circular") == 1);
                    route.setName(rs.getString("name"));
                    loadStops(route, stationMap);
                    list.add(route);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private void loadStops(Route route, Map<Integer, Station> stationMap) throws SQLException {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "SELECT station_id FROM route_stops WHERE route_id = ? ORDER BY position")) {
            ps.setInt(1, route.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Station s = stationMap.get(rs.getInt("station_id"));
                    if (s != null) route.getStops().add(s);
                }
            }
        }
    }

    public void deleteAllBySaveId(int saveId) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("DELETE FROM routes WHERE save_id = ?")) {
            ps.setInt(1, saveId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int insert(int saveId, Route route) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "INSERT INTO routes (save_id, color_hex, active, circular, name) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, saveId);
            ps.setString(2, route.getColorHex());
            ps.setInt(3, route.isActive() ? 1 : 0);
            ps.setInt(4, route.isCircular() ? 1 : 0);
            ps.setString(5, route.getName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);
                route.setId(id);
                saveStops(route);
                return id;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveStops(Route route) throws SQLException {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "INSERT INTO route_stops (route_id, station_id, position) VALUES (?, ?, ?)")) {
            for (int i = 0; i < route.getStops().size(); i++) {
                ps.setInt(1, route.getId());
                ps.setInt(2, route.getStops().get(i).getId());
                ps.setInt(3, i);
                ps.executeUpdate();
            }
        }
    }

    public void delete(int routeId) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("DELETE FROM routes WHERE id = ?")) {
            ps.setInt(1, routeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateActive(int routeId, boolean active) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("UPDATE routes SET active = ? WHERE id = ?")) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, routeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateName(int routeId, String name) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("UPDATE routes SET name = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setInt(2, routeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
