package at.htl.afterfall.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseInitializer {

    private final JdbcTemplate jdbc;

    public DatabaseInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void init() {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    player_name  TEXT PRIMARY KEY,
                    net_worth    REAL NOT NULL,
                    last_updated TEXT NOT NULL
                )
                """);
    }
}
