package uk.ac.ed.inf;

import java.util.Objects;

public class Node {

    private static final double H_WEIGHT = 1.5;

    // Field to represent the parent node (point before) of this node.
    private final Node parent;

    // LngLat object to represent the location coordinates of this node.
    private final LngLat lngLat;

    // Field to store the angle (w.r.t East) travelled to get from the parent node to this node.
    private final Double angleFromParent;

    // Field to store the number of steps taken to get from the start node to this node.
    private final int stepsFromStart;

    // Field to store whether the path from the start node to this node crosses the CA boundary.
    private boolean isCaBoundaryCrossed;

    // Field to store the target point of the path.
    private final LngLat targetLngLat;

    // Field to store the time since the start of the pathfinding process, when this node was added to the path.
    private final long ticksSinceStartOfCalculation;

    /**
     * Class constructor to initialise a new Node instance, representing the starting point of a path.
     * @param lngLat The LngLat object representing the starting point of the path.
     * @param targetLngLat The LngLat object representing the target point of the path.
     * @param ticksSinceStartOfCalculation The time since the start of the pathfinding process,
     *                                     when this node was added to the path.
     */
    public Node(LngLat lngLat, LngLat targetLngLat, long ticksSinceStartOfCalculation) {
        this.lngLat = lngLat;
        this.parent = null;
        this.targetLngLat = targetLngLat;
        this.angleFromParent = null;
        this.stepsFromStart = 0;
        this.isCaBoundaryCrossed = false;
        this.ticksSinceStartOfCalculation = ticksSinceStartOfCalculation;
    }

    /**
     * Class constructor to initialise a new Node instance, representing a point in a path.
     * @param lngLat The LngLat object representing the location of this node.
     * @param parent The parent node (point before) of this node.
     * @param angleFromParent The angle (w.r.t East) travelled to get from the parent node to this node.
     * @param ticksSinceStartOfCalculation The time since the start of the pathfinding process,
     *                                     when this node was added to the path.
     */
    public Node(LngLat lngLat, Node parent, Double angleFromParent, long ticksSinceStartOfCalculation) {
        this.lngLat = lngLat;
        this.parent = parent;
        this.targetLngLat = parent.getTargetLngLat();
        this.angleFromParent = angleFromParent;
        this.stepsFromStart = parent.getStepsFromStart() + 1;
        this.isCaBoundaryCrossed = parent.isCaBoundaryCrossed();
        this.ticksSinceStartOfCalculation = ticksSinceStartOfCalculation;
    }

    /**
     * Method to get the number of steps taken to get from the start node to this node in a path.
     * @return The number of steps taken to get from the start node to this node in a path.
     */
    private int getStepsFromStart() {
        return this.stepsFromStart;
    }

    /**
     * Method to get the parent node (point before) of this node in a path.
     * @return The parent node (point before) of this node in a path.
     */
    public Node getParent() {
        return this.parent;
    }

    /**
     * Method to get the LngLat object representing the location coordinates of this node.
     * @return The LngLat object representing the location coordinates of this node.
     */
    public LngLat getLngLat() {
        return this.lngLat;
    }

    /**
     * Method to get the angle (w.r.t East) travelled to get from the parent node to this node.
     * @return The angle (w.r.t East) travelled to get from the parent node to this node.
     */
    public Double getAngleFromParent() {
        return angleFromParent;
    }

    /**
     * Method to get the target point of the path this node is part of.
     * @return The target point of the path this node is part of.
     */
    public LngLat getTargetLngLat() {
        return this.targetLngLat;
    }

    /**
     * Method to get whether the path from the start node to this node crosses the Central Area boundaries.
     * @return Whether the path from the start node to this node crosses the Central Area boundaries.
     */
    public boolean isCaBoundaryCrossed() {
        return this.isCaBoundaryCrossed;
    }

    /**
     * Method to set whether the path from the start node to this node crosses the Central Area boundaries.
     * @param isCaBoundaryCrossed Whether the path from the start node to this node crosses
     *                            the Central Area boundaries.
     */
    public void setIsCaBoundaryCrossed(boolean isCaBoundaryCrossed) {
        this.isCaBoundaryCrossed = isCaBoundaryCrossed;
    }

    /**
     * Method to get the time since the start of the pathfinding process,
     * when this node was added to the path.
     * @return The time since the start of the pathfinding process,
     *        when this node was added to the path.
     */
    public long getTicksSinceStartOfCalculation() {
        return this.ticksSinceStartOfCalculation;
    }

    /**
     * Method to get the weighted F cost of this node in the A* Search pathfinding algorithm. The
     * cost will be in terms of drone moves, where the G cost is the number of moves taken to reach
     * this node and the H cost is the estimated number of moves required to reach the target node.
     * @return The weighted F cost of this node in the A* Search pathfinding algorithm.
     */
    public double getFCost() {
        double gCost = this.stepsFromStart; // Number of moves from the start node to this node.

        // Estimated steps to get from this node to the target node (Euclidean_distance / move_length).
        double hCost = this.lngLat.distanceTo(this.targetLngLat) / Drone.MOVE_LENGTH;
        return gCost + H_WEIGHT * hCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(parent, node.parent) && Objects.equals(lngLat, node.lngLat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, lngLat);
    }
}
