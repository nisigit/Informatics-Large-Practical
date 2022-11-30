package uk.ac.ed.inf;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Class to fetch and store information orders, restaurants, flying zones, etc. in the REST service for a given date.
 */
public class WorldState {

    /**
     * LngLat to represent the coordinates of Appleton tower, which is also the drone's starting position.
     */
    public static final LngLat APPLETON_TOWER_COORDINATES = new LngLat(-3.186874, 55.944494);

    // LngLat object to represent starting position coordinates of the drone.
    private final LngLat droneStartPos = APPLETON_TOWER_COORDINATES;

    // Array to store Restaurant objects, representing the restaurants in the service, fetched from the REST server.
    private Restaurant[] restaurants;

    // Array to store the vertices of the central area, fetched from the REST server.
    private LngLat[] centralAreaVertices;

    // Array to store array of NoFlyZone objects, representing the no-fly zones in the service.
    private NoFlyZone[] noFlyZones;

    // Array to store Order objects, representing the orders in the service for a given day, fetched from the REST server.
    private Order[] orders;

    // Field to store the data for which information about orders, restaurants, flying zones etc. is being fetched.
    private final LocalDate date;

    /**
     * Class constructor for initialising a WorldState object for a given day.
     * @param date the date for which information from the REST server is being fetched for.
     */
    public WorldState(LocalDate date) throws IOException {
        this.date = date;
        this.initialiseWorldState();
    }

    /**
     * Method called by the class constructor to initialise the world state i.e, fetch and store the central area,
     * no-fly-zones, orders and restaurants, at the beginning of a day the drone delivery service is running.
     * @throws IOException if the REST server cannot be reached or if the response from the REST server
     *                     could not be parsed.
     */
    private void initialiseWorldState() throws IOException {
        ResponseFetcher responseFetcher = ResponseFetcher.getInstance();
        this.noFlyZones = responseFetcher.getNoFlyZonesFromRestServer();
        this.centralAreaVertices = responseFetcher.getCentralAreaFromRestServer();
        this.restaurants = responseFetcher.getRestaurantsFromRestServer();
        this.orders = responseFetcher.getOrdersFromRestServer(this.date);
    }

    /**
     * Method to get an array of Order objects representing all the orders in the
     * system from the REST server, for a given date.
     * @return An array of Order objects representing all the orders fetched from
     *         the REST server for a given date.
     */
    public Order[] getOrders() {
        return this.orders;
    }

    /**
     * Method to get an array of Restaurant objects representing the participating
     * restaurants of the service, fetched from the REST server.
     * @return An array of Restaurant objects representing the participating
     *         restaurants fetched from the REST server.
     */
    public Restaurant[] getRestaurants() {
        return this.restaurants;
    }

    /**
     * Method to get an array of LngLat objects representing the vertices of the
     * central area, fetched from the rest server.
     * @return An array of LngLat objects representing the vertices of the central area.
     */
    public LngLat[] getCentralAreaVertices() {
        return this.centralAreaVertices;
    }

    /**
     * Method to get an array of NoFlyZone objects representing the no-fly zones
     * fetched from the REST server.
     * @return An array of NoFlyZone objects representing the no-fly zones fetched
     *         from the REST server.
     */
    public NoFlyZone[] getNoFlyZones() {
        return this.noFlyZones;
    }

    /**
     * Method to get the LngLat object representing the starting position of the
     * drone every day.
     * @return LngLat object representing the starting position of the drone every day.
     */
    public LngLat getDroneStartPos() {
        return this.droneStartPos;
    }

    /**
     * Method to get the date for which information about orders, restaurants, flying zones etc. has been fetched and set.
     * @return the date for which information about orders, restaurants, flying zones etc. has been fetched and set.
     */
    public LocalDate getDate() {
        return this.date;
    }
}
