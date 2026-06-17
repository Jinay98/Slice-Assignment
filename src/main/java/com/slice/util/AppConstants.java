package com.slice.util;

/**
 * Application-wide constants.
 * Utility class — non-instantiable by design (private constructor).
 */
public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // API versioning
    public static final String API_V1 = "/api/v1";

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "createdAt";

    // Date / Time
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    // Response status strings
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_ERROR = "ERROR";

    // Generic messages
    public static final String MSG_CREATED = "Resource created successfully";
    public static final String MSG_UPDATED = "Resource updated successfully";
    public static final String MSG_DELETED = "Resource deleted successfully";
    public static final String MSG_FETCHED = "Resource fetched successfully";
    public static final String MSG_VALIDATION_FAILED = "Validation failed";
    public static final String MSG_INTERNAL_ERROR = "An unexpected error occurred";
}
