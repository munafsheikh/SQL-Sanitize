package org.example.sqlsanitize.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

/** Request payload for creating or updating a sensitive word/phrase. */
@Value
@Schema(description = "Payload containing a sensitive word/phrase.")
public class SqlSanitizeWordDTO {

    @NotBlank
    @Schema(description = "Word or phrase to store as sensitive.", example = "SELECT")
    String word;
}
