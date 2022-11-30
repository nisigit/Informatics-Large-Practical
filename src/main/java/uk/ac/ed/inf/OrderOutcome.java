package uk.ac.ed.inf;

/**
 * Enum class to represent the outcome of an order.
 */
public enum OrderOutcome {
    Delivered,
    ValidButNotDelivered,
    InvalidCardNumber,
    InvalidExpiryDate,
    InvalidCvv,
    InvalidTotal,
    InvalidPizzaNotDefined,
    InvalidPizzaCount,
    InvalidPizzaCountMultipleSuppliers,
    Invalid
}
