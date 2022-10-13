package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Represents an order placed by a user on the service.
 */
public class Order {

    @JsonProperty("orderNo")
    private String orderNo;

    @JsonProperty("orderDate")
    private String orderDate;

    @JsonProperty("customer")
    private String customer;

    @JsonProperty("creditCardNumber")
    private String creditCardNumber;

    @JsonProperty("creditCardExpiry")
    private String creditCardExpiry;

    @JsonProperty("cvv")
    private String cvv;

    @JsonProperty("priceTotalInPence")
    private int priceTotalInPence;

    @JsonProperty("orderItems")
    private String[] orderItems;

    public Order() {

    }

    /**
     * Method to get the total delivery cost of an order of pizzas.
     * @param participants Array of restaurants participating in the service.
     * @param orderedPizzas Array of name of the pizzas in the order.
     * @return The total price of the order, including the delivery charge of 1 pound.
     * @throws InvalidPizzaCombinationException If a pizza name is not found or if pizzas ordered are from different restaurants.
     */
    public int getDeliveryCost(Restaurant[] participants, String[] orderedPizzas) throws InvalidPizzaCombinationException {
        // Hashmap to store items and their prices for the restaurant the order has been placed from.
        HashMap<String, Integer> orderRestaurantMenu = null;
        Set<String> orderedPizzasSet = new HashSet<>(Arrays.asList(orderedPizzas));
        orderRestaurantMenu = this.getValidRestaurant(participants, orderedPizzasSet);

        // Either the ordered items are from menus of different restaurants or at least one item does not exist.
        if (orderRestaurantMenu == null) {
            throw new InvalidPizzaCombinationException("Order item not found and/or order contains items from multiple restaurants");
        }
        int orderPriceInPence = 100; // Delivery charge of 100 pence (£1).
        for (String orderedPizza : orderedPizzas) {
            orderPriceInPence += orderRestaurantMenu.get(orderedPizza);
        }

        return orderPriceInPence;
    }

    /**
     * Checks if a combination of ordered pizzas has been ordered from a single restaurant.
     * @param participants Array of the restaurants participating in the service.
     * @param orderedPizzasSet Set of the names of pizzas ordered.
     * @return HashMap of the menu of the restaurant all the pizzas have been ordered from, in [item name -> price in pence]
     *     format. Returns null if any pizzas are not in any restaurant's menu or if pizzas are from different menus.
     */
    private HashMap<String, Integer> getValidRestaurant(Restaurant[] participants, Set<String> orderedPizzasSet) {
        for (Restaurant participant : participants) {
            Menu[] menuItems = participant.getMenu();
            HashMap<String, Integer> restaurantMenu = new HashMap<>();
            for (Menu menuItem : menuItems) {
                restaurantMenu.put(menuItem.name, menuItem.priceInPence);
            }

            // If all the items in the order are contained in a restaurant's menu.
            if (restaurantMenu.keySet().containsAll(orderedPizzasSet)) {
                return restaurantMenu;
            }
        }
        // No single restaurant menu contains all the items in the order.
        return null;
    }

}
