package com.slice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.slice.util.AppConstants;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Standard API response envelope used for ALL endpoints.
 *
 * All responses — success and error — share this structure
 * so API clients have a single, predictable shape to handle.
 *
 * <pre>
 * Success:  { "status": "SUCCESS", "message": "...", "data": {...}, "timestamp": "..." }
 * Error:    { "status": "ERROR",   "message": "...", "data": null, "timestamp": "..." }
 * </pre>
 *
 * @param <T> type of the payload data
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String status;
    private final String message;
    private final T data;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    // ── Factory helpers ──────────────────────────────────────────────────

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(AppConstants.STATUS_SUCCESS)
                .message(AppConstants.MSG_FETCHED)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(AppConstants.STATUS_SUCCESS)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status(AppConstants.STATUS_ERROR)
                .message(message)
                .build();
    }
}
