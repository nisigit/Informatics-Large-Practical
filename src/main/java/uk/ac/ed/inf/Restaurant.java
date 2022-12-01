package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private MenuItem[] menuItems;

    // HashMap to store the menu items and their prices in the menu of the restaurant.
    private HashMap<String, Integer> menuItemPrices;

    // LngLat instance to represent the location of the restaurant.
    private LngLat restLngLat;

    /**
     * Class constructor to initialise a new Restaurant object.
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
            for (MenuItem menuItem : menuItems) {
                menuItemPrices.put(menuItem.name(), menuItem.priceInPence());
            }
        }
        return menuItemPrices;
    }

    /**
     * Method to get the LngLat instance representing the location of the restaurant.
     * @return LngLat instance representing the location of the restaurant.
     */
    public LngLat getLngLat() {
        if (this.restLngLat == null) {
            this.restLngLat = new LngLat(this.lng, this.lat);
        }
        return this.restLngLat;
    }

}
