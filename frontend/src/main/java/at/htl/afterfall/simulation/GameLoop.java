package at.htl.afterfall.simulation;

import at.htl.afterfall.GameConfig;
import at.htl.afterfall.model.GameWorld;
import at.htl.afterfall.model.Station;
import at.htl.afterfall.model.Train;
import at.htl.afterfall.view.GameView;
import javafx.animation.AnimationTimer;

import java.util.List;
import java.util.function.Consumer;

public class GameLoop extends AnimationTimer {
    private static final double MAX_DELTA   = 0.1;
    private static final double SMOOTH_TIME = 2.0;

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

    public void setServerMode(boolean b) { serverMode = b; lastTime = 0; }

    @Override
    public void handle(long now) {
        if (serverMode) {
            if (paused) {
                gameView.render();
                return;
            }
            if (lastTime != 0) {
                double delta = Math.min((now - lastTime) / 1_000_000_000.0, MAX_DELTA);
                moveTrainsVisual(delta * speedMultiplier);
            }
            lastTime = now;
            gameView.render();
            return;
        }

        if (paused) return;
        if (lastTime == 0) { lastTime = now; return; }

        double delta      = Math.min((now - lastTime) / 1_000_000_000.0, MAX_DELTA);
        lastTime = now;
        double scaledDelta = delta * speedMultiplier;

        double balanceBefore = world.getEconomy().getBalance();

        cityGrowthEngine.tick(scaledDelta);
        passengerSim.tick(scaledDelta);
        economyEngine.tick(scaledDelta);
        satisfactionEngine.tick(scaledDelta);

        double netChange   = world.getEconomy().getBalance() - balanceBefore;
        double instantRate = delta > 0 ? netChange / delta : 0;
        double alpha       = delta / (SMOOTH_TIME + delta);
        double smoothed    = world.getEconomy().getIncomeRate() * (1 - alpha) + instantRate * alpha;
        world.getEconomy().setIncomeRate(smoothed);

        gameView.render();
    }

    // Lokale Zugbewegung für Client-Rendering im Server-Modus (kein Boarding, keine Wirtschaft)
    private void moveTrainsVisual(double delta) {
        for (Train train : world.getTrains()) {
            if (!train.isActive() || train.getRoute() == null || !train.getRoute().isActive()) continue;
            List<Station> stops = train.getRoute().getStops();
            if (stops.size() < 2) continue;

            double speed = GameConfig.get().trainBaseSpeed * train.getType().speedFactor();
            int    idx   = train.getCurrentStopIndex();
            int    next  = train.isForward() ? idx + 1 : idx - 1;

            if (train.getRoute().isCircular()) {
                if (next < 0)                  next = stops.size() - 1;
                else if (next >= stops.size()) next = 0;
            } else {
                if (next < 0 || next >= stops.size()) {
                    train.setForward(!train.isForward());
                    next = train.isForward() ? idx + 1 : idx - 1;
                    if (next < 0 || next >= stops.size()) continue;
                }
            }

            Station from   = stops.get(idx);
            Station to     = stops.get(next);
            double  segLen = Math.hypot(to.getX() - from.getX(), to.getY() - from.getY());
            if (segLen < 1) { train.setCurrentStopIndex(next); continue; }

            double progress = train.getPosition() + speed * delta / segLen;
            if (progress >= 1.0) {
                train.setPosition(0.0);
                train.setCurrentStopIndex(next);
            } else {
                train.setPosition(progress);
            }
        }
    }

    public void togglePause() {
        paused = !paused;
        if (!paused) lastTime = 0;
    }

    public boolean isPaused()              { return paused; }
    public int     getSpeedMultiplier()    { return speedMultiplier; }
    public void    setSpeedMultiplier(int m) { speedMultiplier = Math.max(1, m); }
}
