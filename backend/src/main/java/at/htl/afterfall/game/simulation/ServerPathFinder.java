package at.htl.afterfall.game.simulation;

import at.htl.afterfall.game.model.ServerGameWorld;
import at.htl.afterfall.game.model.ServerRoute;
import at.htl.afterfall.game.model.ServerStation;

import java.util.*;

public class ServerPathFinder {
    private final ServerGameWorld world;

    public ServerPathFinder(ServerGameWorld world) {
        this.world = world;
    }

    public List<ServerStation> findPath(ServerStation from, ServerStation to) {
        if (from == to) return List.of(from);
        Map<ServerStation, ServerStation> prev = new HashMap<>();
        Queue<ServerStation> queue = new LinkedList<>();
        queue.add(from);
        prev.put(from, null);
        while (!queue.isEmpty()) {
            ServerStation curr = queue.poll();
            if (curr == to) return buildPath(prev, to);
            for (ServerStation neighbor : getNeighbors(curr)) {
                if (!prev.containsKey(neighbor)) {
                    prev.put(neighbor, curr);
                    queue.add(neighbor);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<ServerStation> getNeighbors(ServerStation s) {
        List<ServerStation> neighbors = new ArrayList<>();
        for (ServerRoute route : world.getRoutes()) {
            if (!route.isActive()) continue;
            List<ServerStation> stops = route.getStops();
            for (int i = 0; i < stops.size(); i++) {
                if (stops.get(i) == s) {
                    if (i > 0)                neighbors.add(stops.get(i - 1));
                    if (i < stops.size() - 1) neighbors.add(stops.get(i + 1));
                }
            }
        }
        return neighbors;
    }

    private List<ServerStation> buildPath(Map<ServerStation, ServerStation> prev, ServerStation to) {
        LinkedList<ServerStation> path = new LinkedList<>();
        for (ServerStation cur = to; cur != null; cur = prev.get(cur)) path.addFirst(cur);
        return path;
    }
}
