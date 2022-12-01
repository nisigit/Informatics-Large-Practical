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

    // Restaurant instance of the restaurant that the order is from.
    private Restaurant orderRestaurant;


    // Field to store the outcome of the order.
    private OrderOutcome orderOutcome;

    // Integer to store the estimated number of moves required by the drone to deliver the order.
    private int movesToDeliver;

    /**
     * Class constructor.
     */
    public Order() {

    }

    /**
     * Method to get an OrderOutcome instance that represents the outcome of the order.
     * @return OrderOutcome instance that represents the outcome of the order.
     */
    public OrderOutcome getOrderOutcome() {
        return this.orderOutcome;
    }

    /**
     * Method to set the OrderOutcome instance that represents the outcome of the order.
     * @param orderOutcome OrderOutcome instance that represents the outcome of the order.
     */
    public void setOrderOutcome(OrderOutcome orderOutcome) {
        this.orderOutcome = orderOutcome;
    }

    /**
     * Method to get the number of moves the drone has to make to get to the order's restaurant.
     * @return number of moves the drone has to make to get to the order's restaurant.
     */
    public int getMovesToDeliver() {
        return this.movesToDeliver;
    }

    /**
     * Method to set the number of moves the drone has to make to get to the order's restaurant.
     * @param movesToDeliver number of moves the drone has to make to get to the order's restaurant.
     */
    public void setMovesToDeliver(int movesToDeliver) {
        this.movesToDeliver = movesToDeliver;
    }

    /**
     * Method to get the delivery cost (in pence) of the order.
     * @return delivery cost (in pence) of the order.
     */
    private int getDeliveryCost() {
        if (this.orderRestaurant == null) {
            if (this.orderOutcome == null) {
                throw new IllegalArgumentException("Check order validity before getting delivery cost");
            } else {
                throw new IllegalArgumentException("Cannot calculate delivery cost for invalid order.");
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
     * Method to check if the items in the order are valid. If the items are valid. The
     * method sets the restaurant of the order to the restaurant that the items are from.
     * @param participants An array of Restaurant instances representing the
     *                     restaurants that are participating in the service.
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
     * order is valid, the method sets the order outcome to OrderOutcome.ValidButNotDelivered,
     * and the order restaurant to the restaurant that the order is from.
     * @return True if the order is valid for the given world state, false otherwise.
     */
    public boolean isOrderValid() throws IOException {
        // The number of pizzas ordered must be greater than 0 and up to 5.
        if (this.orderItems.length < 1 || this.orderItems.length > 5) {
            this.orderOutcome = OrderOutcome.InvalidPizzaCount;
            return false;
        }

        if (!(this.isCardCvvValid() && this.isCardExpiryValid() && this.isCardNumberValid())) {
            return false;
        }

        Restaurant[] participants = DataFetcher.getInstance().getRestaurants();
        if (!this.areItemsValid(participants)) {
            return false;
        }

        if (this.getDeliveryCost() != this.priceTotalInPence) {
            this.orderOutcome = OrderOutcome.InvalidTotal;
            return false;
        }
        this.orderOutcome = OrderOutcome.ValidButNotDelivered;
        return true;
    }

    /**
     * Method to check if the expiry date of the credit card is of a valid format and
     * is not before the order date. If expiry date is passed, the method sets the order
     * outcome to OrderOutcome.InvalidCardExpired.
     * @return True if the expiry date is valid, false otherwise.
     */
    private boolean isCardExpiryValid() {
        // Credit card expiry date must be in the format MM/YY and only contain digits.
        if (this.creditCardExpiry.length() != 5 ||
                !this.creditCardExpiry.matches("([0-9]{2})/([0-9]{2})")) {
            this.orderOutcome = OrderOutcome.InvalidExpiryDate;
            return false;
        }

        // Checking if the month number in the expiry date is valid.
        String[] expiryMonthYear = this.creditCardExpiry.split("/");
        int month = Integer.parseInt(expiryMonthYear[0]);
        if (month < 1 || month > 12) {
            this.orderOutcome = OrderOutcome.InvalidExpiryDate;
            return false;
        }

        YearMonth expiryYearMonth = YearMonth.parse(this.creditCardExpiry, DateTimeFormatter.ofPattern("MM/yy"));
        LocalDate expiryDate = expiryYearMonth.atEndOfMonth(); // Card expiry is the last day of the month.
        LocalDate orderDate = LocalDate.parse(this.orderDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Card is invalid if order date is after expiry date.
        if (orderDate.isAfter(expiryDate)) {
            this.orderOutcome = OrderOutcome.InvalidExpiryDate;
            return false;
        }
        return true;
    }

    /**
     * Method to check if credit card number of an order is valid by checking its length and
     * whether it contains only digits. Also checks if the card number satisfies Luhn's algorithm.
     * If invalid, the order outcome is set to OrderOutcome.InvalidCardNumber.
     * @return True if the credit card number is valid, false otherwise.
     */
    private boolean isCardNumberValid() {
        // Credit card number should be 16 characters long and only contain digits.
        if (this.creditCardNumber.length() != 16 ||
                !this.creditCardNumber.matches("[0-9]+")) {
            this.orderOutcome = OrderOutcome.InvalidCardNumber;
            return false;
        }

        // Using Luhn's algorithm to check if the credit card number is valid.
        int digitSum = 0;
        for (int i = 0; i < this.creditCardNumber.length(); i++) {
            int digit = Integer.parseInt(Character.toString(this.creditCardNumber.charAt(i)));
            if (i % 2 == 0) { // Odd digits.
                digit *= 2;
                digitSum += (digit / 10) + (digit % 10); // Adding the digits of the doubled number.
            } else {
                digitSum += digit;
            }
        }
        if (digitSum % 10 != 0) { // Credit card number is invalid if the sum is not divisible by 10.
            this.orderOutcome = OrderOutcome.InvalidCardNumber;
            return false;
        }
        return true;
    }

    /**
     * Method to check if Card CVV in the order is valid. If the CVV is invalid, the
     * order outcome is set to OrderOutcome.InvalidCvv.
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

    /**
     * Method to get the Restaurant instance representing the restaurant that the order is from.
     * @return Restaurant instance representing the restaurant that the order is from.
     * @throws IllegalStateException if order validity has not been checked or the order is invalid.
     */
    public Restaurant getRestaurant() {
        if (this.orderOutcome == null) {
            throw new IllegalStateException("Check for order validity before getting restaurant.");
        } else if (!(this.orderOutcome == OrderOutcome.ValidButNotDelivered ||
                this.orderOutcome == OrderOutcome.Delivered)) {
            throw new IllegalStateException("Cannot return restaurant for invalid order.");
        }
        return this.orderRestaurant;
    }

    /**
     * Method to get the order number of the order.
     * @return String representing the order number of the order.
     */
    public String getOrderNo() {
        return this.orderNo;
    }

    /**
     * Method to get the price total in pence of the order, received from the REST server.
     * @return Integer representing the price total in pence of the order (from the REST server).
     */
    public int getPriceTotalInPence() {
        return this.priceTotalInPence;
    }
}
