package at.htl.afterfall.persistence;

import at.htl.afterfall.model.SaveGame;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaveGameDao {
    public List<SaveGame> findAll() {
        List<SaveGame> list = new ArrayList<>();
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery("SELECT * FROM save_games ORDER BY last_saved DESC")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public int insert(String name) {
        String sql = "INSERT INTO save_games (name, created_at, last_saved) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String now = LocalDateTime.now().toString();
            ps.setString(1, name);
            ps.setString(2, now);
            ps.setString(3, now);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateLastSaved(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("UPDATE save_games SET last_saved = ? WHERE id = ?")) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("DELETE FROM save_games WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private SaveGame map(ResultSet rs) throws SQLException {
        return new SaveGame(
            rs.getInt("id"),
            rs.getString("name"),
            LocalDateTime.parse(rs.getString("created_at")),
            LocalDateTime.parse(rs.getString("last_saved"))
        );
    }
}
