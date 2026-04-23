package com.metrobuilder.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseManager {
    private static HikariDataSource dataSource;

    public static void initialize() {
        if (dataSource != null) return;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:metro-builder.db");
        config.setMaximumPoolSize(4);
        config.setConnectionTimeout(30_000);
        dataSource = new HikariDataSource(config);
        runSchema();
    }

    public static DataSource getDataSource() {
        if (dataSource == null) throw new IllegalStateException("DatabaseManager not initialized");
        return dataSource;
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) dataSource.close();
    }

    private static void runSchema() {
        try (InputStream is = DatabaseManager.class.getResourceAsStream("/database/schema.sql");
             Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            assert is != null;
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            // Execute each statement separated by semicolons
            for (String s : sql.split(";")) {
                String trimmed = s.strip();
                if (!trimmed.isEmpty()) stmt.execute(trimmed);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to run schema", e);
        }
    }
}
