package at.htl.afterfall.view;

import at.htl.afterfall.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameView extends Canvas {
    private static final double STATION_RADIUS = 13;
    private static final double TRAIN_RADIUS   = 8;
    private static final Color  BG_COLOR       = Color.rgb(12, 13, 24);
    private static final Color  GRID_COLOR     = Color.rgb(22, 24, 42);

    private final GameWorld world;
    private double camX = 0, camY = 0, zoom = 1.0;
    private double dragStartX, dragStartY, camStartX, camStartY;

    private Station highlightStation;
    private Route   activeRouteHighlight;

    /** Font-Cache: Schlüssel = (int)(size*2) → spart Font-Objekte pro Frame */
    private final Map<Integer, Font> fontCache = new HashMap<>();

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
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);

        drawDotGrid(gc, w, h);

        gc.save();
        gc.translate(w / 2.0 + camX * zoom, h / 2.0 + camY * zoom);
        gc.scale(zoom, zoom);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        drawTracks(gc);
        drawStations(gc);
        drawTrains(gc);

        gc.restore();
    }

    // ─── Dot-Grid Hintergrund (Screen-Space, folgt Kamera) ───────────────────

    private void drawDotGrid(GraphicsContext gc, double w, double h) {
        double spacing = 60 * zoom;
        if (spacing < 10) return; // bei extremem Rauszoomen nicht zeichnen
        double ox = (w / 2.0 + camX * zoom) % spacing;
        double oy = (h / 2.0 + camY * zoom) % spacing;
        gc.setFill(GRID_COLOR);
        double dotR = Math.max(1.0, 1.5 * zoom);
        for (double x = ox - spacing; x < w + spacing; x += spacing) {
            for (double y = oy - spacing; y < h + spacing; y += spacing) {
                gc.fillOval(x - dotR, y - dotR, dotR * 2, dotR * 2);
            }
        }
    }

    // ─── Strecken & Routen ────────────────────────────────────────────────────

    private void drawTracks(GraphicsContext gc) {
        // Basis-Strecken (dunkel, thin)
        gc.setStroke(Color.rgb(40, 43, 68));
        gc.setLineWidth(2.5 / zoom);
        for (Track track : world.getTracks()) {
            gc.strokeLine(track.getFrom().getX(), track.getFrom().getY(),
                          track.getTo().getX(),   track.getTo().getY());
        }

        // Route-Linien (farbig, dick, mit Glow-Effekt via doppeltem Zeichnen)
        for (Route route : world.getRoutes()) {
            List<Station> stops = route.getStops();
            if (stops.size() < 2) continue;

            Color baseColor = route.getColor();
            Color c = route.isActive() ? baseColor : baseColor.deriveColor(0, 0.3, 0.5, 0.6);

            // Glow-Layer (breiter, halbdurchsichtig)
            gc.setStroke(Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.25));
            gc.setLineWidth(12.0 / zoom);
            for (int i = 0; i < stops.size() - 1; i++) {
                gc.strokeLine(stops.get(i).getX(), stops.get(i).getY(),
                              stops.get(i+1).getX(), stops.get(i+1).getY());
            }

            // Linie selbst
            gc.setStroke(c);
            gc.setLineWidth(5.5 / zoom);
            for (int i = 0; i < stops.size() - 1; i++) {
                gc.strokeLine(stops.get(i).getX(), stops.get(i).getY(),
                              stops.get(i+1).getX(), stops.get(i+1).getY());
            }

            // Kreis-Schlusskante (gestrichelt)
            if (route.isCircular() && stops.size() >= 2) {
                Station last  = stops.get(stops.size() - 1);
                Station first = stops.get(0);
                gc.setStroke(c);
                gc.setLineWidth(4.0 / zoom);
                gc.setLineDashes(12.0 / zoom, 7.0 / zoom);
                gc.strokeLine(last.getX(), last.getY(), first.getX(), first.getY());
                gc.setLineDashes(null);
            }
        }
    }

    // ─── Stationen ────────────────────────────────────────────────────────────

    private void drawStations(GraphicsContext gc) {
        for (Station s : world.getStations()) {
            boolean hl          = s == highlightStation;
            boolean inRoute     = activeRouteHighlight != null
                                  && activeRouteHighlight.getStops().contains(s);
            boolean isFirstStop = inRoute && !activeRouteHighlight.getStops().isEmpty()
                                  && activeRouteHighlight.getStops().get(0) == s;

            // Glow für hervorgehobene Stationen
            if (hl || isFirstStop || inRoute) {
                Color glowC = hl ? Color.color(1,1,0,0.18)
                            : isFirstStop ? Color.color(0.5, 0.85, 0.78, 0.22)
                            : Color.color(0.68, 0.85, 0.50, 0.15);
                gc.setFill(glowC);
                double gr = STATION_RADIUS * 2.4;
                gc.fillOval(s.getX() - gr, s.getY() - gr, gr * 2, gr * 2);
            }

            // Station-Körper
            Color fill = hl          ? Color.web("#FFE57F")
                       : isFirstStop ? Color.web("#80DEEA")
                       : inRoute     ? Color.web("#CCFF90")
                       : Color.web("#E8EAF6");

            gc.setFill(fill);
            gc.fillOval(s.getX() - STATION_RADIUS, s.getY() - STATION_RADIUS,
                        STATION_RADIUS * 2, STATION_RADIUS * 2);

            // Rand
            gc.setStroke(Color.rgb(30, 32, 52));
            gc.setLineWidth(2.5 / zoom);
            gc.strokeOval(s.getX() - STATION_RADIUS, s.getY() - STATION_RADIUS,
                          STATION_RADIUS * 2, STATION_RADIUS * 2);

            // Name
            double fontSize = Math.max(9, 12.0 / zoom);
            gc.setFont(getCachedFont(fontSize));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFill(Color.web("#C5CAE9"));
            gc.fillText(s.getName(), s.getX(), s.getY() + STATION_RADIUS + fontSize + 3);

            // Warte-Badge: dunkle Pille ÜBER der Station, weißer Text
            int waiting = s.getWaitingPassengers().size();
            if (waiting > 0) {
                String  txt  = String.valueOf(waiting);
                double  fs   = Math.max(8, 10.5 / zoom);
                double  padX = 5.0 / zoom;
                double  padY = 2.5 / zoom;
                double  bw   = txt.length() * fs * 0.65 + padX * 2;
                double  bh   = fs + padY * 2;
                double  bx   = s.getX() - bw / 2.0;
                double  by   = s.getY() - STATION_RADIUS - bh - 5.0 / zoom;
                double  arc  = 4.0 / zoom;

                // Dunkle Pille
                gc.setFill(Color.rgb(10, 11, 24, 0.90));
                gc.fillRoundRect(bx, by, bw, bh, arc, arc);

                // Weißer Text zentriert in Pille
                gc.setFont(getCachedFont(fs));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setFill(Color.WHITE);
                gc.fillText(txt, s.getX(), by + bh - padY - 0.5 / zoom);
            }
        }
    }

    // ─── Züge ─────────────────────────────────────────────────────────────────

    private void drawTrains(GraphicsContext gc) {
        for (Train train : world.getTrains()) {
            if (!train.isActive() || train.getRoute() == null) continue;
            Route         route = train.getRoute();
            List<Station> stops = route.getStops();
            if (stops.size() < 2) continue;

            int idx  = train.getCurrentStopIndex();
            int next = train.isForward() ? idx + 1 : idx - 1;

            if (route.isCircular()) {
                if (next < 0)                  next = stops.size() - 1;
                else if (next >= stops.size()) next = 0;
            } else {
                if (next < 0 || next >= stops.size()) continue;
            }

            Station from = stops.get(idx);
            Station to   = stops.get(next);
            double  t    = train.getPosition();
            double  x    = from.getX() + (to.getX() - from.getX()) * t;
            double  y    = from.getY() + (to.getY() - from.getY()) * t;

            Color routeColor = route.getColor();

            // Glow
            gc.setFill(Color.color(routeColor.getRed(), routeColor.getGreen(), routeColor.getBlue(), 0.3));
            double glowR = TRAIN_RADIUS * 2.2;
            gc.fillOval(x - glowR, y - glowR, glowR * 2, glowR * 2);

            // Zug-Körper
            gc.setFill(routeColor);
            gc.fillOval(x - TRAIN_RADIUS, y - TRAIN_RADIUS, TRAIN_RADIUS * 2, TRAIN_RADIUS * 2);

            // Weiße Kontur
            gc.setStroke(Color.web("#E8EAF6"));
            gc.setLineWidth(1.8 / zoom);
            gc.strokeOval(x - TRAIN_RADIUS, y - TRAIN_RADIUS, TRAIN_RADIUS * 2, TRAIN_RADIUS * 2);

            // Richtungs-Pfeil (kleines Dreieck)
            double angle = Math.atan2(to.getY() - from.getY(), to.getX() - from.getX());
            if (!train.isForward()) angle += Math.PI;
            double arrowLen = TRAIN_RADIUS * 0.55;
            double ax = x + Math.cos(angle) * arrowLen;
            double ay = y + Math.sin(angle) * arrowLen;
            gc.setFill(Color.color(1, 1, 1, 0.6));
            gc.fillOval(ax - 2.5 / zoom, ay - 2.5 / zoom, 5.0 / zoom, 5.0 / zoom);
        }
    }

    // ─── Hilfsmethoden ────────────────────────────────────────────────────────

    private Font getCachedFont(double size) {
        int key = (int) Math.round(size * 2);
        return fontCache.computeIfAbsent(key, k -> Font.font("Segoe UI", k / 2.0));
    }

    public double  toWorldX(double screenX) { return (screenX - getWidth()  / 2.0) / zoom - camX; }
    public double  toWorldY(double screenY) { return (screenY - getHeight() / 2.0) / zoom - camY; }

    public Station findStationAt(double worldX, double worldY) {
        for (Station s : world.getStations()) {
            if (Math.hypot(s.getX() - worldX, s.getY() - worldY) <= STATION_RADIUS + 7) return s;
        }
        return null;
    }

    public void setHighlightStation(Station s)   { this.highlightStation     = s; }
    public void setActiveRouteHighlight(Route r) { this.activeRouteHighlight = r; }
    public void resetCamera()                    { camX = 0; camY = 0; zoom  = 1.0; }
}
