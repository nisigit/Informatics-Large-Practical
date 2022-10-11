package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.ArrayList;

@JsonIgnoreProperties("name")
public record LngLat(@JsonProperty("longitude") double lng, @JsonProperty("latitude") double lat) {

    private static final double MOVE_LENGTH = 0.00015;

    public boolean inCentralArea() throws IOException {
        ResponseFetcher responseFetcher = ResponseFetcher.getInstance();
        ArrayList<LngLat> centralVertices = responseFetcher.getCentralArea();
        int numPoints = centralVertices.size();
        boolean result = false;

        for (int i = 0, j = numPoints - 1; i < numPoints; j = i++) {
            boolean wl = (centralVertices.get(i).lat > this.lat) != (centralVertices.get(j).lat > this.lat);

            boolean sm = (this.lng < (centralVertices.get(j).lng - centralVertices.get(i).lng) * (this.lat - centralVertices.get(i).lat) / (centralVertices.get(j).lat - centralVertices.get(i).lat) + centralVertices.get(i).lng);

            if (wl && sm) {
                result = !result;
            }
        }
        return result;
    }

    public double distanceTo(LngLat otherPoint) {
        double longDif = this.lng - otherPoint.lng;
        double latDif = this.lat - otherPoint.lat;
        return Math.sqrt(Math.pow(longDif, 2) + Math.pow(latDif, 2));
    }

    public boolean closeTo(LngLat otherPoint) {
        double pointDist = this.distanceTo(otherPoint);
        return pointDist < MOVE_LENGTH;
    }

    public LngLat nextPosition(CompassDirection compassDirection) {
        double radianAngle = Math.toRadians(compassDirection.getAngle());
        double newLng = this.lng + (MOVE_LENGTH * Math.cos(radianAngle));
        double newLat = this.lat + (MOVE_LENGTH * Math.sin(radianAngle));

        return new LngLat(newLng, newLat);
    }

}
