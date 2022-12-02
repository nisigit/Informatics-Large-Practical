package uk.ac.ed.inf;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Main class to run the application.
 */
public class App {

    /**
     * Calls the deliverOrders() method in the Drone class to deliver valid orders on a given day, and
     * prints the number of valid orders, number of delivered orders and remaining moves of the drone.
     * @param drone Drone object representing the drone that delivered orders.
     * @throws IOException if the orders could not be fetched from the REST server.
     */
    private static void printDeliveryInformation(Drone drone) throws IOException {
        Order[] orders = DataFetcher.getInstance().getOrders();
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

            // Setting the base url of the REST server in the ResponseFetcher class, and the date for which
            // data is being fetched.
            DataFetcher dataFetcher = DataFetcher.getInstance();
            dataFetcher.setBaseUrl(baseUrl);
            dataFetcher.setDate(date);

            // Initialise the drone object.
            Drone drone = new Drone();

            System.out.println("Delivering orders for date: " + dataFetcher.getDate());
            drone.deliverOrders();

            printDeliveryInformation(drone); // Deliver orders for the given date.

            JsonMaker.createDeliveriesJson(); // Create deliveries JSON file.
            JsonMaker.createFlightPathJson(drone.getAllDroneMoves()); // Create JSON file for flight path.
            JsonMaker.createDroneGeoJson(drone.getAllDroneMoves()); // Create GeoJSON file for drone flight path.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}