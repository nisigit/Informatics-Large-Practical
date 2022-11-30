package uk.ac.ed.inf;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Main class to run the application.
 */
public class App {

    /**
     * Initialises a drone object and calls the deliverOrders method to deliver orders
     * for a given date.
     *
     * @param worldState the world state for the drone to deliver orders in.
     * @throws IOException if the REST server cannot be reached or the response cannot
     */
    private static void makeDeliveries(WorldState worldState) throws IOException {
        Drone drone = new Drone(worldState);
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

        JsonMaker.createDeliveriesJson(worldState);
        JsonMaker.createFlightPathJson(drone, worldState);
        JsonMaker.createDroneGeoJson(drone, worldState);
    }

    /**
     * Main method of the program to read the date and rest server url arguments, initialise
     * the world state for the given date and then call the makeDeliveries method.
     *
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
            ResponseFetcher responseFetcher = ResponseFetcher.getInstance();
            responseFetcher.setBaseUrl(baseUrl);

            // Initialise the world state for given date.
            WorldState worldState = new WorldState(date);
            makeDeliveries(worldState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}