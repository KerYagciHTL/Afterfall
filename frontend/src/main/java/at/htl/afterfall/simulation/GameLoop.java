package at.htl.afterfall.simulation;

import at.htl.afterfall.model.GameWorld;
import at.htl.afterfall.view.GameView;
import javafx.animation.AnimationTimer;

public class GameLoop extends AnimationTimer {
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
        double delta = (now - lastTime) / 1_000_000_000.0;
        lastTime = now;

        passengerSim.tick(delta);
        economyEngine.tick(delta);
        satisfactionEngine.tick(delta);
        gameView.render();
    }

    public void togglePause() {
        paused = !paused;
        if (!paused) lastTime = 0;
    }

    public boolean isPaused() { return paused; }
}
