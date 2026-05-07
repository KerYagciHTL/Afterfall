package at.htl.afterfall.view;

import at.htl.afterfall.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import java.util.List;

public class GameView extends Canvas {
    private static final double STATION_RADIUS = 12;
    private static final double TRAIN_RADIUS   = 8;

    private final GameWorld world;
    private double camX = 0, camY = 0, zoom = 1.0;
    private double dragStartX, dragStartY, camStartX, camStartY;

    // highlighted station during track-build mode
    private Station highlightStation;

    public GameView(GameWorld world) {
        this.world = world;

        setOnMousePressed(e -> {
            if (e.isSecondaryButtonDown()) {
                dragStartX = e.getX(); dragStartY = e.getY();
                camStartX  = camX;    camStartY  = camY;
            }
        });
        setOnMouseDragged(e -> {
            if (e.isSecondaryButtonDown()) {
                camX = camStartX + (e.getX() - dragStartX) / zoom;
                camY = camStartY + (e.getY() - dragStartY) / zoom;
                render();
            }
        });
        setOnScroll(e -> {
            double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            zoom = Math.max(0.3, Math.min(3.0, zoom * factor));
            render();
        });
    }

    public void render() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;
        GraphicsContext gc = getGraphicsContext2D();

        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.rgb(28, 30, 38));
        gc.fillRect(0, 0, w, h);

        gc.save();
        gc.translate(w / 2.0 + camX * zoom, h / 2.0 + camY * zoom);
        gc.scale(zoom, zoom);

        drawTracks(gc);
        drawStations(gc);
        drawTrains(gc);

        gc.restore();
    }

    private void drawTracks(GraphicsContext gc) {
        // uncolored track layer (thin, dark)
        gc.setStroke(Color.rgb(70, 75, 95));
        gc.setLineWidth(2.0 / zoom);
        for (Track track : world.getTracks()) {
            gc.strokeLine(track.getFrom().getX(), track.getFrom().getY(),
                          track.getTo().getX(),   track.getTo().getY());
        }
        // colored route lines on top
        for (Route route : world.getRoutes()) {
            List<Station> stops = route.getStops();
            if (stops.size() < 2) continue;
            Color c = route.isActive()
                ? route.getColor()
                : route.getColor().deriveColor(0, 0.3, 0.5, 0.7);
            gc.setStroke(c);
            gc.setLineWidth(5.0 / zoom);
            for (int i = 0; i < stops.size() - 1; i++) {
                gc.strokeLine(stops.get(i).getX(), stops.get(i).getY(),
                              stops.get(i + 1).getX(), stops.get(i + 1).getY());
            }
        }
    }

    private void drawStations(GraphicsContext gc) {
        for (Station s : world.getStations()) {
            boolean hl = s == highlightStation;
            gc.setFill(hl ? Color.YELLOW : Color.WHITE);
            gc.fillOval(s.getX() - STATION_RADIUS, s.getY() - STATION_RADIUS,
                        STATION_RADIUS * 2, STATION_RADIUS * 2);
            gc.setStroke(Color.rgb(50, 55, 75));
            gc.setLineWidth(2.0 / zoom);
            gc.strokeOval(s.getX() - STATION_RADIUS, s.getY() - STATION_RADIUS,
                          STATION_RADIUS * 2, STATION_RADIUS * 2);

            double fontSize = Math.max(8, 11.0 / zoom);
            gc.setFont(Font.font(fontSize));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFill(Color.WHITE);
            gc.fillText(s.getName(), s.getX(), s.getY() + STATION_RADIUS + fontSize + 2);

            int waiting = s.getWaitingPassengers().size();
            if (waiting > 0) {
                gc.setFill(Color.rgb(255, 220, 0));
                gc.setFont(Font.font(Math.max(7, 9.0 / zoom)));
                gc.fillText(String.valueOf(waiting), s.getX(), s.getY() + 4.0 / zoom);
            }
        }
    }

    private void drawTrains(GraphicsContext gc) {
        for (Train train : world.getTrains()) {
            if (!train.isActive() || train.getRoute() == null) continue;
            List<Station> stops = train.getRoute().getStops();
            if (stops.size() < 2) continue;

            int idx  = train.getCurrentStopIndex();
            int next = train.isForward() ? idx + 1 : idx - 1;
            if (next < 0 || next >= stops.size()) continue;

            Station from = stops.get(idx);
            Station to   = stops.get(next);
            double  t    = train.getPosition();
            double  x    = from.getX() + (to.getX() - from.getX()) * t;
            double  y    = from.getY() + (to.getY() - from.getY()) * t;

            gc.setFill(train.getRoute().getColor());
            gc.fillOval(x - TRAIN_RADIUS, y - TRAIN_RADIUS, TRAIN_RADIUS * 2, TRAIN_RADIUS * 2);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.5 / zoom);
            gc.strokeOval(x - TRAIN_RADIUS, y - TRAIN_RADIUS, TRAIN_RADIUS * 2, TRAIN_RADIUS * 2);
        }
    }

    // --- coordinate helpers ---

    public double toWorldX(double screenX) { return (screenX - getWidth()  / 2.0) / zoom - camX; }
    public double toWorldY(double screenY) { return (screenY - getHeight() / 2.0) / zoom - camY; }

    public Station findStationAt(double worldX, double worldY) {
        for (Station s : world.getStations()) {
            if (Math.hypot(s.getX() - worldX, s.getY() - worldY) <= STATION_RADIUS + 6) return s;
        }
        return null;
    }

    public void setHighlightStation(Station s) { this.highlightStation = s; }

    public void resetCamera() { camX = 0; camY = 0; zoom = 1.0; }
}
