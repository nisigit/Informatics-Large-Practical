package uk.ac.ed.inf;

import java.time.LocalDate;

/**
 * Main class to run the application.
 */
public class App {

    /**
     * Calls the deliverOrders() method in the Drone class to deliver valid orders on a given day, and
     * prints the number of valid orders, number of delivered orders and remaining moves of the drone.
     * @param worldState the world state for the drone to deliver orders in.
     */
    private static void makeDeliveries(WorldState worldState, Drone drone) {
        System.out.println("Delivering orders for date: " + worldState.getDate());
        drone.deliverOrders();

        Order[] orders = worldState.getOrders();
        int validCount = 0;
        int delivered = 0;
        for (Order order : orders) {
            if (order.getOrderOutcome() == OrderOutcome.Delivered) {
                delivered++;
                validCount++;
            } else if (order.getOrderOutcome() == OrderOutcome.ValidButNotDelivered) {
                validCount++;
            }
        }
        System.out.println("Valid orders: " + validCount);
        System.out.println("Delivered: " + delivered);
        System.out.println("Drone moves remaining: " + drone.getMovesRemaining());
    }

    /**
     * Main method of the program to read the date and rest server url arguments, initialise
     * the world state for the given date and then call the makeDeliveries method.
     * @param args the date and rest server's base url arguments.
     */
    public static void main(String[] args) {
        try {
            LocalDate date = LocalDate.parse(args[0]);
            String baseUrl = args[1];
            System.out.println(baseUrl);
            if (!baseUrl.endsWith("/")) {
                throw new IllegalArgumentException("Invalid Rest API URL");
            }

            // Setting the base url of the REST server in the ResponseFetcher class.
            ResponseFetcher responseFetcher = ResponseFetcher.getInstance();
            responseFetcher.setBaseUrl(baseUrl);

            // Initialise the world state for given date.
            WorldState worldState = new WorldState(date);

            // Initialise the drone object.
            Drone drone = new Drone(worldState);

            makeDeliveries(worldState, drone); // Deliver orders for the given date.
            JsonMaker.createDeliveriesJson(worldState); // Create deliveries JSON file.
            JsonMaker.createFlightPathJson(drone, worldState); // Create JSON file for flight path.
            JsonMaker.createDroneGeoJson(drone, worldState); // Create GeoJSON file for drone flight path.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}