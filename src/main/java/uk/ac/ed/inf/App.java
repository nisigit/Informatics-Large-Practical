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
                System.out.println("Order " + order.orderNo + " is valid");
            } else {
                invalidOrders++;
                System.out.println("Order " + order.orderNo + " is invalid");
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
        LngLat start = new LngLat(-3.186874, 55.944494);
        LngLat end = new LngLat(-3.1912869215011597, 55.945535152517735);

        PathFinder pathFinder = new PathFinder(worldState);

        ArrayList<LngLat> path = pathFinder.findPath(start, end);
        for (LngLat lngLat : path) {
            System.out.println("[" + lngLat.lng() + ", " + lngLat.lat() + "],");
        }
        LngLat fin = new LngLat(-3.1911798928079014, 55.94546984275254);
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
        LocalDate date = LocalDate.parse("2023-05-01");
        WorldState worldState = new WorldState(date);

//        printOrderStatus(worldState);
        printPath(worldState);
    }
}