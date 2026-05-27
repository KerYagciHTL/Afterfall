package at.htl.afterfall.util;

import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

public class RankingClient {

    private static final String BASE_URL;
    private static final String TOKEN;
    private static final Path PLAYER_NAME_FILE;

    static {
        try (var in = RankingClient.class.getResourceAsStream("/at/htl/afterfall/config.properties")) {
            var props = new Properties();
            props.load(in);
            BASE_URL = props.getProperty("ranking.url", "http://localhost:8080/ranking");
            TOKEN    = props.getProperty("ranking.token", "");
        } catch (Exception e) { throw new RuntimeException(e); }

        PLAYER_NAME_FILE = Path.of("ranking.properties");
    }

    private static String loadPlayerName() {
        try {
            if (Files.exists(PLAYER_NAME_FILE)) {
                var p = new Properties();
                p.load(Files.newBufferedReader(PLAYER_NAME_FILE));
                return p.getProperty("player.name", "").strip();
            }
        } catch (Exception ignored) {}
        return "";
    }

    private static void savePlayerName(String name) {
        try {
            var p = new Properties();
            p.setProperty("player.name", name);
            p.store(Files.newBufferedWriter(PLAYER_NAME_FILE), null);
        } catch (Exception ignored) {}
    }

    public static boolean hasPlayerName() {
        return !loadPlayerName().isBlank();
    }

    public static void ensurePlayerName() {
        if (hasPlayerName()) return;
        TextInputDialog dlg = new TextInputDialog("");
        dlg.setTitle("Willkommen!");
        dlg.setHeaderText("Dein Name für die Rangliste:");
        dlg.setContentText("Name:");
        dlg.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) savePlayerName(name.strip());
        });
    }

    public static List<RankingEntry> fetchRanking() {
        try {
            var client = HttpClient.newHttpClient();
            var req = HttpRequest.newBuilder(URI.create(BASE_URL))
                    .GET().build();
            var res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return parseEntries(res.body());
        } catch (Exception e) {
            return null;
        }
    }

    public static void submitScore(double netWorth, Consumer<Integer> onSuccess, Runnable onError) {
        String existing = loadPlayerName();
        if (existing.isBlank()) {
            Platform.runLater(() -> {
                TextInputDialog dlg = new TextInputDialog("");
                dlg.setTitle("Rangliste");
                dlg.setHeaderText("Dein Name für die Rangliste:");
                dlg.showAndWait().ifPresent(name -> {
                    if (!name.isBlank()) {
                        savePlayerName(name.strip());
                        doSubmit(name.strip(), netWorth, onSuccess, onError);
                    }
                });
            });
        } else {
            doSubmit(existing, netWorth, onSuccess, onError);
        }
    }

    private static void doSubmit(String name, double netWorth,
                                  Consumer<Integer> onSuccess, Runnable onError) {
        String body = "{\"token\":\"%s\",\"playerName\":\"%s\",\"netWorth\":%s}"
                .formatted(TOKEN, name, netWorth);

        var client = HttpClient.newHttpClient();
        var req = HttpRequest.newBuilder(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
              .thenAccept(res -> {
                  if (res.statusCode() == 200) {
                      int rank = parseRank(res.body());
                      Platform.runLater(() -> onSuccess.accept(rank));
                  } else {
                      Platform.runLater(onError);
                  }
              })
              .exceptionally(ex -> { Platform.runLater(onError); return null; });
    }

    private static List<RankingEntry> parseEntries(String json) {
        try {
            var arr = new org.json.JSONArray(json);
            var list = new ArrayList<RankingEntry>();
            for (int i = 0; i < arr.length(); i++) {
                var obj = arr.getJSONObject(i);
                list.add(new RankingEntry(
                        obj.getInt("rank"),
                        obj.getString("playerName"),
                        obj.getDouble("netWorth")
                ));
            }
            return list;
        } catch (Exception e) {
            return List.of();
        }
    }

    private static int parseRank(String json) {
        try {
            return new org.json.JSONObject(json).getInt("rank");
        } catch (Exception e) {
            return -1;
        }
    }

    public record RankingEntry(int rank, String playerName, double netWorth) {}
}
