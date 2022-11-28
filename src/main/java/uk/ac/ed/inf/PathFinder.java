package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class PathFinder {
    private final WorldState worldState;

    public PathFinder(WorldState worldState) {
        this.worldState = worldState;
    }

    /**
     * Finds a one-way path from a start point to an end point.
     *
     * @param start The start point of the path.
     * @param end   The end point of the path.
     * @return An ArrayList of LngLat points representing the path. Null if no path
     *         is found.
     */
    public ArrayList<PathStep> findPath(LngLat start, LngLat end) {
        PathStep startPathStep = new PathStep(start, end);
        PriorityQueue<PathStep> openList = new PriorityQueue<>(
                Comparator.comparingDouble(PathStep::getDistanceToTarget));
        openList.add(startPathStep);
        ArrayList<PathStep> closedList = new ArrayList<>();
        boolean crossedCentralAreaBoundaries = false;

        while (openList.size() > 0) {
            PathStep cur_pathStep = openList.poll();
            closedList.add(cur_pathStep);
            for (CompassDirection compassDirection : CompassDirection.values()) {
                LngLat neighbourLngLat = cur_pathStep.toLngLat.nextPosition(compassDirection);
                if (pathCrossesNoFlyZone(cur_pathStep.toLngLat, neighbourLngLat)) {
                    continue;
                }
                if (pathCrossesCentralAreaBoundary(cur_pathStep.toLngLat, neighbourLngLat)) {
                    if (crossedCentralAreaBoundaries) {
                        continue;
                    } else {
                        crossedCentralAreaBoundaries = true;
                    }
                }

                PathStep neighbourPathStep = new PathStep(neighbourLngLat, cur_pathStep, compassDirection, end);
                if (neighbourPathStep.toLngLat.closeTo(end)) {
                    return generatePathFromEnd(neighbourPathStep);
                }

                if (!closedList.contains(neighbourPathStep)) {
                    openList.add(neighbourPathStep);
                } else if (closedList.get(
                        closedList.indexOf(neighbourPathStep)).getDistanceToTarget() > neighbourPathStep.getDistanceToTarget()) {
                    openList.add(neighbourPathStep);
                }
            }
        }
        return null;
    }

    private ArrayList<PathStep> generatePathFromEnd(PathStep endPathStep) {
        ArrayList<PathStep> path = new ArrayList<>();
        PathStep curStep = endPathStep;
        while (curStep != null) {
            path.add(0, curStep);
            curStep = curStep.prevStep;
        }
        return path;
    }

    /**
     * Checks if the path between two points crosses a no-fly zone boundary.
     *
     * @param curLngLat       start of the path.
     * @param neighbourLngLat end of the path.
     * @return True if the path crosses a no-fly zone boundary, false otherwise.
     */
    private boolean pathCrossesNoFlyZone(LngLat curLngLat, LngLat neighbourLngLat) {
        NoFlyZone[] noFlyZones = this.worldState.getNoFlyZones();
        for (NoFlyZone noFlyZone : noFlyZones) {
            for (int i = 0; i < noFlyZone.getCoordinatesLngLat().length - 1; i++) {
                LngLat nfzVertex1 = noFlyZone.getCoordinatesLngLat()[i];
                LngLat nfzVertex2 = noFlyZone.getCoordinatesLngLat()[i + 1];
                if (segmentsIntercept(curLngLat, neighbourLngLat, nfzVertex1, nfzVertex2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method to check if a path crosses the central area boundary.
     *
     * @param curLngLat       start of the path.
     * @param neighbourLngLat end of the path.
     * @return True if the path crosses the central area boundary, false otherwise.
     */
    private boolean pathCrossesCentralAreaBoundary(LngLat curLngLat, LngLat neighbourLngLat) {
        LngLat[] centralAreaVertices = this.worldState.getCentralAreaVertices();
        for (int i = 0; i < centralAreaVertices.length - 1; i++) {
            LngLat caVertex1 = centralAreaVertices[i];
            LngLat caVertex2 = centralAreaVertices[i + 1];
            if (segmentsIntercept(curLngLat, neighbourLngLat, caVertex1, caVertex2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method return whether two line segments intercept each other.
     *
     * @param seg1Vertex1 first vertex of the first segment.
     * @param seg1Vertex2 second vertex of the first segment.
     * @param seg2Vertex1 first vertex of the second segment.
     * @param seg2Vertex2 second vertex of the second segment.
     * @return true if the two segments intercept each other, false otherwise.
     */
    private boolean segmentsIntercept(LngLat seg1Vertex1, LngLat seg1Vertex2, LngLat seg2Vertex1, LngLat seg2Vertex2) {
        // Using formula of slope and formula of a line to calculate intercept of the line segments.
        double seg1Slope = (seg1Vertex2.lat() - seg1Vertex1.lat()) / (seg1Vertex2.lng() - seg1Vertex1.lng());
        double seg1Intercept = seg1Vertex1.lat() - seg1Slope * seg1Vertex1.lng();
        double seg2Slope = (seg2Vertex2.lat() - seg2Vertex1.lat()) / (seg2Vertex2.lng() - seg2Vertex1.lng());
        double seg2Intercept = seg2Vertex1.lat() - seg2Slope * seg2Vertex1.lng();

        if (seg1Slope == seg2Slope && seg1Intercept == seg2Intercept) { // collinear segments.
            return false;
        }
        double intersectionLng = (seg2Intercept - seg1Intercept) / (seg1Slope - seg2Slope);

        // If the intersection point of the two lines is within the bounds of the two
        // segments, then the two segments intersect.
        return intersectionLng >= Math.min(seg1Vertex1.lng(), seg1Vertex2.lng())
                && intersectionLng <= Math.max(seg1Vertex1.lng(), seg1Vertex2.lng()) &&
                intersectionLng >= Math.min(seg2Vertex1.lng(), seg2Vertex2.lng())
                && intersectionLng <= Math.max(seg2Vertex1.lng(), seg2Vertex2.lng());
    }
}
