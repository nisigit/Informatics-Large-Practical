package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Restaurant {

    @JsonProperty("name")
    private String name;

    @JsonProperty("longitude")
    private double lng;

    @JsonProperty("latitude")
    private double lat;

    @JsonProperty("menu")
    private Menu[] menu;

    public Restaurant() {

    }

    public Menu[] getMenu() {
    }

}
