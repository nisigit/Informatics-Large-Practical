package uk.ac.ed.inf;

/**
 * Enum to represent compass directions and the angle they would form with the
 * positive (east-facing) x-axis (latitude).
 */
public enum CompassDirection {
    N(90),
    NNE(67.5),
    NE(45),
    ENE(22.5),
    E(0),
    ESE(337.5),
    SE(315),
    SSE(292.5),
    S(270),
    SSW(247.5),
    SW(225),
    WSW(202.5),
    W(180),
    WNW(157.5),
    NW(135),
    NNW(112.5);

    // Field to store the angle a direction forms with the positive x-axis (longitude)
    private final double angle;

    /**
     * Enum constructor.
     * 
     * @param angle angle (in degrees) between the direction represented by the CompassDirection enum
     *              constant and the x-axis (east direction).
     */
    CompassDirection(double angle) {
        this.angle = angle;
    }

    /**
     * Method to get the angle (in degrees) formed between the direction represented
     * by the CompassDirection enum constant and the x-axis (east direction).
     * 
     * @return The angle between x-axis (east direction) and the direction
     *         represented by the enum.
     */
    public double getAngle() {
        return angle;
    }
}
