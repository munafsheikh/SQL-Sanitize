package org.example.sqlsanitize.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a word or phrase that should be sanitized from incoming text.
 */
@Schema(description = "Entity representing a sensitive word to be masked in text")
@Entity
@Table(name = "sensitive_words")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWord {

    /** Auto-generated primary key. */
    @Schema(description = "Auto-generated ID of the sensitive word", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The word or phrase to be masked (case-insensitive). */
    @Schema(description = "Word to be masked (case-insensitive)", example = "SELECT")
    @NotBlank(message = "Word cannot be blank")
    @Column(nullable = false, unique = true)
    private String word;
}