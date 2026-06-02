package at.htl.afterfall.simulation;

import at.htl.afterfall.model.GameWorld;
import at.htl.afterfall.model.Station;
import at.htl.afterfall.view.GameView;
import javafx.animation.AnimationTimer;
import java.util.function.Consumer;

public class GameLoop extends AnimationTimer {
    private static final double MAX_DELTA     = 0.1;  // cap: max 100ms pro Frame
    private static final double SMOOTH_TIME   = 2.0;  // EMA-Konstante für €/s

    private long    lastTime        = 0;
    private boolean paused          = false;
    private int     speedMultiplier = 1;
    private boolean serverMode      = false;

    private final GameWorld           world;
    private final GameView            gameView;
    private final PassengerSimulation passengerSim;
    private final EconomyEngine       economyEngine;
    private final SatisfactionEngine  satisfactionEngine;
    private final CityGrowthEngine    cityGrowthEngine;

    public GameLoop(GameWorld world, GameView gameView) {
        this.world              = world;
        this.gameView           = gameView;
        this.passengerSim       = new PassengerSimulation(world);
        this.economyEngine      = new EconomyEngine(world);
        this.satisfactionEngine = new SatisfactionEngine(world);
        this.cityGrowthEngine   = new CityGrowthEngine(world);
    }

    public void setOnNewStation(Consumer<Station> cb) {
        cityGrowthEngine.setOnNewStation(cb);
    }

    public void setServerMode(boolean b) { serverMode = b; }

    @Override
    public void handle(long now) {
        if (serverMode) { gameView.render(); return; }
        if (paused) return;
        if (lastTime == 0) { lastTime = now; return; }

        double delta       = Math.min((now - lastTime) / 1_000_000_000.0, MAX_DELTA);
        lastTime = now;
        double scaledDelta = delta * speedMultiplier;

        // Balance vor dem Tick → für Einnahmen-Rate
        double balanceBefore = world.getEconomy().getBalance();

        cityGrowthEngine.tick(scaledDelta);
        passengerSim.tick(scaledDelta);
        economyEngine.tick(scaledDelta);
        satisfactionEngine.tick(scaledDelta);

        // EMA für €/s — gegen reale Zeit (nicht Spielzeit), damit Label stabil bleibt
        double netChange    = world.getEconomy().getBalance() - balanceBefore;
        double instantRate  = delta > 0 ? netChange / delta : 0;
        double alpha        = delta / (SMOOTH_TIME + delta);
        double smoothed     = world.getEconomy().getIncomeRate() * (1 - alpha) + instantRate * alpha;
        world.getEconomy().setIncomeRate(smoothed);

        gameView.render();
    }

    public void togglePause() {
        paused = !paused;
        if (!paused) lastTime = 0;
    }

    public boolean isPaused()              { return paused; }
    public int     getSpeedMultiplier()    { return speedMultiplier; }
    public void    setSpeedMultiplier(int m) { speedMultiplier = Math.max(1, m); }
}
