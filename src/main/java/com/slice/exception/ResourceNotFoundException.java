package com.slice.exception;

/**
 * Thrown when a requested resource does not exist.
 * Maps to HTTP 404 NOT FOUND via GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resourceName, field, value));
    }
}
