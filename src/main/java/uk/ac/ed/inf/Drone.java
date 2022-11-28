package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Drone {

    public static final int MAX_DRONE_MOVES = 2000;

    // The drone's starting position.
    private final LngLat startPos;

    private final WorldState worldState;

    // The drone's current position.
    private LngLat currentPos;

    // Number of moves the drone can make before it runs out of battery.
    private int movesRemaining;


    // List storing all the steps taken by the drone
    private ArrayList<PathStep> dronePath;

    private final PathFinder pathFinder;

    public Drone(WorldState worldState) {
        this.worldState = worldState;
        this.startPos = worldState.getDroneStartPos();
        this.currentPos = worldState.getDroneStartPos();
        this.movesRemaining = MAX_DRONE_MOVES;
        this.dronePath = new ArrayList<PathStep>();
        this.pathFinder = new PathFinder(worldState);
    }

    public void deliverOrders() {
        PriorityQueue<Order> orderPriorityQueue = this.getOrderQueue();
        while (orderPriorityQueue.size() > 0) {
            Order order = orderPriorityQueue.poll();
            if (order.getOrderOutcome() == OrderOutcome.ValidButNotDelivered &&
                    (2 * order.getMovesToRestaurant()) < this.movesRemaining) {
                deliverOrder(order);
            } else { // No more valid orders or drone does not have enough battery to deliver order.
                break;
            }
        }
    }

    private void deliverOrder(Order order) {
        ArrayList<PathStep> pathToRestaurant = this.pathFinder.findPath(this.currentPos, order.getRestaurant().getLngLat());

        ArrayList<PathStep> pathToStart = this.pathFinder.findPath(pathToRestaurant.get(pathToRestaurant.size() - 1).toLngLat, this.startPos);

        ArrayList<PathStep> fullOrderPath = new ArrayList<PathStep>();
        fullOrderPath.addAll(pathToRestaurant);
        fullOrderPath.addAll(pathToStart);

        for (PathStep pathStep : fullOrderPath) {
            pathStep.setOrderNo(order.getOrderNo());
            this.dronePath.add(pathStep);
            this.currentPos = pathStep.toLngLat;
            this.movesRemaining--;
            // Extra hover step for collecting at restaurant and delivering at start position (Appleton).
            if (pathStep.toLngLat.closeTo(order.getRestaurant().getLngLat()) ||
                    pathStep.toLngLat.closeTo(this.startPos)) {
                PathStep hoverStep = new PathStep(pathStep.toLngLat, pathStep, null, pathStep.toLngLat);
                this.dronePath.add(hoverStep);
            }
        }
        order.setOrderOutcome(OrderOutcome.Delivered);
        System.out.println("Moves remaining: " + this.movesRemaining);
    }



    private PriorityQueue<Order> getOrderQueue() {
        PriorityQueue<Order> orderPriorityQueue = new PriorityQueue<>(
                Comparator.comparingInt(Order::getMovesToRestaurant));
        Order[] orders = this.worldState.getOrders();
        for (Order order : orders) {
            int movesToRestaurant;
            if (order.isOrderValid(this.worldState)) {
                ArrayList<PathStep> pathToRestaurant = this.pathFinder.findPath(this.currentPos,
                        order.getRestaurant().getLngLat());
                movesToRestaurant = pathToRestaurant.size() + 1; // Adding 1 for hovering at the restaurant.
            } else {
                movesToRestaurant = Integer.MAX_VALUE;
            }
            order.setMovesToRestaurant(movesToRestaurant);
            orderPriorityQueue.add(order);
        }
        return orderPriorityQueue;
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
