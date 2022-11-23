package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * Hello world!
 */
public class App {

    public static void printOrderStatus() throws IOException {
        ResponseFetcher responseFetcher = ResponseFetcher.getInstance();
        Order[] orders = responseFetcher.getOrdersFromRestServer();
        int validOrders = 0;
        int invalidOrders = 0;
        for (Order order : orders) {
            boolean validOrder = order.isOrderValid();
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
        responseFetcher.setBaseUrl("https://ilp-rest.azurewebsites.net/");

        LocalDate date = LocalDate.parse("2023-05-01");
        WorldState worldState = new WorldState(date);

        printOrderStatus();
    }
}