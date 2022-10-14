package uk.ac.ed.inf;

/**
 * Checked exception thrown when an order has an invalid pizza or pizzas ordered are from
 * different restaurants.
 */
public class InvalidPizzaCombinationException extends Exception {

    /**
     * Constructs a new Exception with the specified detail message.
     * @param errorMessage the detail message which indicates why the error was thrown.
     */
    public InvalidPizzaCombinationException(String errorMessage) {
        super(errorMessage);
    }

}
