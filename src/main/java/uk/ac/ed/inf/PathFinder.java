package uk.ac.ed.inf;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class PathFinder {

    // Field to store a WorldState object, which has information about flying zones, so that the pathfinder
    // can avoid breaking any requirements for the drone's flight.
    private final WorldState worldState;

    // Field to store the initialisation time of the pathfinder, so that ticks since start of calculation can be
    // calculated for each calculated step during the pathfinding process.
    private final long startTime;

    /**
     * Constructor to initialise a new PathFinder object.
     * @param worldState WorldState object containing information about orders, flying zones, restaurants, etc.
     */
    public PathFinder(WorldState worldState) {
        this.worldState = worldState;
        this.startTime = System.nanoTime();
    }

    /**
     * Method to get the full delivery path for a drone to deliver a given order, including hovering over the
     * restaurant and the delivery location.
     * @param droneCurPos LngLat object representing the drone's location from which it will start collecting
     *                      and delivering the order.
     * @param deliveryPoint LngLat object representing the delivery location of the order.
     * @param restaurant LngLat object representing the restaurant location from which an order is to be picked up.
     * @return An ArrayList of PathStep objects representing every step/move to be made by the drone to deliver an order.
     */
    public ArrayList<PathStep> getFullDeliveryPath(LngLat droneCurPos, LngLat deliveryPoint, Restaurant restaurant) {
        ArrayList<PathStep> fullDeliveryPath = new ArrayList<>();
        ArrayList<PathStep> pathToRestaurant = this.findPath(droneCurPos, restaurant.getLngLat());
        addHoverStep(pathToRestaurant);
        LngLat collectionPoint = pathToRestaurant.get(pathToRestaurant.size() - 1).getToLngLat();

        ArrayList<PathStep> pathToStartPos = this.findPath(collectionPoint, deliveryPoint);
        addHoverStep(pathToStartPos);

        fullDeliveryPath.addAll(pathToRestaurant);
        fullDeliveryPath.addAll(pathToStartPos);
        return fullDeliveryPath;
    }

    /**
     * Method to add a hovering step at the end of a path, to represent the drone hovering over the
     * collection or delivery point.
     * @param path ArrayList of PathStep objects representing a path.
     */
    private void addHoverStep(ArrayList<PathStep> path) {
        PathStep finalStep = path.get(path.size() - 1);
        LngLat hoverPoint = finalStep.getToLngLat();
        PathStep hoverStep = new PathStep(hoverPoint, finalStep, null, finalStep.getTargetLngLat(), System.nanoTime() - this.startTime);
        path.add(hoverStep);
    }

    /**
     * Finds a one-way path from a start point to an end point, using weighted A* search. The method
     * returns a list of PathStep objects, each of which represents a move to be made by the drone.
     * @param start The start point of the path.
     * @param end   The end point of the path.
     * @return An ArrayList of LngLat points representing the path. Null if no path is found.
     */
    private ArrayList<PathStep> findPath(LngLat start, LngLat end) {
        // Priority queue to store the nodes to be explored, sorted by their fCost.
        PriorityQueue<PathStep> openList = new PriorityQueue<>(Comparator.comparingDouble(PathStep::getFCost));
        ArrayList<PathStep> closedList = new ArrayList<>();
        PathStep startPathStep = new PathStep(start, end, System.nanoTime()- this.startTime);
        openList.add(startPathStep);

        while (openList.size() > 0) { // While there are still valid steps to explore.
            PathStep curPathStep = openList.poll();
            closedList.add(curPathStep);
            // Traverse all the directions we can move towards from the current position.
            for (CompassDirection compassDirection : CompassDirection.values()) {
                LngLat neighbourLngLat = curPathStep.getToLngLat().nextPosition(compassDirection);
                PathStep neighbourPathStep =
                        new PathStep(neighbourLngLat, curPathStep, compassDirection.getAngle(), end, System.nanoTime() - this.startTime);
                boolean stepCrossesNfzBoundary = pathCrossesNoFlyZone(curPathStep.getToLngLat(), neighbourLngLat);
                boolean stepCrossesCaBoundary = pathCrossesCentralAreaBoundary(curPathStep.getToLngLat(), neighbourLngLat);

                // Skip moves/steps/nodes that enter a no-fly zone or cross the central area boundary, if already crossed.
                if (stepCrossesNfzBoundary || (stepCrossesCaBoundary && neighbourPathStep.isCaBoundaryCrossed())) {
                    continue;
                } else if (stepCrossesCaBoundary) { // If the next step crosses the central area boundary, set the flag to true.
                    neighbourPathStep.setCaBoundaryCrossed(true);
                }

                // If the next step takes us to the close to the end (target) point, return the generated path.
                if (neighbourPathStep.getToLngLat().closeTo(end)) {
                    return generatePathFromEnd(neighbourPathStep);
                }

                // If a step takes us to an unexplored point/coordinate, add it to the open list to be explored.
                if (!closedList.contains(neighbourPathStep)) {
                    openList.add(neighbourPathStep);
                } else if (closedList.get(closedList.indexOf(neighbourPathStep)).getFCost() > neighbourPathStep.getFCost()) {
                    // If a step that takes us to a point that has already been explored, but a better
                    // path (lower f cost) is found, explore the new step with the lower cost.
                    closedList.remove(neighbourPathStep);
                    openList.add(neighbourPathStep);
                }
            }
        }
        return null; // No valid path found.
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

    /**
     * Checks if the path between two points crosses a no-fly zone boundary.
     * @param curLngLat       start of the path.
     * @param neighbourLngLat end of the path.
     * @return True if the path crosses a no-fly zone boundary, false otherwise.
     */
    private boolean pathCrossesNoFlyZone(LngLat curLngLat, LngLat neighbourLngLat) {
        NoFlyZone[] noFlyZones = this.worldState.getNoFlyZones();
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
     */
    private boolean pathCrossesCentralAreaBoundary(LngLat curLngLat, LngLat neighbourLngLat) {
        LngLat[] centralAreaVertices = this.worldState.getCentralAreaVertices();
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
