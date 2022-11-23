package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NoFlyZone {

    // Field to store the name of the no-fly zone.
    @JsonProperty("name")
    public String name;

    // Array to store the vertices of the no-fly zone.
    @JsonProperty("coordinates")
    private double[][] coordinates;

    private LngLat[] coordinatesLngLat;

    /**
     * Class constructor.
     */
    public NoFlyZone() {

    }

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