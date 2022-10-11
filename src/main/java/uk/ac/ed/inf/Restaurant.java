package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;

public class Restaurant {

    @JsonProperty("name")
    public String name;

    @JsonProperty("longitude")
    public double lng;

    @JsonProperty("latitude")
    public double lat;

    @JsonProperty("menu")
    public Menu[] menuItems;

    public Restaurant() {

    }

    public Menu[] getMenu() {
        return menuItems;
    }

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
