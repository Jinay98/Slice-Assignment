package com.slice.exception;

/**
 * Thrown when a business rule / invariant is violated.
 * Maps to HTTP 422 UNPROCESSABLE_ENTITY via GlobalExceptionHandler.
 *
 * Examples:
 *  - Withdrawing more than wallet balance
 *  - Booking an already-occupied slot
 *  - Sending to a deactivated user
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
