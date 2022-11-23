package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/**
 * Record to represent a point/position.
 * @param lng to represent to longitude of the point
 * @param lat to represent the latitude of the point
 */
@JsonIgnoreProperties("name")
public record LngLat(@JsonProperty("longitude") double lng, @JsonProperty("latitude") double lat) {

    /**
     * Constant to store the distance (in degrees) that the drone covers in one move. Also, a measure
     * to check if two points are close to each other.
     */
    private static final double MOVE_LENGTH = 0.00015;

    /**
     * Method to check if the current LngLat point is close inside a polyon.
     * @param polygon An array of LngLat objects representing the vertices of the polygon.
     * @return true if the current LngLat point is inside the polygon, false otherwise.
     */
    private boolean inPolygon(LngLat[] polygon) {
        int numPoints = polygon.length;
        boolean isInside = false;
        for (int i = 0, j = numPoints - 1; i < numPoints; j = i++) {
            // If the point is on the edge of the polygon, return true.
            if (this.distanceTo(polygon[i]) + this.distanceTo(polygon[j]) ==
                    polygon[i].distanceTo(polygon[j])) {
                return true;
            }
            // Whether the latitude of vertice is north of this point and that of the other is south.
            boolean isNorthSouth = (polygon[i].lat > this.lat) != (polygon[j].lat > this.lat);
            // Whether the edge will intersect a ray towards positive x-axis (east) of the point.
            boolean eastIntersect = (this.lng < ((polygon[j].lng - polygon[i].lng) *
                    (this.lat - polygon[i].lat) / (polygon[j].lat - polygon[i].lat)) +
                    polygon[i].lng);
            if (isNorthSouth && eastIntersect) {
                isInside = !isInside;
            }
        }
        return isInside;
    }

    /**
     * Method to return whether a given point is inside the central area.
     * @return True if point is inside the central area. False otherwise.
     * @throws IOException If the REST server is not available or base url is invalid.
     */
    public boolean inCentralArea() throws IOException {
        LngLat[] centralVertices = ResponseFetcher.getInstance().getCentralArea();
        return this.inPolygon(centralVertices);
    }

    /**
     * Method to return whether a given point is inside a no-fly zone.
     * @return True if point is inside a no-fly zone. False otherwise.
     * @throws IOException If the REST server is not available or base url is invalid.
     */
    public boolean inNoFlyZone() throws IOException {
        ResponseFetcher responseFetcher = ResponseFetcher.getInstance();
        NoFlyZone[] noFlyZones = responseFetcher.getNoFlyZones();
        for (NoFlyZone noFlyZone : noFlyZones) {
            if (this.inPolygon(noFlyZone.getCoordinatesLngLat())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to return the Euclidean distance between the current point and another point.
     * @param otherPoint the point distance is being calculated to.
     * @return The Euclidean distance between the current point and otherPoint.
     */
    public double distanceTo(LngLat otherPoint) {
        double longDif = this.lng - otherPoint.lng;
        double latDif = this.lat - otherPoint.lat;
        return Math.sqrt(Math.pow(longDif, 2) + Math.pow(latDif, 2));
    }

    /**
     * Method to return whether the current point and another point are within 0.00015 degrees of each other
     * @param otherPoint the point with which the closeness is being calculated to.
     * @return True if the current point is within 0.00015 degrees of the other point. False otherwise.
     */
    public boolean closeTo(LngLat otherPoint) {
        return this.distanceTo(otherPoint) < MOVE_LENGTH;
    }

    /**
     * Method to return a new LngLat object which represents the new position of the drone after moving 1 move length
     * (0.00015 degrees) from its current position in the given compass direction.
     * @param compassDirection the direction in which the drone is moving.
     * @return A new LngLat object representing the new position of the drone after moving in the given direction.
     */
    public LngLat nextPosition(CompassDirection compassDirection) {
        double radianAngle = Math.toRadians(compassDirection.getAngle());
        double newLng = this.lng + (MOVE_LENGTH * Math.cos(radianAngle));
        double newLat = this.lat + (MOVE_LENGTH * Math.sin(radianAngle));
        return new LngLat(newLng, newLat);
    }

}
