package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
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
    public String orderNo;

    // String to store the date of the order.
    @JsonProperty("orderDate")
    public String orderDate;

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

    private OrderOutcome orderOutcome;

    /**
     * Class constructor.
     */
    public Order() {

    }

    public int getDeliveryCost(Restaurant[] participants) {
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
     * Method to check if the items in the order are valid. If the items are valid, the method
     * sets the restaurant of the order to the restaurant that the items are from.
     * @param participants An array of Restaurant instances representing the restaurants that are
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

        // If at least one order item is not in any restaurant's menu.
        if (allPizzas.containsAll(Arrays.asList(this.orderItems))) {
            this.orderOutcome = OrderOutcome.InvalidPizzaCountMultipleSuppliers;
        } else {
            this.orderOutcome = OrderOutcome.InvalidPizzaNotDefined;
        }
        return false;
    }

    public boolean isOrderValid() throws IOException {
        // The number of pizzas ordered must be greater than 0 and up to 5.
        if (this.orderItems.length < 1 || this.orderItems.length > 5) {
            this.orderOutcome = OrderOutcome.InvalidPizzaCount;
            return false;
        }

        if (!(this.isCardCvvValid() && this.isCardExpiryValid() && this.isCardNumberValid())) {
            return false;
        }

        Restaurant[] participants = ResponseFetcher.getInstance().getRestaurants();

        if (!this.areItemsValid(participants)) {
            return false;
        }

        if (this.getDeliveryCost(participants) != this.priceTotalInPence) {
            this.orderOutcome = OrderOutcome.InvalidTotal;
            return false;
        }

        return true;
    }

    /**
     * Method to check if the expiry date of the credit card is of a
     * valid format and is not before the order date.
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
     * @return true if the CVV is valid, false otherwise.
     */
    private boolean isCardCvvValid() {
        // Credit card CVV should be 3 digits long and only contain digits.
        if (!this.cvv.matches("^[0-9]{3,4}$")) {
            this.orderOutcome = OrderOutcome.InvalidCvv;
            return false;
        }
        return true;
    }

}
