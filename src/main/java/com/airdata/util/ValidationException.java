package com.airdata.util;

/**
 * Custom application exception to demonstrate Exception Handling in OOP.
 * Thrown when data validation fails during CSV parsing, entity creation, or user input.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
