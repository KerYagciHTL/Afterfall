package at.htl.afterfall.game.db;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class GameDatabaseInitializer {

    private final JdbcTemplate jdbc;

    public GameDatabaseInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void init() {
        try { jdbc.execute("ALTER TABLE players ADD COLUMN uuid TEXT"); }
        catch (Exception ignored) { /* column already exists */ }

        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS game_saves (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid         TEXT NOT NULL,
                    name         TEXT NOT NULL,
                    created_at   TEXT NOT NULL DEFAULT (datetime('now')),
                    last_saved   TEXT NOT NULL DEFAULT (datetime('now'))
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS save_stations (
                    id      INTEGER PRIMARY KEY AUTOINCREMENT,
                    save_id INTEGER NOT NULL REFERENCES game_saves(id) ON DELETE CASCADE,
                    name    TEXT NOT NULL,
                    x       REAL NOT NULL,
                    y       REAL NOT NULL
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS save_tracks (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    save_id    INTEGER NOT NULL REFERENCES game_saves(id) ON DELETE CASCADE,
                    from_id    INTEGER NOT NULL,
                    to_id      INTEGER NOT NULL
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS save_routes (
                    id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    save_id   INTEGER NOT NULL REFERENCES game_saves(id) ON DELETE CASCADE,
                    name      TEXT NOT NULL,
                    color_hex TEXT NOT NULL,
                    active    INTEGER NOT NULL DEFAULT 1,
                    circular  INTEGER NOT NULL DEFAULT 0
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS save_route_stops (
                    route_id   INTEGER NOT NULL,
                    station_id INTEGER NOT NULL,
                    position   INTEGER NOT NULL,
                    PRIMARY KEY (route_id, position)
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS save_trains (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    save_id  INTEGER NOT NULL REFERENCES game_saves(id) ON DELETE CASCADE,
                    type     TEXT NOT NULL,
                    route_id INTEGER
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS save_economy (
                    save_id      INTEGER PRIMARY KEY REFERENCES game_saves(id) ON DELETE CASCADE,
                    balance      REAL NOT NULL,
                    net_worth    REAL NOT NULL,
                    ticket_price REAL NOT NULL DEFAULT 1.0
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS save_satisfaction (
                    save_id INTEGER PRIMARY KEY REFERENCES game_saves(id) ON DELETE CASCADE,
                    value   REAL NOT NULL
                )
                """);
    }
}
