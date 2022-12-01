package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;

/**
 * Singleton class to fetch responses from the REST server.
 */
public class ResponseFetcher {

    // Singleton class object.
    private static ResponseFetcher responseFetcher;

    // Field to store the base url of the REST server.
    private URL baseUrl;

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
     * @param baseUrlString String containing the base url of the REST server.
     * @throws MalformedURLException if the base url is invalid.
     */
    public void setBaseUrl(String baseUrlString) throws MalformedURLException {
        if (!baseUrlString.endsWith("/")) {
            baseUrlString += "/"; // Ensuring url ends with a slash so endpoints can be appended.
        }
        this.baseUrl = new URL(baseUrlString);
    }

    /**
     * Method to fetch a response from the REST server, given an endpoint and a
     * class to map the response to.
     * @return Object of the class passed in as the second parameter.
     * @param endpoint String containing the endpoint to fetch the response from.
     * @param classType The class type to map the response to.
     * @throws IOException if the REST server cannot be reached or the response cannot
     *                     be mapped to the class passed in as a parameter.
     */
    private <T> T getResponseFromRestServer(String endpoint, Class<T> classType) throws IOException {
        try {
            URL restServerUrl = new URL(this.baseUrl + endpoint);
            return new ObjectMapper().readValue(restServerUrl, classType);
        } catch (Exception e) {
            throw new IOException("Could not fetch response from REST server. Please check if base url is correct.");
        }
    }

    /**
     * Method to get an ArrayList of LngLat objects representing the vertices of the
     * central area from the REST server
     * @return An ArrayList of LngLat objects representing the vertices of the central area.
     * @throws IOException If the REST server is not available or base url is invalid.
     */
    public LngLat[] getCentralAreaFromRestServer() throws IOException {
        return getResponseFromRestServer("centralArea", LngLat[].class);
    }

    /**
     * Method to get an array of Order objects representing all the orders in the
     * system from the REST server.
     * @return An array of Order objects representing all the orders fetched from
     *         the REST server.
     * @throws IOException If the REST server is not running or the base url is invalid.
     */
    public Order[] getOrdersFromRestServer() throws IOException {
        return getResponseFromRestServer("orders", Order[].class);
    }

    /**
     * Method to get an array of Order objects representing the orders fetched from
     * the REST server for a given date.
     * @param date LocalDate object representing the date for which the orders are
     *             to be fetched.
     * @return An array of Order objects representing the orders fetched from the REST server.
     * @throws IOException If the REST server is not running or the base url is invalid.
     */
    public Order[] getOrdersFromRestServer(LocalDate date) throws IOException {
        String endPoint = "orders/" + date;
        return getResponseFromRestServer(endPoint, Order[].class);
    }

    /**
     * Method to get an array of Restaurant objects representing the restaurants
     * fetched from the REST server.
     * @return An array of Restaurant objects representing the restaurants fetched
     *         from the REST server.
     * @throws IOException If the REST server is not running or the base url is invalid.
     */
    public Restaurant[] getRestaurantsFromRestServer() throws IOException {
        return getResponseFromRestServer("restaurants", Restaurant[].class);
    }

    /**
     * Method to get an array of NoFlyZone objects representing the no-fly zones
     * fetched from the REST server.
     * @return An array of NoFlyZone objects representing the no-fly zones fetched
     *         from the REST server.
     * @throws IOException If the REST server is not running or the base url is invalid.
     */
    public NoFlyZone[] getNoFlyZonesFromRestServer() throws IOException {
        return getResponseFromRestServer("noFlyZones", NoFlyZone[].class);
    }

}
