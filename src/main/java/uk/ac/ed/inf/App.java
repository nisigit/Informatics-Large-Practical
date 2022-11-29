package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Hello world!
 */
public class App {

    public static void printOrderStatus(WorldState worldState) throws IOException {
        ResponseFetcher responseFetcher = ResponseFetcher.getInstance();
        Order[] orders = worldState.getOrders();
        int validOrders = 0;
        int invalidOrders = 0;
        for (Order order : orders) {
            boolean validOrder = order.isOrderValid(worldState);
            if (validOrder) {
                validOrders++;
                System.out.println("Order " + order.getOrderNo() + " is valid");
            } else {
                invalidOrders++;
                System.out.println("Order " + order.getOrderNo() + ": " + order.getOrderOutcome());
            }
        }
        System.out.println("Total valid orders: " + validOrders);
        System.out.println("Total invalid orders: " + invalidOrders);
    }

    public static void printNoFlyZones() throws IOException {
        ResponseFetcher responseFetcher = ResponseFetcher.getInstance();
        NoFlyZone[] noFlyZones = responseFetcher.getNoFlyZonesFromRestServer();
        for (NoFlyZone noFlyZone : noFlyZones) {
            System.out.println("No fly zone: " + noFlyZone.name);
            for (LngLat lngLat : noFlyZone.getCoordinatesLngLat()) {
                System.out.println("LngLat: " + lngLat.lng() + ", " + lngLat.lat());
            }
        }
    }

    public static void printRestaurants() throws IOException {
        ResponseFetcher responseFetcher = ResponseFetcher.getInstance();
        Restaurant[] restaurants = responseFetcher.getRestaurantsFromRestServer();
        for (Restaurant restaurant : restaurants) {
            LngLat restLngLat = restaurant.getLngLat();
            System.out.println("Restaurant: " + restaurant.name);
            System.out.println("Location: " + restLngLat.lng() + ", " + restLngLat.lat());
        }
    }

    public static void printPath(WorldState worldState) throws IOException {
        LngLat end = new LngLat(-3.186874, 55.944494);
        LngLat start = new LngLat(-3.1940174102783203,
                55.94390696616939);

        PathFinder pathFinder = new PathFinder(worldState);

        ArrayList<PathStep> path = pathFinder.findPath(start, end);
        for (PathStep pathStep : path) {
            System.out.println("[" + pathStep.getToLngLat().lng() + ", " + pathStep.getToLngLat().lat() + "],");
        }

    }

    public static void deliveries(WorldState worldState) throws IOException {
        Drone drone = new Drone(worldState);
        drone.deliverOrders();
        System.out.println("Drone moves remaining: " + drone.getMovesRemaining());
        System.out.println("Drone final position: " + drone.getCurrentPos().lng() + ", " + drone.getCurrentPos().lat());

        Order[] orders = worldState.getOrders();
        int deliveredOrders = 0;
        int notDeliveredOrders = 0;
        int validOrders = 0;
        for (Order order : orders) {
            if (order.getOrderOutcome() == OrderOutcome.Delivered) {
                deliveredOrders++;
            } else {
                notDeliveredOrders++;
            }
            if (order.getOrderOutcome() == OrderOutcome.ValidButNotDelivered
                    || order.getOrderOutcome() == OrderOutcome.Delivered) {
                validOrders++;
            }
        }
        System.out.println("Valid orders: " + validOrders);
        System.out.println("Total delivered orders: " + deliveredOrders);
        System.out.println("Total not delivered orders: " + notDeliveredOrders);
        JsonMaker.createDeliveriesJson(worldState);
        JsonMaker.createFlightPathJson(drone, worldState);
    }

    public static void main(String[] args) throws IOException {
        // Take input for API url
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the base url of the REST server:");
        String baseUrl = scanner.nextLine();

        // Check if the url is valid
        try {
            URL url = new URL(baseUrl);
        } catch (Exception e) {
            System.err.println("Invalid url");
            return;
        }

        ResponseFetcher responseFetcher = ResponseFetcher.getInstance();
        responseFetcher.setBaseUrl(baseUrl);

        // Initialise the world state for given date.
        LocalDate date = LocalDate.parse("2023-01-01");
        WorldState worldState = new WorldState(date);
        deliveries(worldState);
    }
}