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
     * returns a list of PathStep objects, each of which represents a move to be made by the drone.
     * @param start The start point of the path.
     * @param end   The end point of the path.
     * @param startTime The time at which the drone started calculating paths.
     * @return An ArrayList of LngLat points representing the path. Null if no path is found.
     */
    public ArrayList<PathStep> findPath(LngLat start, LngLat end, long startTime) throws IOException {
        // Priority queue to store the nodes to be explored, sorted by their fCost.
        PriorityQueue<PathStep> openList = new PriorityQueue<>(Comparator.comparingDouble(PathStep::getFCost));
        ArrayList<PathStep> closedList = new ArrayList<>();
        PathStep startPathStep = new PathStep(start, end, System.nanoTime() - startTime);
        openList.add(startPathStep);

        while (openList.size() > 0) { // While there are still valid steps to explore.
            PathStep curPathStep = openList.poll();
            closedList.add(curPathStep);
            // Traverse all the directions we can move towards from the current position.
            for (CompassDirection compassDirection : CompassDirection.values()) {
                LngLat neighbourLngLat = curPathStep.getToLngLat().nextPosition(compassDirection);
                PathStep neighbourStep = new PathStep(neighbourLngLat, curPathStep, compassDirection.getAngle(), end, System.nanoTime() - startTime);
                boolean stepCrossesNfzBoundary = pathCrossesNoFlyZone(curPathStep.getToLngLat(), neighbourLngLat);
                boolean stepCrossesCaBoundary = pathCrossesCentralAreaBoundary(curPathStep.getToLngLat(), neighbourLngLat);

                // Skip moves/steps/nodes that enter a no-fly zone or cross the central area boundary, if already crossed.
                if (stepCrossesNfzBoundary || (stepCrossesCaBoundary && neighbourStep.isCaBoundaryCrossed())) {
                    continue;
                } else if (stepCrossesCaBoundary) { // If the next step crosses the central area boundary, set the flag to true.
                    neighbourStep.setCaBoundaryCrossed(true);
                }

                // If the next step takes us to the close to the end (target) point, return the generated path.
                if (neighbourStep.getToLngLat().closeTo(end)) {
                    return generatePathFromEnd(neighbourStep);
                }

                // If a step takes us to an unexplored point/coordinate, add it to the open list to be explored.
                if (!closedList.contains(neighbourStep)) {
                    openList.add(neighbourStep);
                } else if (closedList.get(closedList.indexOf(neighbourStep)).getFCost() > neighbourStep.getFCost()) {
                    // If a step that takes us to a point has already been explored, but a better
                    // path (lower f cost) is found, explore the new step with the lower cost.
                    closedList.remove(neighbourStep);
                    openList.add(neighbourStep);
                }
            }
        }
        return null; // No valid path found.
    }


    public ArrayList<Node> calculatePath(LngLat startPoint, LngLat endPoint, long startTime) throws IOException {
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
     * Method to generate a path consisting of individual steps, after a final step reaches close to its
     * target, by traversing each step and its parent, until the first step in the path.
     * @param endPathStep The final step that reaches close to the target point.
     * @return An ArrayList of PathStep objects representing the steps/moves to get from one point to another.
     */
    private ArrayList<PathStep> generatePathFromEnd(PathStep endPathStep) {
        ArrayList<PathStep> path = new ArrayList<>();
        PathStep curStep = endPathStep;
        while (curStep.getPrevStep() != null) {
            path.add(0, curStep);
            curStep = curStep.getPrevStep();
        }
        return path;
    }


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
     * Checks if the path between two points crosses a no-fly zone boundary.
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
     * Method to check if a path crosses the central area boundary.
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
