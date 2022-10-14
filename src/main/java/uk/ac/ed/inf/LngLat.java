package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Record to represent a point/position.
 * @param lng to represent to longitude of the point
 * @param lat to represent the latitude of the point
 */
@JsonIgnoreProperties("name")
public record LngLat(@JsonProperty("longitude") double lng, @JsonProperty("latitude") double lat) {

    /**
     * Constant to store the distance (in degrees) that the drone covers in one move. Also, a measure
     * to check if if two points are close to each other.
     */
    private static final double MOVE_LENGTH = 0.00015;

    /**
     * Method to return whether a given point is inside the central area.
     * @return True if point is inside the central area. False otherwise.
     * @throws IOException
     */
    public boolean inCentralArea() throws IOException {
        ResponseFetcher responseFetcher = ResponseFetcher.getInstance();
        ArrayList<LngLat> centralVertices = responseFetcher.getCentralArea();
        int numPoints = centralVertices.size();

        // Number of times a ray to the right (east) from the point intercepts an edge of the area
        // (odd or even). If interceptions are odd, point is inside, else outside.
        boolean result = false;

        // Loop to check if the number of edges a ray from given point intercepts is odd or even.
        for (int i = 0, j = numPoints - 1; i < numPoints; j = i++) {
            // If point is on one of the edges/vertices, then it is inside the central area.
            if (this.distanceTo(centralVertices.get(i)) + this.distanceTo(centralVertices.get(j)) ==
                                centralVertices.get(i).distanceTo(centralVertices.get(j))) {
                return true;
            }

            // Whether the latitude of vertice is north of this point and that of the other is south.
            boolean isNorthSouth = (centralVertices.get(i).lat > this.lat) != (centralVertices.get(j).lat > this.lat);
            // Whether the edge will intersect a ray towards positive x-axis (east) of the point.
            boolean eastIntersect = (this.lng < ((centralVertices.get(j).lng - centralVertices.get(i).lng) *
                    (this.lat - centralVertices.get(i).lat) / (centralVertices.get(j).lat - centralVertices.get(i).lat)) +
                    centralVertices.get(i).lng);
            if (isNorthSouth && eastIntersect) {
                result = !result;
            }
        }
        return result;
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
        double pointDist = this.distanceTo(otherPoint);
        return pointDist < MOVE_LENGTH;
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
