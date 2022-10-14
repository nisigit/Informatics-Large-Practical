package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Represents an order placed by a user on the service.
 */
public class Order {

    // String to store the order number of the order.
    @JsonProperty("orderNo")
    private String orderNo;

    // String to store the date of the order.
    @JsonProperty("orderDate")
    private String orderDate;

    // String to store the customer name of the order.
    @JsonProperty("customer")
    private String customer;

    // String to store the credit card number of the order.
    @JsonProperty("creditCardNumber")
    private String creditCardNumber;

    // String to store the credit card expiry date of the order.
    @JsonProperty("creditCardExpiry")
    private String creditCardExpiry;

    // String to store the credit card CVV of the order
    @JsonProperty("cvv")
    private String cvv;

    // String to store the total price in pence of the order.
    @JsonProperty("priceTotalInPence")
    private int priceTotalInPence;

    // Array of strings to store the names of the items in the order.
    @JsonProperty("orderItems")
    private String[] orderItems;

    /**
     * Class constructor.
     */
    public Order() {

    }

    /**
     * Method to get the total delivery cost of an order of pizzas.
     * @param participants Array of restaurants participating in the service, so that ordered pizzas can be
     *                     searched for in their menus.
     * @param orderedPizzas Array of the names of the pizzas ordered so that pizzas can be searched for
     *                      in the restaurant menus and total price can be calculated.
     * @return The total price of the order, including the delivery charge of 1 pound.
     * @throws InvalidPizzaCombinationException If a pizza in the order is not found or if pizzas ordered
     * are from different restaurants.
     */
    public int getDeliveryCost(Restaurant[] participants, ArrayList<String> orderedPizzas) throws InvalidPizzaCombinationException {
        // Hashmap to store items and their prices for the restaurant the order has been placed from.
        HashMap<String, Integer> orderRestaurantMenu = null;
        orderRestaurantMenu = this.getValidRestaurant(participants, new HashSet<>(orderedPizzas));

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
     * @param participants Array of the restaurants participating in the service so that ordered pizzas can be
     *                     searched for in their menus
     * @param orderedPizzasSet Set of the names of pizzas ordered so that the pizzas can be searched for in restaurant
     *                         menus and total price can be calculated.
     * @return HashMap of the menu of the restaurant all the pizzas have been ordered from, in [item name -> price in pence]
     *     key-value pairs. Returns null if any pizzas are not in any restaurant's menu or if pizzas are from different menus.
     */
    private HashMap<String, Integer> getValidRestaurant(Restaurant[] participants, Set<String> orderedPizzasSet) {
        for (Restaurant participant : participants) {
            Menu[] menuItems = participant.getMenu();
            HashMap<String, Integer> restaurantMenu = new HashMap<>();
            for (Menu menuItem : menuItems) {
                restaurantMenu.put(menuItem.name(), menuItem.priceInPence());
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
