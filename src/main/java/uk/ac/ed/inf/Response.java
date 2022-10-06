package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class Response {

    private static Response response = null;

    private static final String BASE_URL = "https://ilp-rest.azurewebsites.net/";

    private Response() {

    }

    public static Response getInstance() {
        if (response == null) {
            response = new Response();
        }

        return response;
    }

    public void getCentralArea() throws MalformedURLException {
        URL apiUrl = new URL(BASE_URL + "/centralarea");
    }

    public Restaurant[] getRestaurants() throws IOException {
        URL apiUrl = new URL(BASE_URL + "restaurants");

        Restaurant[] restaurants = new ObjectMapper().readValue(
                apiUrl, Restaurant[].class);

        return restaurants;
    }


}
