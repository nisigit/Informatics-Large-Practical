package uk.ac.ed.inf;

import java.util.Objects;

public class PathStep {

    private final PathStep prevStep;

    private final LngLat toLngLat;

    private final Double stepDirectionAngle;

    private final LngLat targetLngLat;

    private boolean caBoundaryCrossed;

    private String orderNo;

    private final long ticksSinceStartOfCalculation;


    /**
     * Constructor for the first step in a path.
     *
     * @param startPoint The starting point of the path.
     * @param targetLngLat LngLat object representing the target point of the path.
     */
    public PathStep(LngLat startPoint, LngLat targetLngLat, Long ticksSinceStartOfCalculation) {
        this.toLngLat = startPoint;
        this.targetLngLat = targetLngLat;
        this.prevStep = null;
        this.stepDirectionAngle = null;
        this.caBoundaryCrossed = false;
        this.ticksSinceStartOfCalculation = ticksSinceStartOfCalculation;
    }


    /**
     * Constructor for PathStep in a path.
     *
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
        this.ticksSinceStartOfCalculation = ticksSinceStartOfCalculation;
    }


    /**
     * Method to set the order number of the order that the drone is collecting or delivering during this step.
     *
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
    	return orderNo;
    }


    /**
     * Method to get the LngLat instance of the target point this path step in the pathfinding algorithm.
     *
     * @return The LngLat instance of the target point this path step in the pathfinding algorithm.
     */
    public LngLat getTargetLngLat() {
        return targetLngLat;
    }

    /**
     * Calculates and returns the Euclidean distance between the final point of this step and
     * the target point.
     *
     * @return The Euclidean distance between the final point of this step and the target point.
     */
    public double getDistanceToTarget() {
        return this.toLngLat.distanceTo(targetLngLat);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PathStep other = (PathStep) obj;
        return Objects.equals(this.toLngLat, other.toLngLat) &&
                Objects.equals(this.targetLngLat, other.targetLngLat);
    }

    public PathStep getPrevStep() {
        return prevStep;
    }

    public LngLat getToLngLat() {
        return toLngLat;
    }

    public Double getStepDirectionAngle() {
        return stepDirectionAngle;
    }

    public boolean isCaBoundaryCrossed() {
        return caBoundaryCrossed;
    }

    public void setCaBoundaryCrossed(boolean caBoundaryCrossed) {
        this.caBoundaryCrossed = caBoundaryCrossed;
    }

    public long getTicksSinceStartOfCalculation() {
        return ticksSinceStartOfCalculation;
    }
}
