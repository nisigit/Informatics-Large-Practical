package uk.ac.ed.inf;

public record DroneMove(LngLat fromLngLat, LngLat toLngLat, Double stepDirectionAngle,
                        long ticksSinceStartOfCalculation, String orderNo) {

}
