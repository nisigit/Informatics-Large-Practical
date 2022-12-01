package uk.ac.ed.inf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Class to represent a drone which plans routes to deliver orders on
 * a given day.
 */
public class Drone {

    /**
     * Field representing the maximum number of moves a drone can make in a day.
     */
    public static final int MAX_DRONE_MOVES = 2000;

    /**
     * Field representing the distance (in degrees) a drone travels in one move.
     */
    public static final double MOVE_LENGTH = 0.00015;

    /**
     * LngLat object representing the coordinates of Appleton Tower.
     */
    public static final LngLat APPLETON_TOWER_COORDINATES = new LngLat(-3.186874, 55.944494);

    // LngLat object representing a drone's starting location.
    private final LngLat startPos;

    // LngLat object representing the drone's current location.
    private LngLat currentPos;

    // Number the moves the drone has remaining, before it runs out of battery.
    private int movesRemaining;

    // List storing all the steps taken by the drone for the current day.
    private ArrayList<PathStep> fullDronePath;

    // PathFinder object to plan a route between two locations.
    private final PathFinder pathFinder;

    // Field to store the time when the drone was initialised, so every drone move can be timed relative to this.
    private final long startTime;

    /**
     * Constructor to initialise a new drone object.
     */
    public Drone() {
        this.startPos = APPLETON_TOWER_COORDINATES;
        this.currentPos = this.startPos;
        this.movesRemaining = Drone.MAX_DRONE_MOVES;
        this.fullDronePath = new ArrayList<>();
        this.pathFinder = new PathFinder();
        this.startTime = System.nanoTime();
    }

    /**
     * Method to simulate delivering orders by a drone on a given day. The method prioritises
     * orders based on the number of moves required to deliver them, and then only delivers
     * an order if the drone has enough moves remaining to do so.
     */
    public void deliverOrders() throws IOException {
        // Get valid orders prioritised by less moves required to deliver them.
        PriorityQueue<Order> orderPriorityQueue = this.getOrderQueue();
        while (orderPriorityQueue.size() > 0) {
            // Getting the next valid order with the least moves required to deliver it.
            Order order = orderPriorityQueue.poll();

            // Full path to collect the order from the restaurant and deliver it to Appleton.
            ArrayList<PathStep> fullOrderPath =
                    this.getFullDeliveryPath(order);
            // If drone has enough moves to deliver the order, then deliver it.
            if (fullOrderPath.size() <= this.movesRemaining) {
                deliverOrder(order, fullOrderPath);
            } else { // drone does not have enough battery to deliver any more orders.
                break;
            }
        }
    }

    /**
     * Method to simulate delivering a single order by a drone. The method calls the getFullDeliveryPath in
     * PathFinder to get the full path for collecting and delivering an order, and also updates the drone's
     * current position, drone's moves remaining, the order no. for which each step was taken, and
     * the order's outcome. The method also adds the steps taken to the drone's full path.
     * @param order Order object representing the order to be delivered by the drone.
     */
    private void deliverOrder(Order order, ArrayList<PathStep> fullOrderPath) {
        for (PathStep pathStep : fullOrderPath) {
            pathStep.setOrderNo(order.getOrderNo());
            this.fullDronePath.add(pathStep);
            this.currentPos = pathStep.getToLngLat();
            this.movesRemaining--;
        }
        order.setOrderOutcome(OrderOutcome.Delivered);
    }

    /**
     * Method to get the full path for collecting and delivering an order. The method gets the path to collect
     * an order from its restaurant and also the path to bring it back to the delivery location (start point).
     * The method also calls the addHoverStep method to add hover steps for collecting and delivering an order.
     * @param order Order object representing the order for which the full delivery path is to be found.
     * @return ArrayList of PathStep objects representing the full path for collecting and delivering an order.
     */
    private ArrayList<PathStep> getFullDeliveryPath(Order order) throws IOException {
        LngLat restLocation = order.getRestaurant().getLngLat();
        ArrayList<PathStep> pathToRestaurant = this.pathFinder.findPath(this.currentPos, restLocation, this.startTime);
        addHoverStep(pathToRestaurant);
        LngLat collectionPoint =  pathToRestaurant.get(pathToRestaurant.size() - 1).getToLngLat();

        // Delivery is at the start position (Appleton Tower)
        ArrayList<PathStep> pathToStart = this.pathFinder.findPath(collectionPoint, this.startPos, this.startTime);
        addHoverStep(pathToStart);

        ArrayList<PathStep> fullOrderPath = new ArrayList<>();
        fullOrderPath.addAll(pathToRestaurant);
        fullOrderPath.addAll(pathToStart);
        return fullOrderPath;
    }

    /**
     * Method to add a hover step at the end of a one-way path. This step is to simulate the drone
     * hovering over the collection or delivery point of an order.
     * @param path ArrayList of PathStep objects representing a one-way path.
     */
    private void addHoverStep(ArrayList<PathStep> path) {
        PathStep finalStep = path.get(path.size() - 1);
        LngLat hoverPoint = finalStep.getToLngLat();
        PathStep hoverStep = new PathStep(hoverPoint, finalStep, null,
                finalStep.getTargetLngLat(), System.nanoTime() - this.startTime);
        path.add(hoverStep);
    }

    /**
     * Method to return a priority queue of valid orders, sorted in increasing order of the number
     * of moves required to deliver them.
     * @return PriorityQueue of valid orders, sorted by the number of moves required to deliver them.
     */
    private PriorityQueue<Order> getOrderQueue() throws IOException {
        PriorityQueue<Order> orderPriorityQueue =
                new PriorityQueue<>(Comparator.comparingInt(Order::getMovesToDeliver));
        Order[] orders = DataFetcher.getInstance().getOrders();
        for (Order order : orders) {
            if (order.isOrderValid()) {
                ArrayList<PathStep> fullDeliveryPath = this.getFullDeliveryPath(order);
                order.setMovesToDeliver(fullDeliveryPath.size());
                orderPriorityQueue.add(order);
            }
        }
        return orderPriorityQueue;
    }

    /**
     * Method the get the number of moves the drone can make before it runs out of battery.
     * @return Number of moves the drone can make before it runs out of battery.
     */
    public int getMovesRemaining() {
        return this.movesRemaining;
    }

    /**
     * Method to get the full flight path, consisting of individual steps, taken by the drone on a given day.
     * @return ArrayList of PathStep, with each PathStep object representing
     * a step taken by the drone on a given day.
     */
    public ArrayList<PathStep> getFullDronePath() {
        return this.fullDronePath;
    }
}
