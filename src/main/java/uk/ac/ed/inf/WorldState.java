package uk.ac.ed.inf;

import java.io.IOException;
import java.time.LocalDate;

public class WorldState {
    public static final LngLat APPLETON_TOWER_COORDINATES = new LngLat(-3.186874, 55.944494);

    private final LngLat droneStartPos = APPLETON_TOWER_COORDINATES;
    private Restaurant[] restaurants;
    private LngLat[] centralAreaVertices;
    private NoFlyZone[] noFlyZones;
    private Order[] orders;
    private final LocalDate date;

    /**
     * Class constructor for initialising a WorldState object.
     *
     * @param date the date the world state is being set for.
     */
    public WorldState(LocalDate date) throws IOException {
        this.date = date;
        this.initialiseWorldState();
    }

    /**
     * Method called by the class constructor to initialise the world state, at the
     * beginning of a day the drone delivery service is running.
     *
     * @throws IOException if the REST server cannot be reached or the response cannot
     *                     be mapped to the class passed in as the second parameter.
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
     *
     * @return An array of Order objects representing all the orders fetched from
     *         the REST server for a given date.
     */
    public Order[] getOrders() {
        return this.orders;
    }

    /**
     * Method to get an array of Restaurant objects representing the participating
     * restaurants of the service,
     * fetched from the REST server.
     *
     * @return An array of Restaurant objects representing the participating
     *         restaurants fetched from the REST server.
     */
    public Restaurant[] getRestaurants() {
        return this.restaurants;
    }

    /**
     * Method to get an array of LngLat objects representing the vertices of the
     * central area, fetched from the rest server.
     *
     * @return An array of LngLat objects representing the vertices of the central area.
     */
    public LngLat[] getCentralAreaVertices() {
        return this.centralAreaVertices;
    }

    /**
     * Method to get an array of NoFlyZone objects representing the no-fly zones
     * fetched from the REST server.
     *
     * @return An array of NoFlyZone objects representing the no-fly zones fetched
     *         from the REST server.
     */
    public NoFlyZone[] getNoFlyZones() {
        return this.noFlyZones;
    }

    /**
     * Method to get the LngLat object representing the starting position of the
     * drone every day.
     *
     * @return LngLat object representing the starting position of the drone every day.
     */
    public LngLat getDroneStartPos() {
        return this.droneStartPos;
    }

    public LocalDate getDate() {
        return date;
    }
}
