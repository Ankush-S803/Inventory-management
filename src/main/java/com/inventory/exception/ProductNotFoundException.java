package com.inventory.exception;

/**
 * Thrown when a requested product does not exist in the database.
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
