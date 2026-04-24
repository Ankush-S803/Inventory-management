package com.inventory.exception;

/**
 * Thrown when a stock-out transaction exceeds available stock.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
