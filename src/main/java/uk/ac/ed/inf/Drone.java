package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Class to represent a drone which plans routes to deliver orders on
 * a given day.
 */
public class Drone {

    /**
     * Field representing the  maximum number of moves a drone can make in a day.
     */
    public static final int MAX_DRONE_MOVES = 2000;

    /**
     * Field representing the distance (in degrees) a drone travels in one move.
     */
    public static final double MOVE_LENGTH = 0.00015;

    // LngLat object representing a drone's starting location.
    private final LngLat startPos;

    // WorldState object containing information about orders, flying zones, restaurants, etc.
    private final WorldState worldState;

    // LngLat object representing the drone's current location.
    private LngLat currentPos;

    // Number the moves the drone has remaining, before it runs out of battery.
    private int movesRemaining;

    // List storing all the steps taken by the drone for the current day.
    private ArrayList<PathStep> fullDronePath;

    // PathFinder object to plan a route between two locations.
    private final PathFinder pathFinder;

    /**
     * Constructor to initialise a new drone object.
     * @param worldState WorldState object containing information about orders,
     *                   flying zones, restaurants, etc.
     */
    public Drone(WorldState worldState) {
        this.worldState = worldState;
        this.startPos = worldState.getDroneStartPos();
        this.currentPos = worldState.getDroneStartPos();
        this.movesRemaining = MAX_DRONE_MOVES;
        this.fullDronePath = new ArrayList<>();
        this.pathFinder = new PathFinder(worldState);
    }

    /**
     * Method to simulate delivering orders by a drone on a given day. The method prioritises
     * orders based on the number of moves required to deliver them, and then only delivers
     * orders if the drone has enough moves remaining to do so.
     *
     */
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

    /**
     * Method to simulate delivering a single order by a drone. The method calls the getFullDeliveryPath in
     * PathFinder to get the full path for collecting and delivering an order, and also updates the
     * drone's current position, drone's moves remaining, the order no. each step was taken, and
     * the order's outcome. The method also adds the steps taken to the drone's full path.
     * @param order Order object representing the order to be delivered by the drone.
     */
    private void deliverOrder(Order order) {

        ArrayList<PathStep> fullOrderPath = this.pathFinder.getFullDeliveryPath(this.currentPos, order.getRestaurant());
        for (PathStep pathStep : fullOrderPath) {
            pathStep.setOrderNo(order.getOrderNo());
            this.fullDronePath.add(pathStep);
            this.currentPos = pathStep.getToLngLat();
            this.movesRemaining--;
        }
        order.setOrderOutcome(OrderOutcome.Delivered);
    }

    /**
     * Method to return a priority queue of valid orders, sorted by the number of moves
     * required to deliver them.
     * @return PriorityQueue of valid orders, sorted by the number of moves required to deliver them.
     */
    private PriorityQueue<Order> getOrderQueue() {
        PriorityQueue<Order> orderPriorityQueue =
                new PriorityQueue<>(Comparator.comparingInt(Order::getMovesToRestaurant));
        Order[] orders = this.worldState.getOrders();
        for (Order order : orders) {
            if (order.isOrderValid(this.worldState)) {
                ArrayList<PathStep> pathToRestaurant =
                        this.pathFinder.findPath(this.currentPos, order.getRestaurant().getLngLat());
                order.setMovesToRestaurant(pathToRestaurant.size() + 1); // extra hover step at restaurant.
                orderPriorityQueue.add(order);
            }
        }
        return orderPriorityQueue;
    }

    /**
     * Method to get the drone's starting position on a given day.
     * @return LngLat object representing the drone's starting position.
     */
    public LngLat getStartPos() {
        return this.startPos;
    }

    /**
     * Method to return a LngLat object representing the drone's current location.
     * @return LngLat object representing the drone's current position.
     */
    public LngLat getCurrentPos() {
        return this.currentPos;
    }

    /**
     * Method the get the number of moves the drone can make before it runs out of battery.
     * @return Number of moves the drone can make before it runs out of battery.
     */
    public int getMovesRemaining() {
        return this.movesRemaining;
    }

    /**
     * Method to get the full path taken by the drone on a given day.
     * @return ArrayList of PathStep, with each PathStep object representing
     * a step taken by the drone on a given day.
     */
    public ArrayList<PathStep> getFullDronePath() {
        return this.fullDronePath;
    }
}
