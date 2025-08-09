package org.example.sqlsanitize.api;

import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Generic wrapper for API responses in this service.
 * <p>
 * Standardizes the structure so all endpoints return:
 * <ul>
 *   <li>a timestamp (when the response was generated)</li>
 *   <li>a status code and message (from {@link ApiCode})</li>
 *   <li>optional payload data</li>
 * </ul>
 *
 * @param <T> the type of the payload (use {@code Void} if none)
 */
@Getter
public class ApiResult<T> {

    /** When this response was created. */
    private final ZonedDateTime timestamp = ZonedDateTime.now();

    /** Numeric status code (mirrors {@link ApiCode#getId()}). */
    private final int code;

    /** Short, human-readable description of the result. */
    private final String message;

    /** Optional payload data; can be {@code null} if not applicable. */
    private final T data;

    /**
     * Create a new API result.
     *
     * @param apiCode the status code/message pair to use
     * @param data    the payload (may be {@code null})
     */
    public ApiResult(ApiCode apiCode, T data) {
        this.code    = apiCode.getId();
        this.message = apiCode.getDescription();
        this.data    = data;
    }
}
