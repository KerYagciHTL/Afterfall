package at.htl.afterfall.game.db;

import at.htl.afterfall.game.model.*;
import at.htl.afterfall.protocol.TrainType;
import at.htl.afterfall.protocol.dto.SaveInfoDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
public class GameSaveRepository {

    private static final int MAX_SAVES = 3;

    private final JdbcTemplate jdbc;

    public GameSaveRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public JdbcTemplate getJdbc() { return jdbc; }

    public void registerPlayer(String uuid, String playerName) {
        jdbc.update("""
                INSERT INTO players (player_name, net_worth, last_updated, uuid)
                VALUES (?, 0, datetime('now'), ?)
                ON CONFLICT(player_name) DO UPDATE SET uuid = excluded.uuid
                """, playerName, uuid);
    }

    public String getPlayerName(String uuid) {
        List<String> result = jdbc.queryForList(
            "SELECT player_name FROM players WHERE uuid = ?", String.class, uuid);
        return result.isEmpty() ? null : result.get(0);
    }

    public void updateRanking(String uuid, double netWorth) {
        jdbc.update("""
                UPDATE players SET
                    net_worth    = MAX(net_worth, ?),
                    last_updated = datetime('now')
                WHERE uuid = ?
                """, netWorth, uuid);
    }

    public void deleteSave(int saveId) {
        deleteWorldData(saveId);
        jdbc.update("DELETE FROM game_saves WHERE id = ?", saveId);
    }

    public List<SaveInfoDto> listSaves(String uuid) {
        return jdbc.query("""
                SELECT id, name, created_at, last_saved FROM game_saves
                WHERE uuid = ? ORDER BY last_saved DESC
                """,
                (rs, row) -> new SaveInfoDto(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("created_at"),
                    rs.getString("last_saved")),
                uuid);
    }

    public int createSave(String uuid, String name) {
        enforceLimit(uuid);

        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO game_saves (uuid, name) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, uuid);
            ps.setString(2, name);
            return ps;
        }, holder);
        return holder.getKey().intValue();
    }

    private void enforceLimit(String uuid) {
        List<Integer> ids = jdbc.queryForList(
            "SELECT id FROM game_saves WHERE uuid = ? ORDER BY last_saved ASC", Integer.class, uuid);
        while (ids.size() >= MAX_SAVES) {
            jdbc.update("DELETE FROM game_saves WHERE id = ?", ids.remove(0));
        }
    }

    public void saveWorld(int saveId, ServerGameWorld world) {
        deleteWorldData(saveId);

        Map<ServerStation, Integer> stationDbIds = new HashMap<>();
        for (ServerStation s : world.getStations()) {
            GeneratedKeyHolder h = new GeneratedKeyHolder();
            jdbc.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO save_stations (save_id, name, x, y) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, saveId);
                ps.setString(2, s.getName());
                ps.setDouble(3, s.getX());
                ps.setDouble(4, s.getY());
                return ps;
            }, h);
            stationDbIds.put(s, h.getKey().intValue());
        }

        for (ServerTrack t : world.getTracks()) {
            Integer fromId = stationDbIds.get(t.getFrom());
            Integer toId   = stationDbIds.get(t.getTo());
            if (fromId == null || toId == null) continue;
            jdbc.update("INSERT INTO save_tracks (save_id, from_id, to_id) VALUES (?, ?, ?)",
                saveId, fromId, toId);
        }

        Map<ServerRoute, Integer> routeDbIds = new HashMap<>();
        for (ServerRoute r : world.getRoutes()) {
            GeneratedKeyHolder h = new GeneratedKeyHolder();
            jdbc.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO save_routes (save_id, name, color_hex, active, circular) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, saveId);
                ps.setString(2, r.getName());
                ps.setString(3, r.getColorHex());
                ps.setInt(4, r.isActive() ? 1 : 0);
                ps.setInt(5, r.isCircular() ? 1 : 0);
                return ps;
            }, h);
            int routeDbId = h.getKey().intValue();
            routeDbIds.put(r, routeDbId);
            for (int i = 0; i < r.getStops().size(); i++) {
                Integer stId = stationDbIds.get(r.getStops().get(i));
                if (stId != null)
                    jdbc.update("INSERT INTO save_route_stops (route_id, station_id, position) VALUES (?,?,?)",
                        routeDbId, stId, i);
            }
        }

        for (ServerTrain t : world.getTrains()) {
            Integer routeDbId = t.getRoute() != null ? routeDbIds.get(t.getRoute()) : null;
            jdbc.update("INSERT INTO save_trains (save_id, type, route_id) VALUES (?,?,?)",
                saveId, t.getType().name(), routeDbId);
        }

        ServerEconomy eco = world.getEconomy();
        jdbc.update("""
                INSERT OR REPLACE INTO save_economy (save_id, balance, net_worth, ticket_price)
                VALUES (?, ?, ?, ?)
                """, saveId, eco.getBalance(), eco.getNetWorth(), eco.getTicketPrice());
        jdbc.update("""
                INSERT OR REPLACE INTO save_satisfaction (save_id, value) VALUES (?, ?)
                """, saveId, world.getSatisfaction());

        jdbc.update("UPDATE game_saves SET last_saved = datetime('now') WHERE id = ?", saveId);
    }

    public boolean loadWorld(int saveId, ServerGameWorld world) {
        List<Map<String, Object>> saveRows = jdbc.queryForList(
            "SELECT id FROM game_saves WHERE id = ?", saveId);
        if (saveRows.isEmpty()) return false;

        Map<Integer, ServerStation> stationMap = new HashMap<>();
        List<Map<String, Object>> stRows = jdbc.queryForList(
            "SELECT id, name, x, y FROM save_stations WHERE save_id = ?", saveId);
        for (Map<String, Object> row : stRows) {
            int id = ((Number) row.get("id")).intValue();
            ServerStation s = new ServerStation(
                world.nextStationId(),
                (String) row.get("name"),
                ((Number) row.get("x")).doubleValue(),
                ((Number) row.get("y")).doubleValue());
            stationMap.put(id, s);
            world.getStations().add(s);
        }

        List<Map<String, Object>> trRows = jdbc.queryForList(
            "SELECT from_id, to_id FROM save_tracks WHERE save_id = ?", saveId);
        for (Map<String, Object> row : trRows) {
            ServerStation from = stationMap.get(((Number) row.get("from_id")).intValue());
            ServerStation to   = stationMap.get(((Number) row.get("to_id")).intValue());
            if (from != null && to != null)
                world.getTracks().add(new ServerTrack(world.nextTrackId(), from, to));
        }

        Map<Integer, ServerRoute> routeMap = new HashMap<>();
        List<Map<String, Object>> rRows = jdbc.queryForList(
            "SELECT id, name, color_hex, active, circular FROM save_routes WHERE save_id = ?", saveId);
        for (Map<String, Object> row : rRows) {
            int dbId = ((Number) row.get("id")).intValue();
            ServerRoute r = new ServerRoute(world.nextRouteId(), (String) row.get("color_hex"));
            r.setName((String) row.get("name"));
            r.setActive(((Number) row.get("active")).intValue() == 1);
            r.setCircular(((Number) row.get("circular")).intValue() == 1);
            routeMap.put(dbId, r);
            world.getRoutes().add(r);

            List<Map<String, Object>> stops = jdbc.queryForList(
                "SELECT station_id FROM save_route_stops WHERE route_id = ? ORDER BY position", dbId);
            for (Map<String, Object> stop : stops) {
                ServerStation s = stationMap.get(((Number) stop.get("station_id")).intValue());
                if (s != null) r.getStops().add(s);
            }
        }

        List<Map<String, Object>> tRows = jdbc.queryForList(
            "SELECT type, route_id FROM save_trains WHERE save_id = ?", saveId);
        for (Map<String, Object> row : tRows) {
            TrainType type = TrainType.valueOf((String) row.get("type"));
            ServerTrain t = new ServerTrain(world.nextTrainId(), type);
            Object routeDbId = row.get("route_id");
            if (routeDbId != null) {
                ServerRoute r = routeMap.get(((Number) routeDbId).intValue());
                if (r != null) { t.setRoute(r); r.getTrains().add(t); }
            }
            world.getTrains().add(t);
        }

        List<Map<String, Object>> ecoRows = jdbc.queryForList(
            "SELECT balance, net_worth, ticket_price FROM save_economy WHERE save_id = ?", saveId);
        if (!ecoRows.isEmpty()) {
            Map<String, Object> r = ecoRows.get(0);
            world.getEconomy().setBalance(((Number) r.get("balance")).doubleValue());
            world.getEconomy().setNetWorth(((Number) r.get("net_worth")).doubleValue());
            world.getEconomy().setTicketPrice(((Number) r.get("ticket_price")).doubleValue());
        }

        List<Map<String, Object>> satRows = jdbc.queryForList(
            "SELECT value FROM save_satisfaction WHERE save_id = ?", saveId);
        if (!satRows.isEmpty())
            world.setSatisfaction(((Number) satRows.get(0).get("value")).doubleValue());

        return true;
    }

    private void deleteWorldData(int saveId) {
        jdbc.update("DELETE FROM save_trains WHERE save_id = ?", saveId);
        jdbc.update("DELETE FROM save_route_stops WHERE route_id IN "
            + "(SELECT id FROM save_routes WHERE save_id = ?)", saveId);
        jdbc.update("DELETE FROM save_routes WHERE save_id = ?", saveId);
        jdbc.update("DELETE FROM save_tracks WHERE save_id = ?", saveId);
        jdbc.update("DELETE FROM save_stations WHERE save_id = ?", saveId);
        jdbc.update("DELETE FROM save_economy WHERE save_id = ?", saveId);
        jdbc.update("DELETE FROM save_satisfaction WHERE save_id = ?", saveId);
    }
}
