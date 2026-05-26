package at.htl.afterfall.view;

import at.htl.afterfall.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GameView extends Canvas {
    private static final double STATION_RADIUS = 13;
    private static final double TRAIN_RADIUS   = 8;
    private static final Color  BG_COLOR       = Color.rgb(12, 13, 24);
    private static final Color  GRID_COLOR     = Color.rgb(22, 24, 42);

    // ─── Inner Types ──────────────────────────────────────────────────────────

    public static class RouteSegmentHit {
        public final Route route;
        public final int   stopIndexA;
        public final int   stopIndexB;
        public RouteSegmentHit(Route r, int a, int b) { route = r; stopIndexA = a; stopIndexB = b; }
    }

    @FunctionalInterface
    public interface RouteSegmentRedirectCallback {
        /** newStation wird zwischen stopA und stopB eingefügt (insertAfterIndex = stopA-Index). */
        void call(Route route, Station stopA, Station stopB, int insertAfterIndex, Station newStation);
    }

    @FunctionalInterface
    public interface RouteSegmentClickCallback {
        void call(Route route, Station nearestStop, Station otherStop);
    }

    // ─── Fields ───────────────────────────────────────────────────────────────

    private final GameWorld world;
    private double camX = 0, camY = 0, zoom = 1.0;
    private double dragStartX, dragStartY, camStartX, camStartY;

    private Station highlightStation;
    private Route   activeRouteHighlight;
    private Route   focusedRoute   = null;
    private Train   selectedTrain  = null;

    /** Font-Cache: Schlüssel = (int)(size*2) → spart Font-Objekte pro Frame */
    private final Map<Integer, Font> fontCache = new HashMap<>();

    // Track/Segment Interaction
    private boolean             trackInteractionEnabled = false;
    private RouteSegmentHit     pressedSegment          = null;
    private Track               pressedTrack            = null;
    private boolean             movableIsFirst          = true;
    private Station             dragFixedStation        = null;
    private boolean             dragDetected            = false;
    private double              pressSX, pressSY;
    private double              dragWX, dragWY;
    private Station             dragOtherStation        = null; // zweiter Endpunkt des Segments
    private Station             snapTarget              = null;
    private boolean             lastClickConsumed       = false;

    private Consumer<Track>              trackDemolishCb;
    private RouteSegmentRedirectCallback routeSegmentRedirectCb;
    private RouteSegmentClickCallback    routeSegmentClickCb;

    // ─── Constructor ──────────────────────────────────────────────────────────

    public GameView(GameWorld world) {
        this.world = world;

        setOnMousePressed(e -> {
            if (e.isSecondaryButtonDown()) {
                dragStartX = e.getX(); dragStartY = e.getY();
                camStartX  = camX;    camStartY  = camY;
            } else if (e.getButton() == MouseButton.PRIMARY && trackInteractionEnabled) {
                pressSX        = e.getX();
                pressSY        = e.getY();
                lastClickConsumed = false;
                double wx = toWorldX(e.getX()), wy = toWorldY(e.getY());
                pressedSegment   = findRouteSegmentAt(wx, wy);
                pressedTrack     = pressedSegment == null ? findTrackAt(wx, wy) : null;
                dragDetected     = false;
                dragFixedStation = null;

                if (pressedSegment != null) {
                    List<Station> stops = pressedSegment.route.getStops();
                    Station sA = stops.get(pressedSegment.stopIndexA);
                    Station sB = stops.get(pressedSegment.stopIndexB);
                    double  da = Math.hypot(sA.getX()-wx, sA.getY()-wy);
                    double  db = Math.hypot(sB.getX()-wx, sB.getY()-wy);
                    movableIsFirst   = da < db;
                    dragFixedStation = movableIsFirst ? sB : sA;
                    dragOtherStation = movableIsFirst ? sA : sB;
                }
            }
        });

        setOnMouseDragged(e -> {
            if (e.isSecondaryButtonDown()) {
                camX = camStartX + (e.getX() - dragStartX) / zoom;
                camY = camStartY + (e.getY() - dragStartY) / zoom;
                render();
                return;
            }
            if (pressedSegment != null && trackInteractionEnabled) {
                double screenDist = Math.hypot(e.getX()-pressSX, e.getY()-pressSY);
                if (screenDist > 5) {
                    dragDetected = true;
                    dragWX       = toWorldX(e.getX());
                    dragWY       = toWorldY(e.getY());
                    snapTarget   = findSnapStation(dragWX, dragWY, dragFixedStation);
                    render();
                }
            }
        });

        setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY && trackInteractionEnabled) {
                if (pressedSegment != null) {
                    List<Station> stops = pressedSegment.route.getStops();
                    Station sA      = stops.get(pressedSegment.stopIndexA);
                    Station sB      = stops.get(pressedSegment.stopIndexB);
                    Station nearest = movableIsFirst ? sA : sB;
                    Station other   = movableIsFirst ? sB : sA;

                    if (dragDetected && snapTarget != null) {
                        lastClickConsumed = true;
                        if (routeSegmentRedirectCb != null)
                            routeSegmentRedirectCb.call(
                                pressedSegment.route, sA, sB, pressedSegment.stopIndexA, snapTarget);
                    } else if (!dragDetected) {
                        lastClickConsumed = true;
                        if (routeSegmentClickCb != null)
                            routeSegmentClickCb.call(pressedSegment.route, nearest, other);
                    }
                } else if (pressedTrack != null && !dragDetected) {
                    lastClickConsumed = true;
                    if (trackDemolishCb != null) trackDemolishCb.accept(pressedTrack);
                }
                pressedSegment   = null;
                pressedTrack     = null;
                dragDetected     = false;
                dragFixedStation = null;
                dragOtherStation = null;
                snapTarget       = null;
                render();
            }
        });

        setOnScroll(e -> {
            double delta = e.getDeltaY() != 0 ? e.getDeltaY() : -e.getDeltaX();
            if (delta == 0) return;
            double factor  = delta > 0 ? 1.12 : (1.0 / 1.12);
            double oldZoom = zoom;
            zoom = Math.max(0.2, Math.min(5.0, zoom * factor));
            // Zoom auf Mauszeiger anchern: Weltpunkt unter Cursor bleibt fixiert
            double cx = getWidth() / 2.0, cy = getHeight() / 2.0;
            camX += (e.getX() - cx) * (1.0 / zoom - 1.0 / oldZoom);
            camY += (e.getY() - cy) * (1.0 / zoom - 1.0 / oldZoom);
            render();
        });
    }

    // ─── Render ───────────────────────────────────────────────────────────────

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
        if (dragDetected && dragFixedStation != null) drawDragPreview(gc);
        drawStations(gc);
        drawTrains(gc);

        gc.restore();
    }

    // ─── Dot-Grid Hintergrund (Screen-Space, folgt Kamera) ───────────────────

    private void drawDotGrid(GraphicsContext gc, double w, double h) {
        double spacing = 60 * zoom;
        if (spacing < 10) return;
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

        // Fokus-Outline für ausgewählte Route (vor normalen Route-Linien)
        if (focusedRoute != null) {
            List<Station> fs = focusedRoute.getStops();
            if (fs.size() >= 2) {
                gc.setStroke(Color.color(1, 1, 1, 0.38));
                gc.setLineWidth(13.0 / zoom);
                for (int i = 0; i < fs.size() - 1; i++)
                    gc.strokeLine(fs.get(i).getX(), fs.get(i).getY(),
                                  fs.get(i+1).getX(), fs.get(i+1).getY());
                if (focusedRoute.isCircular()) {
                    Station last = fs.get(fs.size()-1), first = fs.get(0);
                    gc.strokeLine(last.getX(), last.getY(), first.getX(), first.getY());
                }
            }
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

    // ─── Drag-Vorschau ────────────────────────────────────────────────────────

    private void drawDragPreview(GraphicsContext gc) {
        double ex = snapTarget != null ? snapTarget.getX() : dragWX;
        double ey = snapTarget != null ? snapTarget.getY() : dragWY;

        Color base = pressedSegment != null
                ? pressedSegment.route.getColor()
                : Color.WHITE;

        gc.save();
        gc.setStroke(Color.color(base.getRed(), base.getGreen(), base.getBlue(), 0.65));
        gc.setLineWidth(5.0 / zoom);
        gc.setLineDashes(10.0 / zoom, 6.0 / zoom);
        if (dragFixedStation != null)
            gc.strokeLine(dragFixedStation.getX(), dragFixedStation.getY(), ex, ey);
        if (dragOtherStation != null)
            gc.strokeLine(dragOtherStation.getX(), dragOtherStation.getY(), ex, ey);
        gc.setLineDashes(null);
        gc.restore();

        if (snapTarget != null) {
            gc.setFill(Color.color(1.0, 0.84, 0.0, 0.28));
            double gr = STATION_RADIUS * 3.2;
            gc.fillOval(snapTarget.getX()-gr, snapTarget.getY()-gr, gr*2, gr*2);
        }
    }

    // ─── Stationen ────────────────────────────────────────────────────────────

    private void drawStations(GraphicsContext gc) {
        double pulse = 0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 500.0);

        for (Station s : world.getStations()) {
            boolean hl          = s == highlightStation;
            boolean inRoute     = activeRouteHighlight != null
                                  && activeRouteHighlight.getStops().contains(s);
            boolean isFirstStop = inRoute && !activeRouteHighlight.getStops().isEmpty()
                                  && activeRouteHighlight.getStops().get(0) == s;

            // Pulsing ring for stations demanding a connection
            if (s.isDemandingConnection()) {
                double alpha = 0.45 + 0.45 * pulse;
                double extra = (5.0 + 4.0 * pulse) / zoom;
                gc.setStroke(Color.color(1.0, 0.55 + 0.1 * pulse, 0.0, alpha));
                gc.setLineWidth(2.5 / zoom);
                double pr = STATION_RADIUS + extra;
                gc.strokeOval(s.getX() - pr, s.getY() - pr, pr * 2, pr * 2);
            }

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
            Color fill = hl                          ? Color.web("#FFE57F")
                       : isFirstStop                 ? Color.web("#80DEEA")
                       : inRoute                     ? Color.web("#CCFF90")
                       : s.isDemandingConnection()   ? Color.web("#FFCC80")
                       : Color.web("#E8EAF6");

            gc.setFill(fill);
            gc.fillOval(s.getX() - STATION_RADIUS, s.getY() - STATION_RADIUS,
                        STATION_RADIUS * 2, STATION_RADIUS * 2);

            gc.setStroke(Color.rgb(30, 32, 52));
            gc.setLineWidth(2.5 / zoom);
            gc.strokeOval(s.getX() - STATION_RADIUS, s.getY() - STATION_RADIUS,
                          STATION_RADIUS * 2, STATION_RADIUS * 2);

            double fontSize = Math.max(9, 12.0 / zoom);
            gc.setFont(getCachedFont(fontSize));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFill(Color.web("#C5CAE9"));
            gc.fillText(s.getName(), s.getX(), s.getY() + STATION_RADIUS + fontSize + 3);

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
                gc.setFill(Color.rgb(10, 11, 24, 0.90));
                gc.fillRoundRect(bx, by, bw, bh, arc, arc);
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

            // Extra-Glow für ausgewählten Zug
            if (train == selectedTrain) {
                gc.setFill(Color.color(1.0, 0.84, 0.0, 0.45));
                double sgR = TRAIN_RADIUS * 3.8;
                gc.fillOval(x-sgR, y-sgR, sgR*2, sgR*2);
            }

            // Standard-Glow
            gc.setFill(Color.color(routeColor.getRed(), routeColor.getGreen(), routeColor.getBlue(), 0.3));
            double glowR = TRAIN_RADIUS * 2.2;
            gc.fillOval(x - glowR, y - glowR, glowR * 2, glowR * 2);

            // Zug-Körper
            gc.setFill(routeColor);
            gc.fillOval(x - TRAIN_RADIUS, y - TRAIN_RADIUS, TRAIN_RADIUS * 2, TRAIN_RADIUS * 2);

            // Kontur: weiß normal, golden wenn ausgewählt
            gc.setStroke(train == selectedTrain ? Color.web("#FFD54F") : Color.web("#E8EAF6"));
            gc.setLineWidth((train == selectedTrain ? 2.5 : 1.8) / zoom);
            gc.strokeOval(x - TRAIN_RADIUS, y - TRAIN_RADIUS, TRAIN_RADIUS * 2, TRAIN_RADIUS * 2);

            // Richtungs-Punkt
            double angle = Math.atan2(to.getY() - from.getY(), to.getX() - from.getX());
            if (!train.isForward()) angle += Math.PI;
            double arrowLen = TRAIN_RADIUS * 0.55;
            double ax = x + Math.cos(angle) * arrowLen;
            double ay = y + Math.sin(angle) * arrowLen;
            gc.setFill(Color.color(1, 1, 1, 0.6));
            gc.fillOval(ax - 2.5 / zoom, ay - 2.5 / zoom, 5.0 / zoom, 5.0 / zoom);

            // Auslastungs-Badge über dem Zug
            int    onboard  = train.getOnboardCount();
            int    cap      = train.getType().capacity;
            String badgeTxt = onboard + "/" + cap;
            double bfs      = Math.max(7, 9.5 / zoom);
            double bpadX    = 4.5 / zoom;
            double bpadY    = 2.0 / zoom;
            double bw       = badgeTxt.length() * bfs * 0.62 + bpadX * 2;
            double bh       = bfs + bpadY * 2;
            double bx       = x - bw / 2.0;
            double by       = y - TRAIN_RADIUS - bh - 4.0 / zoom;
            double barc     = 3.5 / zoom;
            double ratio    = cap > 0 ? (double) onboard / cap : 0;
            Color  badgeBg  = ratio >= 0.9 ? Color.rgb(183, 28, 28, 0.92)
                            : ratio >= 0.5 ? Color.rgb(230, 120, 0, 0.92)
                            :                Color.rgb(27, 94, 32, 0.92);
            gc.setFill(badgeBg);
            gc.fillRoundRect(bx, by, bw, bh, barc, barc);
            gc.setFont(getCachedFont(bfs));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFill(Color.WHITE);
            gc.fillText(badgeTxt, x, by + bh - bpadY - 0.5 / zoom);
        }
    }

    // ─── Hit Detection ────────────────────────────────────────────────────────

    /**
     * Findet das Route-Segment unter (wx,wy).
     * focusedRoute hat Priorität; danach gewinnt das oberste (zuletzt gezeichnete) Segment.
     */
    public RouteSegmentHit findRouteSegmentAt(double wx, double wy) {
        double threshold = 10.0 / zoom;
        // Fokussierte Route zuerst
        if (focusedRoute != null) {
            RouteSegmentHit hit = findSegmentInRoute(focusedRoute, wx, wy, threshold);
            if (hit != null) return hit;
        }
        // Andere Routen – letztes gezeichnetes Segment gewinnt
        RouteSegmentHit result = null;
        for (Route route : world.getRoutes()) {
            if (route == focusedRoute) continue;
            RouteSegmentHit hit = findSegmentInRoute(route, wx, wy, threshold);
            if (hit != null) result = hit;
        }
        return result;
    }

    private RouteSegmentHit findSegmentInRoute(Route route, double wx, double wy, double threshold) {
        List<Station> stops = route.getStops();
        RouteSegmentHit result = null;
        for (int i = 0; i < stops.size() - 1; i++) {
            Station a = stops.get(i), b = stops.get(i+1);
            if (distToSegment(wx, wy, a.getX(), a.getY(), b.getX(), b.getY()) <= threshold)
                result = new RouteSegmentHit(route, i, i+1);
        }
        if (route.isCircular() && stops.size() >= 2) {
            Station last  = stops.get(stops.size()-1);
            Station first = stops.get(0);
            if (distToSegment(wx, wy, last.getX(), last.getY(), first.getX(), first.getY()) <= threshold)
                result = new RouteSegmentHit(route, stops.size()-1, 0);
        }
        return result;
    }

    /** Graue Infrastruktur-Strecke nahe (wx,wy). */
    public Track findTrackAt(double wx, double wy) {
        double threshold = 10.0 / zoom;
        for (Track t : world.getTracks()) {
            if (distToSegment(wx, wy,
                    t.getFrom().getX(), t.getFrom().getY(),
                    t.getTo().getX(),   t.getTo().getY()) <= threshold)
                return t;
        }
        return null;
    }

    private static double distToSegment(double px, double py,
                                        double x1, double y1, double x2, double y2) {
        double dx = x2-x1, dy = y2-y1;
        double len2 = dx*dx + dy*dy;
        if (len2 == 0) return Math.hypot(px-x1, py-y1);
        double t = Math.max(0, Math.min(1, ((px-x1)*dx + (py-y1)*dy) / len2));
        return Math.hypot(px-(x1+t*dx), py-(y1+t*dy));
    }

    private Station findSnapStation(double wx, double wy, Station exclude) {
        double  snapRadius = 50.0;
        Station nearest    = null;
        double  minDist    = snapRadius;
        for (Station s : world.getStations()) {
            if (s == exclude) continue;
            double d = Math.hypot(s.getX()-wx, s.getY()-wy);
            if (d < minDist) { minDist = d; nearest = s; }
        }
        return nearest;
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

    public boolean popLastClickConsumed() {
        boolean v = lastClickConsumed;
        lastClickConsumed = false;
        return v;
    }

    public void setTrackInteractionEnabled(boolean b)                       { trackInteractionEnabled  = b; }
    public void setTrackDemolishCb(Consumer<Track> cb)                      { trackDemolishCb          = cb; }
    public void setRouteSegmentRedirectCb(RouteSegmentRedirectCallback cb)  { routeSegmentRedirectCb   = cb; }
    public void setRouteSegmentClickCb(RouteSegmentClickCallback cb)        { routeSegmentClickCb      = cb; }
    public void setFocusedRoute(Route r)                                    { this.focusedRoute        = r; }
    public void setSelectedTrain(Train t)                                   { this.selectedTrain       = t; }

    public void setHighlightStation(Station s)   { this.highlightStation     = s; }
    public void setActiveRouteHighlight(Route r) { this.activeRouteHighlight = r; }
    public void resetCamera()                    { camX = 0; camY = 0; zoom  = 1.0; }
}
