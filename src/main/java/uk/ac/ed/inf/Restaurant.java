package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;

/**
 * Class to represent a participating restaurant in the service.
 */
public class Restaurant {

    // Field to store the name of the restaurant.
    @JsonProperty("name")
    public String name;

    // Field to store the longitude of the restaurant.
    @JsonProperty("longitude")
    public double lng;

    // Field to store the latitude of the restaurant.
    @JsonProperty("latitude")
    public double lat;

    // Array to store the list of menu items in the menu of the restaurant.
    @JsonProperty("menu")
    public Menu[] menuItems;

    /**
     * Class constructor.
     */
    public Restaurant() {

    }

    /**
     * Method to get the menu of the restaurant.
     * @return An array of Menu instances representing items in the restaurant's menu.
     */
    public Menu[] getMenu() {
        return menuItems;
    }

    /**
     * Method to fetch the list of participating restaurants in the service.
     * @param serverBaseAddress Base URL of the rest server.
     * @return Array of Restaurant objects representing a participating restaurant in the service.
     * @throws IOException
     */
    public static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) throws IOException {
        String endPoint = "";

        if (!serverBaseAddress.toString().endsWith("/")) {
            endPoint = "/" + endPoint;
        }
        endPoint += "restaurants";

        URL serverAddress = new URL(serverBaseAddress + endPoint);
        return new ObjectMapper().readValue(
                serverAddress, Restaurant[].class);
    }

}
