package org.example.sqlsanitize.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sqlsanitize.api.ApiCode;
import org.example.sqlsanitize.api.ApiResult;
import org.example.sqlsanitize.model.SensitiveWord;
import org.example.sqlsanitize.service.SensitiveWordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST controller for managing sensitive words.
 * <p>
 * Provides endpoints to perform CRUD operations on sensitive words,
 * as well as an endpoint to sanitize input strings by masking sensitive words.
 * </p>
 */
@RestController
@RequestMapping("/api/sensitive-words")
@RequiredArgsConstructor
@Tag(name = "Sensitive Words", description = "Endpoints for managing and sanitizing sensitive words")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    @GetMapping
    @Operation(summary = "Get all sensitive words",
            description = "Returns a list of all words marked as sensitive.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No sensitive words found")
    })
    public ApiResult<List<SensitiveWord>> getAllSensitiveWords() {
        List<SensitiveWord> list = sensitiveWordService.getAllSensitiveWords();

        //If no data is found return no content
        if (list.isEmpty()) {
            return new ApiResult<>(ApiCode.NO_CONTENT, list);
        }

        return new ApiResult<>(ApiCode.OK, list);
    }

    /**
     * Adds a new sensitive word to the system.
     *
     * @param word the SensitiveWord object to add; its id is ignored.
     * @return ApiResult with code CREATED and the saved SensitiveWord.
     */
    @PostMapping
    @Operation(summary = "Add a sensitive word",
            description = "Adds a new word to the sensitive word list.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Word created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate word")
    })
    public ApiResult<SensitiveWord> add(@Valid @RequestBody SensitiveWord word) {
        SensitiveWord saved = sensitiveWordService.add(word);
        return new ApiResult<>(ApiCode.CREATED, saved);
    }

    /**
     * Updates the sensitive word with the given ID, replacing it with the provided value.
     * <p>
     * The incoming {@code payload}’s {@code id} is ignored; only its {@code word} field is used.
     * Returns an ApiResult containing the updated entity.
     * </p>
     *
     * @param id      the database ID of the word to update; must not be null
     * @param payload a {@link SensitiveWord} containing the new word value; its {@code id} is ignored
     * @return an {@link ApiResult} with {@link ApiCode#OK} and the updated {@link SensitiveWord}
     * @throws IllegalArgumentException if {@code id} is null, or if {@code payload} or its word is blank
     * @throws NoSuchElementException   if no sensitive word exists with the given {@code id}
     * @throws IllegalStateException    if the new word would duplicate another existing entry
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a sensitive word by ID",
            description = "Replaces the stored word for the given ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Word updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate"),
            @ApiResponse(responseCode = "404", description = "Word not found")
    })
    public ApiResult<SensitiveWord> update(
            @PathVariable Long id,
            @Valid @RequestBody SensitiveWord payload
    ) {
        SensitiveWord updated = sensitiveWordService.update(id, payload);
        return new ApiResult<>(ApiCode.OK, updated);
    }

    /**
     * Deletes a sensitive word by its database ID.
     *
     * @param id the ID of the sensitive word to delete.
     * @return ApiResult with code OK and a confirmation message.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sensitive word by ID",
            description = "Deletes the word with the given ID and returns confirmation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Word deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied")
    })
    public ApiResult<String> deleteById(@PathVariable Long id) {
        sensitiveWordService.deleteById(id);
        return new ApiResult<>(ApiCode.OK, "Deleted");
    }

    /**
     * Sanitizes a given input string by replacing any sensitive words with asterisks.
     *
     * @param body JSON object with field "text" containing the original string.
     * @return ApiResult with code OK and a map containing the sanitized text.
     */
    @PostMapping("/sanitize")
    @Operation(summary = "Sanitize a string",
            description = "Replaces sensitive words in the input text with asterisks.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Text sanitized successfully")
    })
    public ApiResult<String> sanitize(@RequestBody Map<String, String> body) {
        String input = body.get("text");
        return new ApiResult<>(ApiCode.OK, sensitiveWordService.sanitize(input));
    }
}