package today.tecktip.killbill.backend.gameserver.map;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.data.TileCoordinates;
import today.tecktip.killbill.common.maploader.KillBillMap;
import today.tecktip.killbill.common.maploader.MapDirective;
import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.common.maploader.directives.ChestDirective;
import today.tecktip.killbill.common.maploader.directives.ObjectDirective;
import today.tecktip.killbill.common.maploader.directives.RoomDirective;
import today.tecktip.killbill.common.maploader.directives.TileDirective;

/**
 * Runs the A* pathfinding algorithm on a loaded map.
 * 
 * I strongly suggest you don't read this one.
 * @author cs
 */
public class PathfindingGrid {
    private static final TileCoordinates[] NEIGHBORS = new TileCoordinates[] {new TileCoordinates(0, 1), new TileCoordinates(0, -1), new TileCoordinates(1, 0), new TileCoordinates(-1, 0)}; 
    private static final CostComparator COST_COMPARATOR = new CostComparator();
    private boolean[][] grid;
    private int xOffset;
    private int yOffset;

    public PathfindingGrid(final KillBillMap map) {
        final List<TileCoordinates> objects = new ArrayList<TileCoordinates>();
        for (final MapDirective d : map.getDirectives()) {
            if (d instanceof RoomDirective) {
                RoomDirective r = (RoomDirective) d;

                // Walls surrounding it, except for exclusions.
                // y = 1
                // h = 5
                // 1, 2, 3, 4, 5
                // so the actual end is y + h - 1
                for (int x = r.getLocation().x(); x < r.getLocation().x() + r.getSize().x(); x++) {
                    // Add on both sides
                    if (!isExcluded(x, r.getLocation().y(), r)) {
                        objects.add(new TileCoordinates(x, r.getLocation().y()));
                    }
                    if (!isExcluded(x, r.getLocation().y() + r.getSize().y() - 1, r)) {
                        objects.add(new TileCoordinates(x, r.getLocation().y() - 1));
                    }
                }

                // Add the side lines
                for (int y = r.getLocation().y() + 1; y < r.getLocation().y() + r.getSize().y() - 1; y++) {
                    // Top X and bottom X
                    if (!isExcluded(r.getLocation().x(), y, r)) {
                        objects.add(new TileCoordinates(r.getLocation().x(), y));
                    }
                    if (!isExcluded(r.getLocation().x() + r.getSize().x() - 1, y, r)) {
                        objects.add(new TileCoordinates(r.getLocation().x() + r.getSize().x() - 1, y));
                    }
                }
            }

            else if (d instanceof TileDirective) {
                TileDirective t = (TileDirective) d;
                if (!t.getFlags().contains(ObjectFlag.SOLID)) continue;
                addFilled(t.getLocations(), t.getSize(), objects);
            }

            else if (d instanceof ObjectDirective) {
                ObjectDirective o = (ObjectDirective) d;
                if (!o.getFlags().contains(ObjectFlag.SOLID)) continue;
                addFilled(o.getLocations(), o.getSize(), objects);
            }

            else if (d instanceof ChestDirective) {
                ChestDirective c = (ChestDirective) d;
                if (!c.getFlags().contains(ObjectFlag.SOLID)) continue;
                addFilled(List.of(c.getLocation()), c.getSize(), objects);
            }
        }

        if (objects.size() == 0) throw new IllegalArgumentException("Can't operate on an empty map");

        // Turn this into a 2d array for speed
        int maxX = objects.get(0).x();
        int minX = objects.get(0).x();

        int maxY = objects.get(0).y();
        int minY = objects.get(0).y();

        for (final TileCoordinates c : objects) {
            if (c.x() < minX) minX = c.x();
            if (c.x() > maxX) maxX = c.x();
            if (c.y() < minY) minY = c.y();
            if (c.y() > maxY) maxY = c.y();
        } 

        // Define our map
        // max 10
        // min 1
        // size 10
        // 1->0 10->9
        xOffset = minX;
        yOffset = minY;
        grid = new boolean[maxX + 1 - minX][maxY + 1 - minY];

        // Fill in the tiles
        for (final TileCoordinates c : objects) {
            grid[c.x() - xOffset][c.y() - yOffset] = true;
        }
    }

    public List<TileCoordinates> bestPath(final int fromX, final int fromY, final int toX, final int toY) {
        final Map<TileCoordinates, TileCoordinates> path = new HashMap<>();
        final Map<TileCoordinates, Integer> cost = new HashMap<>();

        final PriorityQueue<SimpleEntry<TileCoordinates, Integer>> frontier = new PriorityQueue<>(10, COST_COMPARATOR);
        
        final TileCoordinates start = new TileCoordinates(fromX - xOffset, fromY - yOffset);
        final TileCoordinates end = new TileCoordinates(toX - xOffset, toY - yOffset);

        frontier.add(new SimpleEntry<>(start, 0));

        path.put(start, start);
        cost.put(start, 0);

        while (!frontier.isEmpty()) {
            TileCoordinates current = frontier.poll().getKey();

            if (current.x() == end.x() && current.y() == end.y()) {
                // Found a path. Reconstruct it and return
                final List<TileCoordinates> followedPath = new ArrayList<>();
                TileCoordinates c = end;
                while (!c.equals(start)) {
                    followedPath.add(new TileCoordinates(c.x() + xOffset, c.y() + yOffset));
                    c = path.get(c);
                }
                return followedPath.reversed();
            }

            for (TileCoordinates c : NEIGHBORS) {
                int newX = current.x() + c.x();
                int newY = current.y() + c.y();

                // Invalid spot
                if (newX >= grid.length || newY >= grid[0].length) continue;

                if (grid[newX][newY]) continue;

                final TileCoordinates next = new TileCoordinates(newX, newY);

                int newCost = cost.get(current) + 1;
                if (!cost.containsKey(next) || newCost < cost.get(next)) {
                    cost.put(next, newCost);
                    int priority = newCost + Math.abs(end.x() - next.x()) + Math.abs(end.y() - next.y());
                    frontier.add(new SimpleEntry<>(next, priority));
                    path.put(next, current);
                }
            }
        }

        // No path :(
        // (this should not be possible unless we have a really badly designed map...)
        return null;
    }

    private static class CostComparator implements Comparator<SimpleEntry<TileCoordinates, Integer>> {
        @Override
        public int compare(SimpleEntry<TileCoordinates, Integer> x, SimpleEntry<TileCoordinates, Integer> y) {
            return x.getValue() - y.getValue();
        }
    }

    private void addFilled(final List<TileCoordinates> coordinates, final TileCoordinates size, final List<TileCoordinates> objects) {
        for (final TileCoordinates c : coordinates) {
            // Iterate up to size
            for (int x = c.x(); x < c.x() + size.x(); x++) {
                for (int y = c.y(); y < c.y() + size.y(); y++) {
                    objects.add(new TileCoordinates(x, y));
                }
            }
        }
    }

    private void addFilled(final List<Coordinates> coordinates, final Coordinates size, final List<TileCoordinates> objects) {
        for (final Coordinates c : coordinates) {
            // Iterate up to size
            for (int x = (int) c.x(); x < (int) c.x() + (int) Math.ceil(size.x()); x++) {
                for (int y = (int) c.y(); y < (int) c.y() + (int) Math.ceil(size.y()); y++) {
                    objects.add(new TileCoordinates(x, y));
                }
            }
        }
    }

    private boolean isExcluded(int x, int y, RoomDirective r) {
        for (final TileCoordinates c : r.getWallExclusions()) {
            if (c.x() == x && c.y() == y) return true;
        }
        return false;
    }

    public TileCoordinates nextTile(final TileCoordinates start, final List<TileCoordinates> path) {
        if (path.size() == 0) return null;
        if (path.size() == 1) return path.get(0);
        // Check if the next two jumps would be a diagonal
        int dx = path.get(1).x() - start.x();
        int dy = path.get(1).y() - start.y();

        if (Math.abs(dx) == 1 && Math.abs(dy) == 1) {
            // Diagonal. See if we can form a square between both of the diffs
            TileCoordinates left = start.offsetX(dx);
            TileCoordinates right = start.offsetY(dy);
            TileCoordinates end = path.get(1);

            // If all 3 are open, we can move diagonally straight to end.
            if (!grid[left.x() - xOffset][left.y() - yOffset] && !grid[right.x() - xOffset][right.y() - yOffset]) {
                return end;
            }
        }
        return path.get(0);
    }
}
