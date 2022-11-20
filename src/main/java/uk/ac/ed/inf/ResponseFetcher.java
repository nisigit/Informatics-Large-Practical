package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class to fetch responses from the REST server.
 */
public class ResponseFetcher {

    // Object of the singleton class.
    private static ResponseFetcher responseFetcher;

    // Field to store the base url of the REST server.
    private String baseUrl;

    private Restaurant[] participants;

    /**
     * Class constructor.
     */
    private ResponseFetcher() {

    }

    /**
     * Method to return the single instance of the ResponseFetcher singleton class.
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
     * @param baseUrl String containing the base url of the REST server.
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Method to get an ArrayList of LngLat objects representing the vertices of the central area from the REST server
     * @return An ArrayList of LngLat objects representing the vertices of the central area.
     * @throws IOException If the REST server is not available or base url is invalid.
     */
    public ArrayList<LngLat> getCentralArea() throws IOException {
        if (baseUrl == null) {
            throw new IOException("Base url not set");
        }
        URL apiUrl = new URL(baseUrl + "/centralarea");
        LngLat[] centralAreaVertices = new ObjectMapper().readValue(
                apiUrl, LngLat[].class);

        return new ArrayList<>(List.of(centralAreaVertices));
    }

    /**
     * Method to get an array of Order objects representing the orders fetched from the REST server.
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
     * @return An array of Restaurant objects representing the restaurants fetched from the REST server.
     * @throws IOException If the REST server is not running or the base url is invalid.
     */
    public Restaurant[] getRestaurants() throws IOException {
        // Throw exception if baseUrl is not set
        if (participants != null) {
            return participants;
        }
        if (baseUrl == null) {
            throw new IllegalArgumentException("Base url not set");
        }

        URL apiUrl = new URL(baseUrl + "restaurants");

        participants =  new ObjectMapper().readValue(
                apiUrl, Restaurant[].class);
        return participants;
    }
}
