package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;

/**
 * Singleton class to fetch responses from the REST server.
 */
public class DataFetcher {

    // Singleton class object.
    private static DataFetcher dataFetcher;

    // Field to store the base url of the REST server.
    private URL baseUrl;

    // Field to store the date for which data is being fetched and stored.
    private LocalDate date;

    // Field to store the orders fetched from the REST server for the given date.
    private Order[] orders;

    // Field to store the participating restaurants fetched from the REST server for the given date.
    private Restaurant[] restaurants;

    // Field to store the no-fly zones fetched from the REST server for the given date.
    private NoFlyZone[] noFlyZones;

    // Array to store the vertices of the central area fetched from the REST server.
    private LngLat[] centralArea;

    /**
     * Class constructor to initialise the singleton class object.
     */
    private DataFetcher() {

    }

    /**
     * Method to return the single instance of the ResponseFetcher singleton class.
     * @return instance of the ResponseFetcher singleton class.
     */
    public static DataFetcher getInstance() {
        if (dataFetcher == null) {
            dataFetcher = new DataFetcher();
        }
        return dataFetcher;
    }

    /**
     * Method to set the base url of the REST server from which data will be fetched.
     * @param baseUrlString String containing the base url of the REST server.
     * @throws MalformedURLException if the base url is invalid.
     */
    public void setBaseUrl(String baseUrlString) throws MalformedURLException {
        if (!baseUrlString.endsWith("/")) {
            baseUrlString += "/"; // Ensuring url ends with a slash so endpoints can be appended.
        }
        this.baseUrl = new URL(baseUrlString);
    }


    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return this.date;
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
        if (this.baseUrl == null) {
            throw new IllegalStateException("Set the base url of the REST server before fetching data.");
        }
        try {
            URL restServerUrl = new URL(this.baseUrl + endpoint);
            return new ObjectMapper().readValue(restServerUrl, classType);
        } catch (Exception e) {
            throw new IOException("Could not fetch response from REST server. Please check if base url is correct.");
        }
    }

    /**
     * Method to get an ArrayList of LngLat objects representing the vertices of the
     * central area from the REST server.
     * @return An ArrayList of LngLat objects representing the vertices of the central area.
     * @throws IOException If the REST server is not available or base url is invalid.
     */
    public LngLat[] getCentralArea() throws IOException {
        if (this.centralArea == null) {
            this.centralArea = this.getResponseFromRestServer("centralarea", LngLat[].class);
        }
        return this.centralArea;
    }

    /**
     * Method to get an array of Order objects representing the orders fetched from
     * the REST server for a given date.
     * @return An array of Order objects representing the orders fetched from the REST server.
     * @throws IOException If the REST server is not running or the base url is invalid.
     */
    public Order[] getOrders() throws IOException {
        if (this.date == null) {
            throw new IllegalStateException("Please set the date for which orders are to be fetched.");
        }
        if (this.orders == null) {
            String endPoint = "orders/" + this.date;
            this.orders = this.getResponseFromRestServer(endPoint, Order[].class);
        }
        return this.orders;
    }

    /**
     * Method to get an array of Restaurant objects representing the restaurants
     * fetched from the REST server.
     * @return An array of Restaurant objects representing the restaurants fetched
     *         from the REST server.
     * @throws IOException If the REST server is not running or the base url is invalid.
     */
    public Restaurant[] getRestaurants() throws IOException {
        if (this.restaurants == null) {
            this.restaurants = this.getResponseFromRestServer("restaurants", Restaurant[].class);
        }
        return this.restaurants;
    }

    /**
     * Method to get an array of NoFlyZone objects representing the no-fly zones
     * fetched from the REST server.
     * @return An array of NoFlyZone objects representing the no-fly zones fetched
     *         from the REST server.
     * @throws IOException If the REST server is not running or the base url is invalid.
     */
    public NoFlyZone[] getNoFlyZones() throws IOException {
        if (this.noFlyZones == null) {
            this.noFlyZones = this.getResponseFromRestServer("noflyzones", NoFlyZone[].class);
        }
        return this.noFlyZones;
    }

}
