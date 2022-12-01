package uk.ac.ed.inf;

import java.util.Objects;

/**
 * Class to represent a step/move from one location to another. Each PathStep object
 * is also being used as a node in the A* search algorithm in the PathFinder class, where
 * the parent node is the previous step in the path and the g and h costs are the number
 * of parents (moves)
 */
public class PathStep {

    /**
     * Weighting factor for the h cost of a node/step in the weighted A* search algorithm.
     */
    private static final double H_WEIGHT = 1.5;

    // Field to represent the previous step taken before this step.
    private final PathStep prevStep;

    // LngLat object to represent the location coordinates that are reached after taking this step/move.
    private final LngLat toLngLat;

    // Angle in degrees between the direction of this step and the positive x-axis (east latitude).
    private final Double stepDirectionAngle;

    // The target point of the path this step is a part of.
    private final LngLat targetLngLat;

    // Field to store if this step, or a step before this step in the path, crosses the central area boundaries.
    private boolean caBoundaryCrossed;

    // Field to store the order no. of the order for whose delivery this step is being taken by the drone.
    private String orderNo;

    // Field to store the time since the start of the pathfinding process, when this step was added to the path.
    private final long ticksSinceStartOfCalculation;

    // Field to store the step no. of this step in the path it is a part of.
    private final int stepsSinceStart;


    /**
     * Constructor to initialise a new PathStep instance, to represent the starting point of a path.
     * @param startPoint The starting point of the path.
     * @param targetLngLat LngLat object representing the target point of the path.
     */
    public PathStep(LngLat startPoint, LngLat targetLngLat, long ticksSinceStartOfCalculation) {
        this.toLngLat = startPoint;
        this.targetLngLat = targetLngLat;
        this.ticksSinceStartOfCalculation = ticksSinceStartOfCalculation;
        this.prevStep = null;
        this.stepDirectionAngle = null;
        this.caBoundaryCrossed = false;
        this.stepsSinceStart = 0;
    }


    /**
     * Constructor to initialise a new PathStep instance, representing a step in a path.
     * @param toLngLat The LngLat instance representing the final location after this step.
     * @param prevStep The previous step in the path.
     * @param stepDirectionAngle The direction angle (w.r.t East) taken to go from the final point of
     *                           the previous step to the final point of this step.
     * @param targetLngLat LngLat instance representing target point of the path.
     */
    public PathStep(LngLat toLngLat, PathStep prevStep, Double stepDirectionAngle, LngLat targetLngLat, Long ticksSinceStartOfCalculation) {
        this.toLngLat = toLngLat;
        this.prevStep = prevStep;
        this.stepDirectionAngle = stepDirectionAngle;
        this.targetLngLat = targetLngLat;
        this.caBoundaryCrossed = prevStep.caBoundaryCrossed;
        this.stepsSinceStart = prevStep.stepsSinceStart + 1;
        this.ticksSinceStartOfCalculation = ticksSinceStartOfCalculation;
    }


    /**
     * Method to set the order number of the order that the drone is collecting or delivering during this step.
     * @param orderNo The order number of the order that the drone is collecting or delivering during this step.
     */
    public void setOrderNo(String orderNo) {
    	this.orderNo = orderNo;
    }

    /**
     * Method to get order number of the order that this step is associated with.
     * @return The order number of the order that this step is associated with.
     */
    public String getOrderNo() {
    	return this.orderNo;
    }


    /**
     * Method to get LngLat instance of the target point of the path this step is part of in the pathfinding algorithm.
     * @return The LngLat instance of the target point of the path this step is part of in the pathfinding algorithm.
     */
    public LngLat getTargetLngLat() {
        return this.targetLngLat;
    }

    /**
     * Method to get weighted f cost of this step in the A* search pathfinding algorithm. The cost will
     * be in terms of number of drone moves, where g cost is the number of moves taken so far and h cost
     * is the estimated number of moves to reach to the target point.
     * @return The weighted f cost of this step in the A* search pathfinding algorithm.
     */
    public double getFCost() {
        double gCost = this.stepsSinceStart; // Number of moves made so far.

        // Estimated number of moves to reach the target point (Euclidean_distance / move_length)
        double hCost = this.toLngLat.distanceTo(this.targetLngLat) / Drone.MOVE_LENGTH;
        return gCost + H_WEIGHT * hCost;
    }

    /**
     * Method to get the step taken before this step in the path. I.e. the parent node in the A* search algorithm.
     * @return PathStep instance representing the step taken before this step in the path.
     */
    public PathStep getPrevStep() {
        return this.prevStep;
    }

    /**
     * Method to get the LngLat instance representing the final location reached after taking this step.
     * @return LngLat instance representing the final location reached after taking this step.
     */
    public LngLat getToLngLat() {
        return this.toLngLat;
    }

    /**
     * Method to get the direction angle (degrees) (w.r.t East) taken to go from the final point of the previous step to the
     * @return The direction angle (degrees) (w.r.t East) taken to go from the final point of the previous step to the
     */
    public Double getStepDirectionAngle() {
        return this.stepDirectionAngle;
    }

    /**
     * Method to get the number of ticks passed since the start of the pathfinding process, when this step was explored
     * in the A* search pathfinding algorithm.
     * @return The number of ticks passed since the start of the pathfinding process, when this step was explored
     * in the A* search pathfinding algorithm.
     */
    public long getTicksSinceStartOfCalculation() {
        return this.ticksSinceStartOfCalculation;
    }

    /**
     * Method to get whether this step, or a step before this step in the path, crosses the central area boundaries.
     * @return True if this step, or a step before this step in the path, crosses the central area boundaries.
     * False otherwise.
     */
    public boolean isCaBoundaryCrossed() {
        return this.caBoundaryCrossed;
    }

    /**
     * Method to set whether this step, or a step before this step in the path, crosses the central area boundaries.
     * @param caBoundaryCrossed True if this step, or a step before this step in the path, crosses the central area boundaries.
     *                          False otherwise.
     */
    public void setCaBoundaryCrossed(boolean caBoundaryCrossed) {
        this.caBoundaryCrossed = caBoundaryCrossed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathStep pathStep = (PathStep) o;
        return Objects.equals(this.toLngLat, pathStep.toLngLat) &&
                Objects.equals(this.targetLngLat, pathStep.targetLngLat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toLngLat, targetLngLat);
    }
}
