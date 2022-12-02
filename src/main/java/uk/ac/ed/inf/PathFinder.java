package uk.ac.ed.inf;

import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class PathFinder {

    /**
     * Constructor to initialise a new PathFinder object.
     */
    public PathFinder() {

    }

    /**
     * Finds a one-way path from a start point to an end point, using weighted A* search. The method
     * returns a list of Node objects, each of which represents a point on the path (seperated by 1 drone move
     * length / 0.00015 degrees).
     * @param startPoint The start point of the path.
     * @param endPoint  The end point of the path.
     * @param startTime The time at which the drone started calculating paths.
     * @return An ArrayList of Node objects representing the path. Null if no path is found.
     * @throws IOException If the central area points or no-fly zone points could not be fetched from
     * the REST server.
     */
    public ArrayList<Node> findPath(LngLat startPoint, LngLat endPoint, long startTime) throws IOException {
        // Priority queue to store the nodes to be explored, sorted by their F cost.
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost));
        ArrayList<Node> closedList = new ArrayList<>();
        Node startNode = new Node(startPoint, endPoint, System.nanoTime() - startTime);
        openList.add(startNode);

        while (openList.size() > 0) {
            Node curNode = openList.poll(); // Exploring the node with the lowest F cost.
            closedList.add(curNode);
            for (CompassDirection direction : CompassDirection.values()) {
                LngLat neighbourLngLat = curNode.getLngLat().nextPosition(direction);
                Node neighbourNode = new Node(neighbourLngLat, curNode, direction.getAngle(), System.nanoTime() - startTime);
                boolean stepCrossesNfzBoundary = pathCrossesNoFlyZone(curNode.getLngLat(), neighbourLngLat);
                boolean stepCrossesCaBoundary = pathCrossesCentralAreaBoundary(curNode.getLngLat(), neighbourLngLat);

                // Skip moves/steps/nodes that enter a no-fly zone or cross the central area boundary, if already crossed.
                if (stepCrossesNfzBoundary || (stepCrossesCaBoundary && neighbourNode.isCaBoundaryCrossed())) {
                    continue;
                } else if (stepCrossesCaBoundary) { // If the next step crosses the central area boundary, set the flag to true.
                    neighbourNode.setIsCaBoundaryCrossed(true);
                }

                // If the next step takes us to the close to the end (target) point, return the generated path.
                if (neighbourNode.getLngLat().closeTo(endPoint)) {
                    return generatePathFromEnd(neighbourNode);
                }

                // If a step takes us to an unexplored point/coordinate, add it to the open list to be explored.
                if (!closedList.contains(neighbourNode)) {
                    openList.add(neighbourNode);
                } else if (closedList.get(closedList.indexOf(neighbourNode)).getFCost() > neighbourNode.getFCost()) {
                    // If a step that takes us to a point has already been explored, but a better
                    // path (lower f cost) is found, explore the new step with the lower cost.
                    closedList.remove(neighbourNode);
                    openList.add(neighbourNode);
                }
            }
        }
        return null; // No valid path found between the start and end points.
    }

    /**
     * Method to generate a path consisting of individual Node points, after a final step reaches close to its
     * target, by traversing each step and its parent, until the first point in the path.
     * @param endNode The final step that reaches close to the target point.
     * @return An ArrayList of Node objects, each representing a point on the path.
     */
    private ArrayList<Node> generatePathFromEnd(Node endNode) {
        ArrayList<Node> path = new ArrayList<>();
        Node curNode = endNode;
        while (curNode.getParent() != null) {
            path.add(0, curNode);
            curNode = curNode.getParent();
        }
        return path;
    }

    /**
     * Checks if a straight line path between two points crosses a no-fly zone boundary.
     * @param curLngLat       start of the path.
     * @param neighbourLngLat end of the path.
     * @return True if the path crosses a no-fly zone boundary, false otherwise.
     * @throws IOException If the no-fly zones cannot be fetched from the REST server.
     */
    private boolean pathCrossesNoFlyZone(LngLat curLngLat, LngLat neighbourLngLat) throws IOException {
        NoFlyZone[] noFlyZones = DataFetcher.getInstance().getNoFlyZones();
        for (NoFlyZone noFlyZone : noFlyZones) {
            LngLat[] nfz = noFlyZone.getCoordinatesLngLat();
            for (int i = 0; i < nfz.length - 1; i++) {
                Line2D nfzEdge = new Line2D.Double(nfz[i].lng(), nfz[i].lat(), nfz[i + 1].lng(), nfz[i + 1].lat());
                if (nfzEdge.intersectsLine(curLngLat.lng(), curLngLat.lat(),
                                    neighbourLngLat.lng(), neighbourLngLat.lat())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method to check if a straight line path between two points the central area boundary.
     * @param curLngLat       start of the path.
     * @param neighbourLngLat end of the path.
     * @return True if the path crosses the central area boundary, false otherwise.
     * @throws IOException If the central area cannot be fetched from the REST server.
     */
    private boolean pathCrossesCentralAreaBoundary(LngLat curLngLat, LngLat neighbourLngLat) throws IOException {
        LngLat[] centralAreaVertices = DataFetcher.getInstance().getCentralArea();
        for (int i = 0; i < centralAreaVertices.length - 1; i++) {
            LngLat caVertex1 = centralAreaVertices[i];
            LngLat caVertex2 = centralAreaVertices[i + 1];
            Line2D caEdge = new Line2D.Double(caVertex1.lng(), caVertex1.lat(), caVertex2.lng(), caVertex2.lat());
            if (caEdge.intersectsLine(curLngLat.lng(), curLngLat.lat(),
                                neighbourLngLat.lng(), neighbourLngLat.lat())) {
                return true;
            }
        }
        return false;
    }

}
