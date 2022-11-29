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
    private ArrayList<PathStep> fullDronePath;

    private final PathFinder pathFinder;

    public Drone(WorldState worldState) {
        this.worldState = worldState;
        this.startPos = worldState.getDroneStartPos();
        this.currentPos = worldState.getDroneStartPos();
        this.movesRemaining = MAX_DRONE_MOVES;
        this.fullDronePath = new ArrayList<>();
        this.pathFinder = new PathFinder(worldState);
    }

    public void deliverOrders() {
        PriorityQueue<Order> orderPriorityQueue = this.getOrderQueue();
        while (orderPriorityQueue.size() > 0) {
            Order order = orderPriorityQueue.poll();
            // If order is valid and drone has enough moves to deliver, deliver order.
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
        ArrayList<PathStep> pathToStart = this.pathFinder.findPath(pathToRestaurant.get(pathToRestaurant.size() - 1).getToLngLat(), this.startPos);

        ArrayList<PathStep> fullOrderPath = new ArrayList<>();
        fullOrderPath.addAll(pathToRestaurant);
        fullOrderPath.addAll(pathToStart);

        for (PathStep pathStep : fullOrderPath) {
            pathStep.setOrderNo(order.getOrderNo());
            this.fullDronePath.add(pathStep);
            this.currentPos = pathStep.getToLngLat();
            this.movesRemaining--;
            // Extra hover step for collecting at restaurant and delivering at start position (Appleton).
            if (pathStep.getToLngLat().closeTo(order.getRestaurant().getLngLat()) ||
                    pathStep.getToLngLat().closeTo(this.startPos)) {
                PathStep hoverStep = new PathStep(pathStep.getToLngLat(), pathStep, null, pathStep.getTargetLngLat());
                this.fullDronePath.add(hoverStep);
                this.movesRemaining--;
            }
        }
        order.setOrderOutcome(OrderOutcome.Delivered);
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
            } else { // Order is not valid, so it is impossible to deliver.
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

    public ArrayList<PathStep> getFullDronePath() {
        return fullDronePath;
    }
}
