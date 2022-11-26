package uk.ac.ed.inf;

public class Drone {
    private final LngLat startPos;
    private LngLat currentPos;

    private int movesRemaining;

    public Drone(WorldState worldState) {
        this.startPos = worldState.getDroneStartPos();
        this.currentPos = worldState.getDroneStartPos();
        this.movesRemaining = WorldState.MAX_DRONE_MOVES;
    }

    public LngLat getStartPos() {
        return this.startPos;
    }

    public LngLat getCurrentPos() {
        return this.currentPos;
    }

    public int getMovesRemaining() {
        return this.movesRemaining;
    }

}
