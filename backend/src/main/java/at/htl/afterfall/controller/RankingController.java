package at.htl.afterfall.controller;

import at.htl.afterfall.model.RankingEntry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ranking")
public class RankingController {

    private final JdbcTemplate jdbc;

    @Value("${RANKING_TOKEN:}")
    private String rankingToken;

    public RankingController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void validateConfig() {
        if (rankingToken == null || rankingToken.isBlank()) {
            throw new IllegalStateException("RANKING_TOKEN env var not set — refusing to start");
        }
    }

    @GetMapping
    public List<RankingEntry> getRanking() {
        return jdbc.query(
                """
                SELECT player_name, net_worth,
                       ROW_NUMBER() OVER (ORDER BY net_worth DESC) AS rank
                FROM players
                ORDER BY net_worth DESC
                LIMIT 100
                """,
                (rs, rowNum) -> new RankingEntry(
                        (int) rs.getLong("rank"),
                        rs.getString("player_name"),
                        rs.getDouble("net_worth")
                )
        );
    }

    @PostMapping
    public ResponseEntity<?> submitScore(@RequestBody Map<String, Object> body) {
        String token = (String) body.get("token");
        if (!rankingToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String playerName = (String) body.get("playerName");
        double netWorth = ((Number) body.get("netWorth")).doubleValue();

        jdbc.update("""
                INSERT INTO players (player_name, net_worth, last_updated)
                VALUES (?, ?, datetime('now'))
                ON CONFLICT(player_name) DO UPDATE SET
                    net_worth    = excluded.net_worth,
                    last_updated = datetime('now')
                """, playerName, netWorth);

        Integer rank = jdbc.queryForObject(
                "SELECT COUNT(*) + 1 FROM players WHERE net_worth > ?",
                Integer.class,
                netWorth
        );

        return ResponseEntity.ok(Map.of("rank", rank != null ? rank : 1));
    }
}
