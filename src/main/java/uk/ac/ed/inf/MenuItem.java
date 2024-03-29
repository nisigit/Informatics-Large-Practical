package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Record to represent an item in a restaurant's menu.
 * @param name         the name of the menu item.
 * @param priceInPence the price in pence of the menu item.
 */
public record MenuItem(@JsonProperty("name") String name, @JsonProperty("priceInPence") int priceInPence) {

}
