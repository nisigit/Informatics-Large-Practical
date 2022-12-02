package uk.ac.ed.inf;

/**
 * Enum to represent compass directions and the angle (in degrees) they form
 * with the positive (east-facing) x-axis (latitude).
 */
public enum CompassDirection {
    N(90.0),
    NNE(67.5),
    NE(45.0),
    ENE(22.5),
    E(0.0),
    ESE(337.5),
    SE(315.0),
    SSE(292.5),
    S(270.0),
    SSW(247.5),
    SW(225.0),
    WSW(202.5),
    W(180.0),
    WNW(157.5),
    NW(135.0),
    NNW(112.5);

    // Field to store angle (in degrees) a direction forms with positive x-axis (longitude)
    private final Double angle;

    /**
     * Enum constructor.
     * @param angle angle (in degrees) between the direction represented by the CompassDirection enum
     *              constant and the x-axis (east direction).
     */
    CompassDirection(Double angle) {
        this.angle = angle;
    }

    /**
     * Method to get the angle (in degrees) formed between the direction represented
     * by the CompassDirection enum constant and the x-axis (east direction).
     * @return The angle between x-axis (east direction) and the direction
     *         represented by the enum.
     */
    public Double getAngle() {
        return this.angle;
    }

}
