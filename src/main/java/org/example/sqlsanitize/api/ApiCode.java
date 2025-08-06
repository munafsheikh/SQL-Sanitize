package org.example.sqlsanitize.api;

import lombok.Getter;

/**
 * The set of response codes for this small service.
 * Only the codes we actually need:
 */
@Getter
public enum ApiCode {
    /** This is a generic "All is fine" */
    OK(200, "Action successful"),
    /** This is when create completed successfully. */
    CREATED(201, "Resource created"),
    /** This is used when no content exists. */
    NO_CONTENT(204, "No sensitive words found"),
    /** This is a Catch-all for any server-side errors. */
    ERROR(500, "Internal server error");

    /**
     * -- GETTER --
     * Numeric HTTP-style code.
     */
    private final int    id;
    /**
     * -- GETTER --
     * The default message.
     */
    private final String description;

    ApiCode(int id, String description) {
        this.id = id;
        this.description = description;
    }

}
