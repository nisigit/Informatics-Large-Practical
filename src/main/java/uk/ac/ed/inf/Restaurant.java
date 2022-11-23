package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

/**
 * Class to represent a participating restaurant in the service.
 */
public class Restaurant {

    // Field to store the name of the restaurant.
    @JsonProperty("name")
    public String name;

    // Field to store the longitude of the restaurant.
    @JsonProperty("longitude")
    private double lng;

    // Field to store the latitude of the restaurant.
    @JsonProperty("latitude")
    private double lat;

    // Array to store the list of menu items in the menu of the restaurant.
    @JsonProperty("menu")
    private Menu[] menuItems;

    // HashMap to store the menu items and their prices in the menu of the restaurant.
    private HashMap<String, Integer> menuItemPrices;

    private LngLat restLngLat;

    /**
     * Class constructor.
     */
    public Restaurant() {

    }

    /**
     * Method to get HashMap of menu items and their prices in the menu of the
     * @return HashMap of menu items and their prices in the menu of the
     */
    public HashMap<String, Integer> getMenuItemPrices() {
        if (menuItemPrices == null) {
            menuItemPrices = new HashMap<>();
            for (Menu menuItem : menuItems) {
                menuItemPrices.put(menuItem.name(), menuItem.priceInPence());
            }
        }
        return menuItemPrices;
    }


    /**
     * Method to get the array of items in the menu of a restaurant.
     * @return An array of Menu instances representing items in the restaurant's menu
     */
    public Menu[] getMenu() {
        if (menuItemPrices == null) {
            menuItemPrices = new HashMap<>();
            for (Menu menuItem : this.menuItems) {
                menuItemPrices.put(menuItem.name(), menuItem.priceInPence());
            }
        }
        return menuItems;
    }

    public LngLat getLngLat() {
        if (this.restLngLat == null) {
            this.restLngLat = new LngLat(this.lng, this.lat);
        }
        return this.restLngLat;
    }


    /**
     * Method to fetch the list of participating restaurants in the service.
     * @param serverBaseAddress Base URL of the rest server to fetch all the
     *                          restaurants' data.
     * @return Array of Restaurant objects representing a participating restaurant
     *         in the service.
     * @throws IOException If the REST server is not available or base url is
     *                     invalid.
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
