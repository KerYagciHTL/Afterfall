package at.htl.afterfall.simulation;

import at.htl.afterfall.model.GameWorld;
import at.htl.afterfall.model.Route;
import at.htl.afterfall.model.Station;
import java.util.*;

public class PathFinder {
    private final GameWorld world;

    public PathFinder(GameWorld world) {
        this.world = world;
    }

    public List<Station> findPath(Station from, Station to) {
        if (from == to) return List.of(from);
        Map<Station, Station> prev = new HashMap<>();
        Queue<Station> queue = new LinkedList<>();
        queue.add(from);
        prev.put(from, null);
        while (!queue.isEmpty()) {
            Station curr = queue.poll();
            if (curr == to) return buildPath(prev, to);
            for (Station neighbor : getNeighbors(curr)) {
                if (!prev.containsKey(neighbor)) {
                    prev.put(neighbor, curr);
                    queue.add(neighbor);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<Station> getNeighbors(Station s) {
        List<Station> neighbors = new ArrayList<>();
        for (Route route : world.getRoutes()) {
            if (!route.isActive()) continue;
            List<Station> stops = route.getStops();
            for (int i = 0; i < stops.size(); i++) {
                if (stops.get(i) == s) {
                    if (i > 0)                 neighbors.add(stops.get(i - 1));
                    if (i < stops.size() - 1)  neighbors.add(stops.get(i + 1));
                }
            }
        }
        return neighbors;
    }

    private List<Station> buildPath(Map<Station, Station> prev, Station to) {
        LinkedList<Station> path = new LinkedList<>();
        for (Station cur = to; cur != null; cur = prev.get(cur)) path.addFirst(cur);
        return path;
    }
}
