package uk.ac.ed.inf;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class PathFinder {
    private final WorldState worldState;

    private final long startTime;

    public PathFinder(WorldState worldState) {
        this.worldState = worldState;
        this.startTime = System.nanoTime();
    }

    public ArrayList<PathStep> getFullDeliveryPath(LngLat droneStartPos, Restaurant restaurant) {
        ArrayList<PathStep> fullDeliveryPath = new ArrayList<>();
        ArrayList<PathStep> pathToRestaurant = this.findPath(droneStartPos, restaurant.getLngLat());
        PathStep finalStepToRestaurant = pathToRestaurant.get(pathToRestaurant.size() - 1);
        LngLat collectionPoint = finalStepToRestaurant.getToLngLat();
        PathStep collectionHover = new PathStep(collectionPoint, finalStepToRestaurant, null, collectionPoint, System.nanoTime() - this.startTime);
        pathToRestaurant.add(collectionHover);

        ArrayList<PathStep> pathToStartPos = this.findPath(collectionPoint, droneStartPos);
        PathStep finalStepToStartPos = pathToStartPos.get(pathToStartPos.size() - 1);
        LngLat deliveryPoint = finalStepToStartPos.getToLngLat();
        PathStep deliveryHover = new PathStep(deliveryPoint, finalStepToStartPos, null, deliveryPoint, System.nanoTime() - this.startTime);
        pathToStartPos.add(deliveryHover);

        fullDeliveryPath.addAll(pathToRestaurant);
        fullDeliveryPath.addAll(pathToStartPos);
        return fullDeliveryPath;
    }


    /**
     * Finds a one-way path from a start point to an end point.
     * @param start The start point of the path.
     * @param end   The end point of the path.
     * @return An ArrayList of LngLat points representing the path. Null if no path is found.
     */
    public ArrayList<PathStep> findPath(LngLat start, LngLat end) {
        PathStep startPathStep = new PathStep(start, end, System.nanoTime()- this.startTime);
        PriorityQueue<PathStep> openList = new PriorityQueue<>(Comparator.comparingDouble(PathStep::getDistanceToTarget));
        openList.add(startPathStep);
        ArrayList<PathStep> closedList = new ArrayList<>();

        while (openList.size() > 0) {
            PathStep curPathStep = openList.poll();
            closedList.add(curPathStep);
            for (CompassDirection compassDirection : CompassDirection.values()) {
                LngLat neighbourLngLat = curPathStep.getToLngLat().nextPosition(compassDirection);
                PathStep neighbourPathStep = new PathStep(neighbourLngLat, curPathStep, compassDirection.getAngle(), end, System.nanoTime() - this.startTime);
                boolean stepCrossesNfz = pathCrossesNoFlyZone(curPathStep.getToLngLat(), neighbourLngLat);
                boolean stepCrossesCaBoundary = pathCrossesCentralAreaBoundary(curPathStep.getToLngLat(), neighbourLngLat);
                if (stepCrossesNfz || (stepCrossesCaBoundary && neighbourPathStep.isCaBoundaryCrossed())) {
                    continue;
                }
                neighbourPathStep.setCaBoundaryCrossed(stepCrossesCaBoundary);

                if (neighbourPathStep.getToLngLat().closeTo(end)) {
                    return generatePathFromEnd(neighbourPathStep);
                }
                if (!closedList.contains(neighbourPathStep)) {
                    openList.add(neighbourPathStep);
                } else if (closedList.get(closedList.indexOf(neighbourPathStep)).getDistanceToTarget() >
                            neighbourPathStep.getDistanceToTarget()) {
                    closedList.remove(neighbourPathStep);
                    openList.add(neighbourPathStep);
                }
            }
        }
        return null; // No valid path found.
    }

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
     *
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
            Line2D caEdge = new Line2D.Double(caVertex1.lng(), caVertex1.lat(), caVertex2.lng(), caVertex2.lat());
            if (caEdge.intersectsLine(curLngLat.lng(), curLngLat.lat(),
                                neighbourLngLat.lng(), neighbourLngLat.lat())) {
                return true;
            }
        }
        return false;
    }

}
