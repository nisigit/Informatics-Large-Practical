package uk.ac.ed.inf;

public class Drone {
    private final LngLat startPos;
    private LngLat currentPos;

    private int movesRemaining;

    public Drone(WorldState worldState) {
        this.startPos = worldState.getDroneStartPos();
        this.currentPos = worldState.getDroneStartPos();
        this.movesRemaining = 2000;
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
