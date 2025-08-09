package org.example.sqlsanitize.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for the sanitize endpoint.
 * Just a single field: the raw text to clean.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload for sanitizing text")
public class SanitizeRequestDTO {

    @Schema(
            description = "Text to sanitize",
            example = "Select * from users order by name"
    )
    @NotBlank(message = "input must not be blank")
    private String input;
}
