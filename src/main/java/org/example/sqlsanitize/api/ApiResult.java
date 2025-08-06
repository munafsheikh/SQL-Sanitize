package org.example.sqlsanitize.api;

import java.time.ZonedDateTime;

/**
 * A simple, generic wrapper for all API responses.
 *
 * @param <T> the type of the payload (or {@code Void} if none)
 */
public class ApiResult<T> {

    /** When the response was created. */
    private final ZonedDateTime timestamp = ZonedDateTime.now();

    /** The numeric code of this result (see {@link ApiCode}). */
    private final int code;

    /** A human-readable message for this code. */
    private final String message;

    /** Optional payload data. */
    private final T data;

    /**
     * Create a result with no payload.
     *
     * @param apiCode the code to return
     */
    public ApiResult(ApiCode apiCode) {
        this(apiCode, null);
    }

    /**
     * Create a result with payload.
     *
     * @param apiCode the code to return
     * @param data    the payload (may be null)
     */
    public ApiResult(ApiCode apiCode, T data) {
        this.code    = apiCode.getId();
        this.message = apiCode.getDescription();
        this.data    = data;
    }

    public ZonedDateTime getTimestamp() { return timestamp; }
    public int            getCode()      { return code;      }
    public String         getMessage()   { return message;   }
    public T              getData()      { return data;      }
}
