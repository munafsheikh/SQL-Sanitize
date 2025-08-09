package org.example.sqlsanitize.api;

import lombok.Getter;

/**
 * List of response codes used by this service.
 * <p>
 * Each entry has:
 * <ul>
 *   <li>a numeric code</li>
 *   <li>a short, default message</li>
 * </ul>
 */
@Getter
public enum ApiCode {

    /** Generic "everything went fine" success. */
    OK(200, "Action successful"),

    /** Resource was successfully created. */
    CREATED(201, "Resource created"),

    /** No data found for the request. */
    NO_CONTENT(204, "No sensitive words found"),

    /** Unexpected server-side error. */
    ERROR(500, "Internal server error");

    /** Numeric code (HTTP-style). */
    private final int id;

    /** Default message text for the code. */
    private final String description;

    ApiCode(int id, String description) {
        this.id = id;
        this.description = description;
    }
}
