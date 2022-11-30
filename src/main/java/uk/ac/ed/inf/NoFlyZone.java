package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class to represent a No-Fly Zone for the drone in the delivery service.
 */
public class NoFlyZone {

    // Field to store the name of the no-fly zone.
    @JsonProperty("name")
    private String name;

    // Array to store the vertices of the no-fly zone.
    @JsonProperty("coordinates")
    private double[][] coordinates;

    // Array to store the vertices of the no-fly zone as LngLat objects.
    private LngLat[] coordinatesLngLat;

    /**
     * Class constructor to initialise a new no-fly zone object.
     */
    public NoFlyZone() {

    }

    /**
     * Method to return a list of LngLat objects representing the vertices of the no-fly zone.
     * @return Array of LngLat objects representing the vertices of the no-fly zone.
     */
    public LngLat[] getCoordinatesLngLat() {
        if (this.coordinatesLngLat == null) {
            this.coordinatesLngLat = new LngLat[this.coordinates.length];
            for (int i = 0; i < coordinates.length; i++) {
                coordinatesLngLat[i] = new LngLat(coordinates[i][0], coordinates[i][1]);
            }
        }
        return this.coordinatesLngLat;
    }
}