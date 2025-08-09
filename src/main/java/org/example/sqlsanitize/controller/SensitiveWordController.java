package org.example.sqlsanitize.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sqlsanitize.api.ApiCode;
import org.example.sqlsanitize.api.ApiResult;
import org.example.sqlsanitize.dto.SanitizeRequestDTO;
import org.example.sqlsanitize.dto.SqlSanitizeWordDTO;
import org.example.sqlsanitize.model.SensitiveWord;
import org.example.sqlsanitize.service.SensitiveWordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints to manage the sensitive word/phrase list and to sanitize text.
 * <p>
 * Conventions:
 * <ul>
 *   <li><b>Create/Update</b>: JSON body using {@link SqlSanitizeWordDTO}.</li>
 *   <li><b>Sanitize</b>: simple query parameter (<code>?input=...</code>).</li>
 * </ul>
 */
@RestController
@RequestMapping(value = "/api/sensitive-words", produces = "application/json")
@RequiredArgsConstructor
@Tag(name = "Sensitive Words", description = "Manage and sanitize sensitive words/phrases")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    /**
     * Get the full list of configured sensitive words/phrases, sorted A→Z (case-insensitive).
     *
     * @return {@link ApiResult} with {@link ApiCode#OK} and the list; if empty, {@link ApiCode#NO_CONTENT}.
     */
    @GetMapping
    @Operation(summary = "Get all sensitive words", description = "Returns all words/phrases marked as sensitive.")
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "List retrieved"),
                    @ApiResponse(responseCode = "204", description = "No sensitive words found")
            }
    )
    public ApiResult<List<SensitiveWord>> getAllSensitiveWords() {
        List<SensitiveWord> list = sensitiveWordService.getAllSensitiveWords();
        if (list.isEmpty()) {
            return new ApiResult<>(ApiCode.NO_CONTENT, list);
        }
        return new ApiResult<>(ApiCode.OK, list);
    }

    /**
     * Add a new sensitive word or phrase.
     * <p>Body example: <pre>{ "word": "SELECT" }</pre></p>
     *
     * @param sqlSanitizeWordDTO DTO containing the word/phrase to add
     * @return {@link ApiResult} with {@link ApiCode#CREATED} and the saved entity
     */
    @PostMapping(consumes = "application/json")
    @Operation(
            summary = "Add a sensitive word/phrase",
            description = "Send JSON: { \"word\": \"SELECT\" }"
    )
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "201", description = "Created"),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "409", description = "Duplicate word")
            }
    )
    public ApiResult<SensitiveWord> add(@Valid @RequestBody SqlSanitizeWordDTO sqlSanitizeWordDTO) {
        return new ApiResult<>(ApiCode.CREATED, sensitiveWordService.add(sqlSanitizeWordDTO.getWord()));
    }

    /**
     * Update an existing word/phrase by its ID.
     * <p>Body example: <pre>{ "word": "order by" }</pre></p>
     *
     * @param id                 database ID of the record to update
     * @param sqlSanitizeWordDTO DTO with the new word/phrase value
     * @return {@link ApiResult} with {@link ApiCode#OK} and the updated entity
     */
    @PutMapping(path = "/{id}", consumes = "application/json")
    @Operation(
            summary = "Update a sensitive word by ID",
            description = "Send JSON: { \"word\": \"order by\" }"
    )
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Updated"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or duplicate"),
                    @ApiResponse(responseCode = "404", description = "Word not found")
            }
    )
    public ApiResult<SensitiveWord> update(@PathVariable Long id,
                                           @Valid @RequestBody SqlSanitizeWordDTO sqlSanitizeWordDTO) {
        return new ApiResult<>(ApiCode.OK, sensitiveWordService.update(id, sqlSanitizeWordDTO.getWord()));
    }

    /**
     * Delete a word/phrase by ID.
     * <p>Idempotent: returns OK even if the ID didn’t exist.</p>
     *
     * @param id database ID to delete
     * @return {@link ApiResult} with {@link ApiCode#OK} and no payload
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a sensitive word by ID",
            description = "Returns OK even if the ID did not exist."
    )
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Deleted (idempotent)"),
                    @ApiResponse(responseCode = "400", description = "Invalid ID")
            }
    )
    public ApiResult<Void> deleteById(@PathVariable Long id) {
        sensitiveWordService.deleteById(id);
        return new ApiResult<>(ApiCode.OK, null);
    }

    /**
     * Sanitize text by masking any configured sensitive words/phrases with asterisks.
     * <p>
     * Example:
     * <pre>POST /api/sensitive-words/sanitize?input=Select%20*%20from%20users%20order%20by%20name</pre>
     * </p>
     *
     * @param sanitizeRequestDTO DTO containing the input to sanitize
     * @return {@link ApiResult} with the sanitized string
     */
    @PostMapping(path = "/sanitize", consumes = "application/json")
    @Operation(
            summary = "Sanitize a string",
            description = "Send JSON with a single 'input' field. Replaces stored words/phrases with asterisks."
    )
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Sanitized")
            }
    )
    public ApiResult<String> sanitize(@Valid @RequestBody SanitizeRequestDTO sanitizeRequestDTO) {
        return new ApiResult<>(ApiCode.OK, sensitiveWordService.sanitize(sanitizeRequestDTO.getInput()));
    }
}
