package at.htl.afterfall.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = resolveDbUrl();
    private static Connection connection;

    public static Path getDataDir() {
        String os   = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home");
        Path dir;
        if (os.contains("mac")) {
            dir = Path.of(home, "Library", "Application Support", "Afterfall");
        } else if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            dir = Path.of(appData != null ? appData : home, "Afterfall");
        } else {
            dir = Path.of(home, ".local", "share", "Afterfall");
        }
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
        return dir;
    }

    private static String resolveDbUrl() {
        return "jdbc:sqlite:" + getDataDir().resolve("afterfall.db");
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void initSchema() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player (
                    key   TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                )""");
        } catch (SQLException e) {
            throw new RuntimeException("DB-Schema-Init fehlgeschlagen", e);
        }
    }

    public static String getPlayerValue(String key) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT value FROM player WHERE key = ?")) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (SQLException ignored) {}
        return null;
    }

    public static void setPlayerValue(String key, String value) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO player(key,value) VALUES(?,?) ON CONFLICT(key) DO UPDATE SET value=excluded.value")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }
}
