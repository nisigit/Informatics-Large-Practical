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

    // List storing all the moves made by the drone in the day.
    private final ArrayList<DroneMove> allDroneMoves;

    // PathFinder object to plan a route between two locations.
    private final PathFinder pathFinder;

    // Field to store the time when the drone was initialised, so every calculated drone move
    // can be timed relative to this.
    private final long startTime;

    /**
     * Constructor to initialise a new drone object.
     */
    public Drone() {
        this.startPos = APPLETON_TOWER_COORDINATES;
        this.currentPos = this.startPos;
        this.movesRemaining = Drone.MAX_DRONE_MOVES;
        this.allDroneMoves = new ArrayList<>();
        this.pathFinder = new PathFinder();
        this.startTime = System.nanoTime();
    }

    /**
     * Method to simulate delivering orders by a drone on a given day. The method prioritises
     * orders based on the number of moves required to deliver them, and then only delivers
     * an order if the drone has enough moves remaining to do so.
     * @throws IOException, if the orders could not be fetched from the REST server.
     */
    public void deliverOrders() throws IOException {
        // Get valid orders prioritised by fewer moves required to deliver them.
        PriorityQueue<Order> orderPriorityQueue = this.getOrderQueue();
        while (orderPriorityQueue.size() > 0) {
            // Getting the next valid order with the least moves required to deliver it.
            Order order = orderPriorityQueue.poll();

            // Get full path to collect the order from the restaurant and deliver it back to drone's start position.
            ArrayList<DroneMove> fullOrderPath = this.getFullOrderPath(order);

            // If drone has enough moves to deliver the order, then deliver it.
            if (fullOrderPath.size() <= this.movesRemaining) {
                deliverOrder(order, fullOrderPath);
            } else { // drone does not have enough battery to deliver any more orders.
                break;
            }
        }
    }

    /**
     * Method to simulate delivering an order by the drone. It updates the drone's position and moves
     * remaining, step-by-step, as it delivers the order, and finally adds all the moves made to the list
     * of all moves made by the drone in the day and updates the order's outcome to OrderOutcome.Delivered.
     * @param order Order object representing the order being delivered by the drone.
     * @param fullOrderPath ArrayList of DroneMove objects representing the individual moves the drone must
     *                      take to successfully collect and deliver the order.
     */
    private void deliverOrder(Order order, ArrayList<DroneMove> fullOrderPath) {
        for (DroneMove droneMove : fullOrderPath) {
            this.allDroneMoves.add(droneMove);
            this.currentPos = droneMove.toLngLat();
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
     * @throws IOException If information about no-fly zones or central area points cannot be read from the
     *                    REST server.
     */
    private ArrayList<DroneMove> getFullOrderPath(Order order) throws IOException {
        LngLat restLocation = order.getRestaurant().getLngLat();
        // Path to go from drone's current location to restaurant and collect the order.
        ArrayList<Node> pointsToRestaurant = this.pathFinder.findPath(this.currentPos, restLocation, this.startTime);
        ArrayList<DroneMove> collectionMoves = this.createDroneSteps(pointsToRestaurant, order);

        LngLat collectionPoint = pointsToRestaurant.get(pointsToRestaurant.size() - 1).getLngLat();

        // Path to go from order's collection point to drone's start position and deliver the order.
        ArrayList<Node> pointsToStart = this.pathFinder.findPath(collectionPoint, this.startPos, this.startTime);
        ArrayList<DroneMove> deliveryMoves = this.createDroneSteps(pointsToStart, order);

        ArrayList<DroneMove> fullOrderPath = new ArrayList<>();
        fullOrderPath.addAll(collectionMoves);
        fullOrderPath.addAll(deliveryMoves);
        return fullOrderPath;
    }

    /**
     * Method to create a list of DroneMove objects, representing each move the drone must make to follow points
     * in a one-way route between two locations. The method also calls the addHoverStep method to add a hover
     * step at the end of all moves, to represent the drone hovering at delivery and collection points of an order.
     * @param pathPoints ArrayList of Node objects representing the points in the route between two locations.
     * @param order Order object representing the order being delivered by the drone.
     * @return ArrayList of DroneMove objects representing the moves the drone must make to follow the route between
     * two locations.
     */
    private ArrayList<DroneMove> createDroneSteps(ArrayList<Node> pathPoints, Order order) {
        ArrayList<DroneMove> droneMoves = new ArrayList<>();
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            Node fromNode = pathPoints.get(i);
            Node toNode = pathPoints.get(i + 1);
            DroneMove droneMove = new DroneMove(fromNode.getLngLat(), toNode.getLngLat(), toNode.getAngleFromParent(),
                    toNode.getTicksSinceStartOfCalculation(), order.getOrderNo());
            droneMoves.add(droneMove);
        }
        addHoverMove(droneMoves);
        return droneMoves;
    }

    /**
     * Method to add a hover move to the end of a list of DroneMove objects, to represent the drone hovering at
     * delivery and collection points of an order.
     * @param droneMoves ArrayList of DroneMove objects representing the moves the drone must make to follow the
     *                   route between two locations.
     */
    private void addHoverMove(ArrayList<DroneMove> droneMoves) {
        DroneMove lastMove = droneMoves.get(droneMoves.size() - 1);
        LngLat hoverPoint = lastMove.toLngLat();
        DroneMove hoverMove = new DroneMove(hoverPoint, hoverPoint, null,
                System.nanoTime() - this.startTime, lastMove.orderNo());
        droneMoves.add(hoverMove);
    }

    /**
     * Method to return a priority queue of valid orders, sorted in increasing order of the number
     * of moves required to deliver them.
     * @return PriorityQueue of valid orders, sorted by the number of moves required to deliver them.
     * @throws IOException If data from the REST server cannot be read.
     */
    private PriorityQueue<Order> getOrderQueue() throws IOException {
        // Prioritise orders by fewer moves required to deliver them.
        PriorityQueue<Order> orderPriorityQueue =
                new PriorityQueue<>(Comparator.comparingInt(Order::getMovesToDeliver));
        Order[] orders = DataFetcher.getInstance().getOrders();
        for (Order order : orders) {
            if (order.isOrderValid()) {
                ArrayList<DroneMove> fullDeliveryPath = this.getFullOrderPath(order);
                // Approximate number of moves required to deliver the order.
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
     * Method to get the full flight path (all moves made by the drone), consisting of individual moves, taken
     * by the drone on a given day.
     * @return ArrayList of DroneMove objects, with each DroneMove object representing a move made by the drone.
     */
    public ArrayList<DroneMove> getAllDroneMoves() {
        return allDroneMoves;
    }
}
