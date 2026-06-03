package at.htl.afterfall.util;

import at.htl.afterfall.GameConfig;
import at.htl.afterfall.persistence.DatabaseManager;
import at.htl.afterfall.protocol.dto.GameConfigDto;
import at.htl.afterfall.protocol.command.GameCommand;
import at.htl.afterfall.protocol.command.RegisterCommand;
import at.htl.afterfall.protocol.response.*;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class GameClient {
    private static final Logger LOG = Logger.getLogger(GameClient.class.getName());

    private static GameClient instance;

    private Socket               socket;
    private ObjectOutputStream   out;
    private Thread               readerThread;
    private boolean              connected = false;

    private String playerUuid;
    private String playerName;

    private Consumer<GameStateUpdate>  onUpdate;
    private Consumer<GameStateSnapshot> onSnapshot;
    private Consumer<CommandResult>     onCommandResult;
    private Consumer<String>            onError;
    private Consumer<String>            onNewStation;
    private Consumer<Integer>           onRankingResult;
    private Consumer<List<?>>           onSaveList;

    private GameClient() {}

    public static GameClient get() {
        if (instance == null) instance = new GameClient();
        return instance;
    }

    public void setOnUpdate(Consumer<GameStateUpdate> cb)    { this.onUpdate = cb; }
    public void setOnSnapshot(Consumer<GameStateSnapshot> cb){ this.onSnapshot = cb; }
    public void setOnCommandResult(Consumer<CommandResult> cb){ this.onCommandResult = cb; }
    public void setOnError(Consumer<String> cb)              { this.onError = cb; }
    public void setOnNewStation(Consumer<String> cb)         { this.onNewStation = cb; }
    public void setOnRankingResult(Consumer<Integer> cb)     { this.onRankingResult = cb; }
    public void setOnSaveList(Consumer<List<?>> cb)          { this.onSaveList = cb; }

    public boolean isConnected() { return connected; }
    public String  getPlayerUuid()  { return playerUuid; }
    public String  getPlayerName()  { return playerName; }

    public boolean connect() {
        try {
            GameConfig cfg = GameConfig.get();
            socket = new Socket(cfg.gameServerHost, cfg.gameServerPort);
            socket.setTcpNoDelay(true);
            out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            out.flush();

            readerThread = new Thread(this::readLoop, "game-client-reader");
            readerThread.setDaemon(true);
            readerThread.start();

            connected = true;
            LOG.info("Verbunden mit Game Server " + cfg.gameServerHost + ":" + cfg.gameServerPort);
            return true;
        } catch (Exception e) {
            LOG.warning("Verbindung zum Game Server fehlgeschlagen: " + e.getMessage());
            return false;
        }
    }

    public void register(String uuid, String name) throws IOException {
        this.playerUuid = uuid;
        this.playerName = name;
        send(new RegisterCommand(uuid, name));
    }

    public void send(GameCommand cmd) throws IOException {
        if (!connected) throw new IOException("Nicht verbunden.");
        synchronized (out) {
            out.writeObject(cmd);
            out.reset();
            out.flush();
        }
    }

    public void disconnect() {
        connected = false;
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }

    private void readLoop() {
        try {
            ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(socket.getInputStream()));
            while (!socket.isClosed()) {
                Object obj = in.readObject();
                dispatch(obj);
            }
        } catch (EOFException | java.net.SocketException ignored) {
            // server closed connection
        } catch (Exception e) {
            LOG.warning("Game Client read error: " + e.getMessage());
        } finally {
            connected = false;
        }
    }

    private void dispatch(Object obj) {
        if (obj instanceof RegisterResponse r) {
            if (!r.success) {
                disconnect();
                if (onError != null) Platform.runLater(() -> onError.accept(r.message));
            } else if (r.config != null) {
                GameConfig.applyFromServer(r.config);
            }
        } else if (obj instanceof GameStateSnapshot s && onSnapshot != null)
            Platform.runLater(() -> onSnapshot.accept(s));
        else if (obj instanceof GameStateUpdate u && onUpdate != null)
            Platform.runLater(() -> onUpdate.accept(u));
        else if (obj instanceof CommandResult r) {
            if (r.success && r.state != null && onSnapshot != null)
                Platform.runLater(() -> onSnapshot.accept(r.state));
            if (!r.success && onError != null)
                Platform.runLater(() -> onError.accept(r.errorMessage));
            if (onCommandResult != null)
                Platform.runLater(() -> onCommandResult.accept(r));
        } else if (obj instanceof NewStationEvent e && onNewStation != null)
            Platform.runLater(() -> onNewStation.accept(e.station.name));
        else if (obj instanceof RankingResultEvent e && onRankingResult != null)
            Platform.runLater(() -> onRankingResult.accept(e.rank));
        else if (obj instanceof SaveListResponse r && onSaveList != null)
            Platform.runLater(() -> onSaveList.accept(r.saves));
    }

    // ── UUID/Name management ─────────────────────────────────────────────────

    public static String loadOrCreateUuid() {
        DatabaseManager.initSchema();
        String uuid = DatabaseManager.getPlayerValue("uuid");
        if (uuid != null && !uuid.isBlank()) return uuid;
        uuid = UUID.randomUUID().toString();
        DatabaseManager.setPlayerValue("uuid", uuid);
        return uuid;
    }
}
