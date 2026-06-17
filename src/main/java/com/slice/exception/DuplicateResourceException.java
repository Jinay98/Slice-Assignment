package com.slice.exception;

/**
 * Thrown when attempting to create a resource that already exists.
 * Maps to HTTP 409 CONFLICT via GlobalExceptionHandler.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String field, Object value) {
        super(String.format("%s already exists with %s: '%s'", resourceName, field, value));
    }
}
