package at.htl.afterfall.game.tcp;

import at.htl.afterfall.game.db.GameSaveRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Component
public class TcpGameServer {
    private static final Logger LOG = Logger.getLogger(TcpGameServer.class.getName());

    @Value("${game.server.port:9090}")
    private int port;

    private final GameSaveRepository saveRepo;
    private ServerSocket             serverSocket;
    private final ExecutorService    threadPool = Executors.newCachedThreadPool();

    public TcpGameServer(GameSaveRepository saveRepo) {
        this.saveRepo = saveRepo;
    }

    @PostConstruct
    public void start() {
        Thread acceptThread = new Thread(this::acceptLoop, "tcp-game-accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
        LOG.info("TCP Game Server starting on port " + port);
    }

    @PreDestroy
    public void stop() {
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignored) {}
        threadPool.shutdownNow();
    }

    private void acceptLoop() {
        try {
            serverSocket = new ServerSocket(port);
            LOG.info("TCP Game Server listening on port " + port);
            while (!serverSocket.isClosed()) {
                Socket client = serverSocket.accept();
                client.setTcpNoDelay(true);
                threadPool.submit(new ClientHandler(client, saveRepo));
                LOG.info("New client connected: " + client.getRemoteSocketAddress());
            }
        } catch (Exception e) {
            if (!serverSocket.isClosed())
                LOG.severe("TCP accept error: " + e.getMessage());
        }
    }
}
