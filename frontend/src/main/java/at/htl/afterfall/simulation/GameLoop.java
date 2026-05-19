package at.htl.afterfall.simulation;

import at.htl.afterfall.model.GameWorld;
import at.htl.afterfall.view.GameView;
import javafx.animation.AnimationTimer;

public class GameLoop extends AnimationTimer {
    private static final double MAX_DELTA     = 0.1;  // cap: max 100ms pro Frame
    private static final double SMOOTH_TIME   = 2.0;  // EMA-Konstante für €/s

    private long    lastTime = 0;
    private boolean paused   = false;

    private final GameWorld           world;
    private final GameView            gameView;
    private final PassengerSimulation passengerSim;
    private final EconomyEngine       economyEngine;
    private final SatisfactionEngine  satisfactionEngine;

    public GameLoop(GameWorld world, GameView gameView) {
        this.world              = world;
        this.gameView           = gameView;
        this.passengerSim       = new PassengerSimulation(world);
        this.economyEngine      = new EconomyEngine(world);
        this.satisfactionEngine = new SatisfactionEngine(world);
    }

    @Override
    public void handle(long now) {
        if (paused) return;
        if (lastTime == 0) { lastTime = now; return; }

        double delta = Math.min((now - lastTime) / 1_000_000_000.0, MAX_DELTA);
        lastTime = now;

        // Balance vor dem Tick → für Einnahmen-Rate
        double balanceBefore = world.getEconomy().getBalance();

        passengerSim.tick(delta);
        economyEngine.tick(delta);
        satisfactionEngine.tick(delta);

        // EMA für €/s (exponential moving average, 2s-Konstante)
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

    public boolean isPaused() { return paused; }
}
