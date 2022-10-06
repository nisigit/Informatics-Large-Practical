package uk.ac.ed.inf;

import java.io.IOException;

public record LngLat(double lng, double lat) {

    public boolean inCentralArea() {

        return true;
    }

    public double distanceTo(LngLat otherPoint) {
        double longDif = this.lng - otherPoint.lng;
        double latDif = this.lat - otherPoint.lat;
        return Math.sqrt(Math.pow(longDif, 2) + Math.pow(latDif, 2));
    }

    public boolean closeTo(LngLat otherPoint) {
        return true;
    }

    public LngLat nextPosition(CompassDirection direction) {
        return new LngLat(this.lng, this.lat);
    }

    public static void main(String[] args) throws IOException {
        Response response = Response.getInstance();

        response.getRestaurants();
    }
}
