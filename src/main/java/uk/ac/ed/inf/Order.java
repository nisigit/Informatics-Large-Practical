package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
    public int priceTotalInPence;

    // Array of strings to store the names of the items in the order.
    @JsonProperty("orderItems")
    private String[] orderItems;

    // Restaurant instance of the restaurant that the order is from.
    private Restaurant orderRestaurant;

    // Integer to store the number of moves the drone has to make to get to the
    // order's restaurant.
    private int movesToRestaurant;

    private OrderOutcome orderOutcome;

    /**
     * Class constructor.
     */
    public Order() {

    }

    public OrderOutcome getOrderOutcome() {
        return orderOutcome;
    }

    public void setOrderOutcome(OrderOutcome orderOutcome) {
        this.orderOutcome = orderOutcome;
    }

    public int getMovesToRestaurant() {
        return movesToRestaurant;
    }

    public void setMovesToRestaurant(int movesToRestaurant) {
        this.movesToRestaurant = movesToRestaurant;
    }

    public int getDeliveryCost() {
        if (this.orderRestaurant == null) {
            if (this.orderOutcome == null) {
                throw new IllegalArgumentException("Check if order is valid before getting delivery cost");
            } else {
                throw new IllegalArgumentException("Order is invalid");
            }
        }
        int deliveryCost = 100; // Delivery cost is £1 (100 pence).
        HashMap<String, Integer> restaurantMenu = this.orderRestaurant.getMenuItemPrices();
        for (String item : this.orderItems) {
            deliveryCost += restaurantMenu.get(item);
        }

        return deliveryCost;
    }

    /**
     * Method to check if the items in the order are valid. If the items are valid,
     * the method
     * sets the restaurant of the order to the restaurant that the items are from.
     * 
     * @param participants An array of Restaurant instances representing the
     *                     restaurants that are
     *                     participating in the service.
     * @return True if the items in the order are valid, false otherwise.
     */
    private boolean areItemsValid(Restaurant[] participants) {
        HashSet<String> allPizzas = new HashSet<>();
        for (Restaurant restaurant : participants) {
            HashMap<String, Integer> restaurantMenu = restaurant.getMenuItemPrices();
            if (restaurantMenu.keySet().containsAll(Arrays.asList(this.orderItems))) {
                this.orderRestaurant = restaurant;
                return true;
            }
            allPizzas.addAll(restaurantMenu.keySet());
        }
        // If all the order items were found in menus of different restaurants.
        if (allPizzas.containsAll(Arrays.asList(this.orderItems))) {
            this.orderOutcome = OrderOutcome.InvalidPizzaCountMultipleSuppliers;
        } else { // At least one item in the order was not found in any of the menus.
            this.orderOutcome = OrderOutcome.InvalidPizzaNotDefined;
        }
        return false;
    }

    /**
     * Method to check if a given order is valid for a given world state. If the
     * order is valid, the method sets the
     * order outcome to OrderOutcome.ValidButNotDelivered, and the order restaurant
     * to the restaurant that the order is from.
     * 
     * @param worldState The world state for a particular dat to check the order
     *                   against.
     * @return True if the order is valid for the given world state, false
     *         otherwise.
     */
    public boolean isOrderValid(WorldState worldState) {
        // The number of pizzas ordered must be greater than 0 and up to 5.
        if (this.orderItems.length < 1 || this.orderItems.length > 5) {
            this.orderOutcome = OrderOutcome.InvalidPizzaCount;
            return false;
        }

        if (!(this.isCardCvvValid() && this.isCardExpiryValid() && this.isCardNumberValid())) {
            return false;
        }

        Restaurant[] participants = worldState.getRestaurants();
        if (!this.areItemsValid(participants)) {
            return false;
        }

        if (this.getDeliveryCost() != this.priceTotalInPence) {
            this.orderOutcome = OrderOutcome.InvalidTotal;
            return false;
        }
        this.orderOutcome = OrderOutcome.ValidButNotDelivered;
        PathFinder pathFinder = new PathFinder(worldState);
        this.movesToRestaurant = (pathFinder.findPath(WorldState.APPLETON_TOWER_COORDINATES,
                this.orderRestaurant.getLngLat())).size();
        return true;
    }

    /**
     * Method to check if the expiry date of the credit card is of a
     * valid format and is not before the order date.
     * 
     * @return True if the expiry date is valid, false otherwise.
     */
    private boolean isCardExpiryValid() {
        // Credit card expiry date must be in the format MM/YY and only contain digits.
        if (this.creditCardExpiry.length() != 5 ||
                !this.creditCardExpiry.matches("([0-9]{2})/([0-9]{2})")) {
            this.orderOutcome = OrderOutcome.InvalidExpiryDate;
            return false;
        }

        String[] expiryMonthYear = this.creditCardExpiry.split("/");
        int month = Integer.parseInt(expiryMonthYear[0]);
        if (month < 1 || month > 12) {
            this.orderOutcome = OrderOutcome.InvalidExpiryDate;
            return false;
        }

        YearMonth expiryYearMonth = YearMonth.parse(this.creditCardExpiry, DateTimeFormatter.ofPattern("MM/yy"));
        LocalDate expiryDate = expiryYearMonth.atEndOfMonth();
        LocalDate orderDate = LocalDate.parse(this.orderDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Card is expired if the expiry date is before the order date.
        if (orderDate.isAfter(expiryDate)) {
            this.orderOutcome = OrderOutcome.InvalidExpiryDate;
            return false;
        }
        return true;
    }

    // TODO: Add Luhn's algorithm
    /**
     * Method to check if credit card number of an order is valid.
     * 
     * @return True if the credit card number is valid, false otherwise.
     */
    private boolean isCardNumberValid() {
        // Credit card number should be 13 to 16 digits long and only contain digits.
        if (this.creditCardNumber.length() != 16 ||
                !this.creditCardNumber.matches("[0-9]+")) {
            this.orderOutcome = OrderOutcome.InvalidCardNumber;
            return false;
        }
        return true;
    }

    /**
     * Method to check if Card CVV in the order is valid.
     * 
     * @return true if the CVV is valid, false otherwise.
     */
    private boolean isCardCvvValid() {
        // Credit card CVV should be 3 digits long and only contain digits.
        if (!this.cvv.matches("^[0-9]{3}$")) {
            this.orderOutcome = OrderOutcome.InvalidCvv;
            return false;
        }
        return true;
    }

    public Restaurant getRestaurant() {
        return this.orderRestaurant;
    }

    public String getOrderNo() {
        return orderNo;
    }

}
