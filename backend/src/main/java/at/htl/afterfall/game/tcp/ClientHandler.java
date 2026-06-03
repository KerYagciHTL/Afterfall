package at.htl.afterfall.game.tcp;

import at.htl.afterfall.game.GameConfig;
import at.htl.afterfall.game.GameSession;
import at.htl.afterfall.game.db.GameSaveRepository;
import at.htl.afterfall.protocol.Protocol;
import at.htl.afterfall.protocol.TrainType;
import at.htl.afterfall.protocol.command.*;
import at.htl.afterfall.protocol.dto.GameConfigDto;
import at.htl.afterfall.protocol.response.*;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final Logger LOG = Logger.getLogger(ClientHandler.class.getName());

    private final Socket              socket;
    private final GameSaveRepository  saveRepo;
    private ObjectInputStream         in;
    private ObjectOutputStream        out;

    private String      playerUuid;
    private String      playerName;
    private GameSession session;
    private int         currentSaveId = -1;

    public ClientHandler(Socket socket, GameSaveRepository saveRepo) {
        this.socket   = socket;
        this.saveRepo = saveRepo;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            out.flush();
            in  = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

            loop();
        } catch (EOFException | java.net.SocketException ignored) {
            // client disconnected
        } catch (Exception e) {
            LOG.warning("ClientHandler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void loop() throws Exception {
        while (!socket.isClosed()) {
            Object obj = in.readObject();
            if (!(obj instanceof GameCommand cmd)) continue;

            if (cmd instanceof RegisterCommand rc) {
                handleRegister(rc);
            } else if (playerUuid == null) {
                send(CommandResult.error("Bitte zuerst registrieren."));
            } else if (cmd instanceof ListSavesCommand) {
                send(new SaveListResponse(saveRepo.listSaves(playerUuid)));
            } else if (cmd instanceof DeleteSaveCommand dc) {
                saveRepo.deleteSave(dc.saveId);
                send(new SaveListResponse(saveRepo.listSaves(playerUuid)));
            } else if (cmd instanceof NewGameCommand nc) {
                handleNewGame(nc);
            } else if (cmd instanceof LoadGameCommand lc) {
                handleLoadGame(lc);
            } else if (cmd instanceof QuitGameCommand) {
                handleQuit();
            } else if (session == null) {
                send(CommandResult.error("Kein aktives Spiel. Bitte neues Spiel starten oder laden."));
            } else {
                dispatchToSession(cmd);
            }
        }
    }

    private void handleRegister(RegisterCommand cmd) throws IOException {
        if (!Protocol.VERSION.equals(cmd.protocolVersion)) {
            send(new RegisterResponse(false,
                "Veralteter Client (v" + cmd.protocolVersion + "). Bitte auf v" + Protocol.VERSION + " aktualisieren."));
            socket.close();
            return;
        }
        this.playerUuid = cmd.uuid;
        this.playerName = cmd.playerName;
        saveRepo.registerPlayer(cmd.uuid, cmd.playerName);
        send(new RegisterResponse(true, "Willkommen, " + cmd.playerName + "!", buildConfigDto()));
        LOG.info("Player registered: " + cmd.playerName + " [" + cmd.uuid + "]");
    }

    private GameConfigDto buildConfigDto() {
        GameConfig c = GameConfig.get();
        GameConfigDto dto = new GameConfigDto();
        dto.stationBuildCost    = c.stationBuildCost;
        dto.stationNetWorthGain = c.stationNetWorthGain;
        dto.trackBuildCost      = c.trackBuildCost;
        dto.trackNetWorthGain   = c.trackNetWorthGain;
        dto.trainBaseSpeed      = c.trainBaseSpeed;
        dto.farePerPx           = c.farePerPx;
        dto.minFare             = c.minFare;
        dto.baseSpawnInterval   = c.baseSpawnInterval;
        dto.spawnChanceDivisor  = c.spawnChanceDivisor;
        dto.minSatModifier      = c.minSatModifier;
        dto.cityInitialDelay    = c.cityInitialDelay;
        dto.cityMinInterval     = c.cityMinInterval;
        dto.cityMaxInterval     = c.cityMaxInterval;
        dto.cityMinStationDist  = c.cityMinStationDist;
        dto.citySpawnRadiusMin  = c.citySpawnRadiusMin;
        dto.citySpawnRadiusMax  = c.citySpawnRadiusMax;
        dto.satInitial          = c.satInitial;
        dto.satMaxWait          = c.satMaxWait;
        dto.satUpdateInterval   = c.satUpdateInterval;
        dto.satBaseTarget       = c.satBaseTarget;
        dto.satConnWeight       = c.satConnWeight;
        dto.satDeliveryWeight   = c.satDeliveryWeight;
        dto.satWaitPenalty      = c.satWaitPenalty;
        dto.satEmptyPenalty     = c.satEmptyPenalty;
        dto.satRiseRate         = c.satRiseRate;
        dto.satFallRate         = c.satFallRate;
        spec(dto, c, TrainType.STANDARD); spec(dto, c, TrainType.MEDIUM);
        spec(dto, c, TrainType.SUPER);    spec(dto, c, TrainType.DELUXE);
        return dto;
    }

    private void spec(GameConfigDto dto, GameConfig c, TrainType t) {
        GameConfig.TrainSpec s = c.trainSpec(t);
        if (s == null) return;
        switch (t) {
            case STANDARD -> { dto.standardCapacity = s.capacity(); dto.standardSpeedFactor = s.speedFactor(); dto.standardBuyCost = s.buyCost(); dto.standardOpCostPerKm = s.opCostPerKm(); }
            case MEDIUM   -> { dto.mediumCapacity   = s.capacity(); dto.mediumSpeedFactor   = s.speedFactor(); dto.mediumBuyCost   = s.buyCost(); dto.mediumOpCostPerKm   = s.opCostPerKm(); }
            case SUPER    -> { dto.superCapacity    = s.capacity(); dto.superSpeedFactor    = s.speedFactor(); dto.superBuyCost    = s.buyCost(); dto.superOpCostPerKm    = s.opCostPerKm(); }
            case DELUXE   -> { dto.deluxeCapacity   = s.capacity(); dto.deluxeSpeedFactor   = s.speedFactor(); dto.deluxeBuyCost   = s.buyCost(); dto.deluxeOpCostPerKm   = s.opCostPerKm(); }
        }
    }

    private void handleNewGame(NewGameCommand cmd) throws IOException {
        cleanupSession();
        currentSaveId = saveRepo.createSave(playerUuid, cmd.saveName);
        session = new GameSession(out);
        session.seedInitialEntities();
        session.setOnQuit(this::onSessionQuit);
        session.startSimulation();
        send(CommandResult.ok(session.buildSnapshot()));
        LOG.info(playerName + " started new game: " + cmd.saveName);
    }

    private void handleLoadGame(LoadGameCommand cmd) throws IOException {
        cleanupSession();
        session = new GameSession(out);
        boolean ok = saveRepo.loadWorld(cmd.saveId, session.getWorld());
        if (!ok) {
            session = null;
            send(CommandResult.error("Spielstand nicht gefunden."));
            return;
        }
        currentSaveId = cmd.saveId;
        session.respawnLoadedTrains();
        session.setOnQuit(this::onSessionQuit);
        session.startSimulation();
        send(CommandResult.ok(session.buildSnapshot()));
        LOG.info(playerName + " loaded save " + cmd.saveId);
    }

    private void handleQuit() throws IOException {
        if (session != null) {
            session.stopSimulation();
            double netWorth = session.getWorld().getEconomy().getNetWorth();
            if (currentSaveId >= 0) saveRepo.saveWorld(currentSaveId, session.getWorld());
            saveRepo.updateRanking(playerUuid, netWorth);
            int rank = queryRank(netWorth);
            send(new RankingResultEvent(rank));
            session.shutdown();
            session = null;
        }
        LOG.info(playerName + " quit game.");
    }

    private void onSessionQuit(GameSession s) { /* session stopped itself */ }

    private void dispatchToSession(GameCommand cmd) throws IOException {
        CommandResult result = switch (cmd) {
            case BuildStationCommand c    -> session.handleBuildStation(c);
            case BuildTrackCommand c      -> session.handleBuildTrack(c);
            case DemolishTrackCommand c   -> session.handleDemolishTrack(c);
            case DemolishStationCommand c -> session.handleDemolishStation(c);
            case BuyTrainCommand c        -> session.handleBuyTrain(c);
            case SellTrainCommand c       -> session.handleSellTrain(c);
            case AssignTrainCommand c     -> session.handleAssignTrain(c);
            case UnassignTrainCommand c   -> session.handleUnassignTrain(c);
            case CreateRouteCommand c     -> session.handleCreateRoute(c);
            case AddRouteStopCommand c    -> session.handleAddRouteStop(c);
            case RemoveRouteStopCommand c -> session.handleRemoveRouteStop(c);
            case InsertRouteStopCommand c -> session.handleInsertRouteStop(c);
            case ToggleRouteCommand c     -> session.handleToggleRoute(c);
            case RenameRouteCommand c     -> session.handleRenameRoute(c);
            case DeleteRouteCommand c     -> session.handleDeleteRoute(c);
            case PauseCommand c           -> { session.handlePause(c); yield null; }
            case SetSpeedCommand c        -> { session.handleSetSpeed(c); yield null; }
            case SetTicketPriceCommand c  -> { session.handleSetTicketPrice(c); yield null; }
            default                       -> CommandResult.error("Unbekannter Befehl.");
        };
        if (result != null) send(result);
    }

    private int queryRank(double netWorth) {
        try {
            Integer rank = saveRepo.getJdbc().queryForObject(
                "SELECT COUNT(*) + 1 FROM players WHERE net_worth > ?", Integer.class, netWorth);
            return rank != null ? rank : 1;
        } catch (Exception e) { return 1; }
    }

    private void send(Object obj) throws IOException {
        synchronized (out) {
            out.writeObject(obj);
            out.reset();
            out.flush();
        }
    }

    private void cleanupSession() {
        if (session != null) {
            session.shutdown();
            session = null;
        }
    }

    private void cleanup() {
        if (session != null && currentSaveId >= 0) {
            session.stopSimulation();
            try { saveRepo.saveWorld(currentSaveId, session.getWorld()); }
            catch (Exception e) { LOG.warning("Auto-save bei Disconnect fehlgeschlagen: " + e.getMessage()); }
        }
        cleanupSession();
        try { socket.close(); } catch (Exception ignored) {}
        LOG.info("Client disconnected: " + (playerName != null ? playerName : "unknown"));
    }
}
