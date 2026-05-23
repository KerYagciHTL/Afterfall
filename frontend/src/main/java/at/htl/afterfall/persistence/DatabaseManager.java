package at.htl.afterfall.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:afterfall.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void initSchema() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS save_games (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    name       TEXT    NOT NULL,
                    created_at TEXT    NOT NULL,
                    last_saved TEXT    NOT NULL
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS stations (
                    id      INTEGER PRIMARY KEY AUTOINCREMENT,
                    save_id INTEGER NOT NULL REFERENCES save_games(id) ON DELETE CASCADE,
                    name    TEXT    NOT NULL,
                    x       REAL    NOT NULL,
                    y       REAL    NOT NULL
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tracks (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    save_id      INTEGER NOT NULL REFERENCES save_games(id) ON DELETE CASCADE,
                    from_station INTEGER NOT NULL REFERENCES stations(id),
                    to_station   INTEGER NOT NULL REFERENCES stations(id),
                    length       REAL    NOT NULL
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS routes (
                    id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    save_id   INTEGER NOT NULL REFERENCES save_games(id) ON DELETE CASCADE,
                    color_hex TEXT    NOT NULL,
                    active    INTEGER NOT NULL DEFAULT 1
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS route_stops (
                    route_id   INTEGER NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
                    station_id INTEGER NOT NULL REFERENCES stations(id),
                    position   INTEGER NOT NULL,
                    PRIMARY KEY (route_id, position)
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS trains (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    save_id  INTEGER NOT NULL REFERENCES save_games(id) ON DELETE CASCADE,
                    type     TEXT    NOT NULL,
                    route_id INTEGER REFERENCES routes(id),
                    active   INTEGER NOT NULL DEFAULT 1
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS economy (
                    save_id      INTEGER PRIMARY KEY REFERENCES save_games(id) ON DELETE CASCADE,
                    balance      REAL    NOT NULL DEFAULT 10000,
                    net_worth    REAL    NOT NULL DEFAULT 10000,
                    ticket_price REAL    NOT NULL DEFAULT 1.5
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS satisfaction (
                    save_id INTEGER PRIMARY KEY REFERENCES save_games(id) ON DELETE CASCADE,
                    value   REAL    NOT NULL DEFAULT 50.0
                )""");
        } catch (SQLException e) {
            throw new RuntimeException("DB-Schema-Init fehlgeschlagen", e);
        }
    }
}
