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

    //
    private static ResponseFetcher responseFetcher = null;

    private static final String BASE_URL = "https://ilp-rest.azurewebsites.net/";

    private ResponseFetcher() {

    }

    public static ResponseFetcher getInstance() {
        if (responseFetcher == null) {
            responseFetcher = new ResponseFetcher();
        }

        return responseFetcher;
    }

    public ArrayList<LngLat> getCentralArea() throws IOException {
        URL apiUrl = new URL(BASE_URL + "/centralarea");
        LngLat[] centralAreaVertices = new ObjectMapper().readValue(
                apiUrl, LngLat[].class);

        return new ArrayList<>(List.of(centralAreaVertices));
    }

    public Order[] getOrders() throws IOException {
        URL apiUrl = new URL(BASE_URL + "/orders");

        return new ObjectMapper().readValue(
                apiUrl, Order[].class);
    }

}
