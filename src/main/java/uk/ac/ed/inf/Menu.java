package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Menu {

    @JsonProperty("name")
    private String name;

    @JsonProperty("priceInPence")
    private int priceInPence;

    public Menu() {

    }

}
