package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;

/**
 * Singleton class to fetch responses from the REST server.
 */
public class ResponseFetcher {

    // Object of the singleton class.
    private static ResponseFetcher responseFetcher;

    // Field to store the base url of the REST server.
    private String baseUrl;

    private Restaurant[] participants;

    private NoFlyZone[] noFlyZones;

    private LngLat[] centralAreaVertices;


    /**
     * Class constructor.
     */
    private ResponseFetcher() {

    }

    /**
     * Method to return the single instance of the ResponseFetcher singleton class.
     *
     * @return instance of the ResponseFetcher singleton class.
     */
    public static ResponseFetcher getInstance() {
        if (responseFetcher == null) {
            responseFetcher = new ResponseFetcher();
        }
        return responseFetcher;
    }

    /**
     * Method to set the base url of the REST server.
     *
     * @param baseUrl String containing the base url of the REST server.
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Method to get an ArrayList of LngLat objects representing the vertices of the central area from the REST server
     *
     * @return An ArrayList of LngLat objects representing the vertices of the central area.
     * @throws IOException If the REST server is not available or base url is invalid.
     */
    public LngLat[] getCentralArea() throws IOException {
        if (this.centralAreaVertices != null) {
            return this.centralAreaVertices;
        }

        if (baseUrl == null) {
            throw new IOException("Base url not set");
        }
        URL apiUrl = new URL(baseUrl + "/centralarea");
        this.centralAreaVertices = new ObjectMapper().readValue(
                apiUrl, LngLat[].class);

        return this.centralAreaVertices;
    }

    /**
     * Method to get an array of Order objects representing the orders fetched from the REST server.
     *
     * @return An array of Order objects representing the orders fetched from the REST server.
     * @throws IOException If the REST server is not running or the base url is invalid.
     */
    public Order[] getOrders() throws IOException {
        // Throw exception if baseUrl is not set
        if (baseUrl == null) {
            throw new IllegalArgumentException("Base url not set");
        }

        URL apiUrl = new URL(baseUrl + "/orders");

        return new ObjectMapper().readValue(
                apiUrl, Order[].class);
    }


    /**
     * Method to get an array of Restaurant objects representing the restaurants fetched from the REST server.
     *
     * @return An array of Restaurant objects representing the restaurants fetched from the REST server.
     * @throws IOException If the REST server is not running or the base url is invalid.
     */
    public Restaurant[] getRestaurants() throws IOException {
        if (this.participants != null) {
            return this.participants;
        }

        // Throw exception if baseUrl is not set
        if (baseUrl == null) {
            throw new IllegalArgumentException("Base url not set");
        }

        URL apiUrl = new URL(baseUrl + "restaurants");
        this.participants = new ObjectMapper().readValue(
                apiUrl, Restaurant[].class);
        return this.participants;
    }

    public NoFlyZone[] getNoFlyZones() throws IOException {
        if (this.noFlyZones != null) {
            return this.noFlyZones;
        }
        // Throw exception if baseUrl is not set
        if (baseUrl == null) {
            throw new IllegalArgumentException("Base url not set");
        }

        URL apiUrl = new URL(baseUrl + "/noflyzones");
        this.noFlyZones = new ObjectMapper().readValue(
                apiUrl, NoFlyZone[].class);

        return this.noFlyZones;
    }
}
