package uk.ac.ed.inf;

/**
 * Record to represent a move of the drone.
 * @param fromLngLat The LngLat object representing the starting point of the move.
 * @param toLngLat The LngLat object representing the end point of the move.
 * @param stepDirectionAngle The angle (w.r.t East) travelled to get from the starting point to the end point.
 * @param ticksSinceStartOfCalculation The time since the start of the pathfinding process, when this move
 *                                     was calculated and added to its path
 * @param orderNo The order number of the order for delivering which this move was made.
 */
public record DroneMove(LngLat fromLngLat, LngLat toLngLat, Double stepDirectionAngle,
                        long ticksSinceStartOfCalculation, String orderNo) {

}
