package uk.ac.ed.inf;

/**
 * Checked exception thrown when an order has an invalid pizza or pizzas ordered are from different restaurants.
 */
public class InvalidPizzaCombinationException extends Exception {

    public InvalidPizzaCombinationException(String errorMessage) {
        super(errorMessage);
    }

}
