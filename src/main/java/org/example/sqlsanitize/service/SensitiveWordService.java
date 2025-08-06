package org.example.sqlsanitize.service;

import org.example.sqlsanitize.model.SensitiveWord;
import org.example.sqlsanitize.repository.SensitiveWordRepository;
import lombok.RequiredArgsConstructor;
import org.example.sqlsanitize.util.WordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service layer responsible for all business logic around sensitive SQL words/phrases.
 * <p>
 * Supports:
 * <ul>
 *   <li>Retrieving the complete, sorted list of sensitive words or phrases.</li>
 *   <li>Adding new words/phrase.</li>
 *   <li>Updating a word/phrase</li>
 *   <li>Deleting existing words by ID.</li>
 *   <li>Sanitizing incoming text by masking stored sensitive words, that appear in the text.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class SensitiveWordService {

    // TODO: update javadocs, fix update (put) method, find solution for special chars in regex
    private final SensitiveWordRepository sensitiveWordRepository;

    /**
     * Fetches all sensitive words from the database, sorted alphabetically (case-insensitive).
     *
     * @return a non-null, modifiable {@link List} of {@link SensitiveWord} entities.
     */
    @Transactional(readOnly = true)
    public List<SensitiveWord> getAllSensitiveWords() {
        List<SensitiveWord> words = sensitiveWordRepository.findAll();
        // Sort the words alphabetically
        words.sort(Comparator.comparing(SensitiveWord::getWord, String.CASE_INSENSITIVE_ORDER));
        return words;
    }

    /**
     * Creates a new sensitive word entry.
     *
     * @param wordDto the incoming SensitiveWord DTO; its id is ignored
     * @return the saved entity with generated id
     * @throws IllegalArgumentException if the word is null or blank
     * @throws IllegalStateException    if the word already exists
     */
    @Transactional
    public SensitiveWord add(SensitiveWord wordDto) {
        String normalizedWord = WordUtils.validateAndNormalize(wordDto.getWord());

        // Check duplicates
        if (sensitiveWordRepository.existsByWordIgnoreCase(normalizedWord)) {
            throw new IllegalStateException("Word/Phrase already exists: " + normalizedWord);
        }

        SensitiveWord sWord = new SensitiveWord();
        sWord.setWord(normalizedWord);
        return sensitiveWordRepository.save(sWord);
    }

    /**
     * Updates an existing sensitive word by id.
     *
     * @param id      the id of the word to update
     * @param payload DTO carrying the new word (id ignored)
     * @return the updated entity
     * @throws IllegalArgumentException if id is null or payload word invalid
     * @throws NoSuchElementException   if no entity with {@code id} exists
     * @throws IllegalStateException    if the new word duplicates another entry
     */
    @Transactional
    public SensitiveWord update(Long id, SensitiveWord payload) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }

        // Load or fail
        SensitiveWord existing = sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No word found with ID " + id));

        // Ensure that the word is valid and to lowercase the word
        String normalized = WordUtils.validateAndNormalize(payload.getWord());

        // Ensure there are no duplicates, excluding itself now
        sensitiveWordRepository.findByWordIgnoreCase(normalized)
                .filter(sWord -> !sWord.getId().equals(id))
                .ifPresent(sWord -> {
                    throw new IllegalStateException("Duplicate word: " + normalized);
                });

        existing.setWord(normalized);
        return sensitiveWordRepository.save(existing);
    }

    /**
     * Deletes the sensitive word with the given ID.
     * <p>
     * No-op if the ID does not exist.
     * </p>
     *
     * @param id the ID of the sensitive word to delete; must not be null
     * @throws IllegalArgumentException if {@code id} is null.
     */
    @Transactional
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        sensitiveWordRepository.deleteById(id);
    }

    /**
     * Sanitizes the provided text by masking all stored sensitive words
     * with the same number of asterisks as the word’s length.
     * <p>
     * Matching is case-insensitive.  Pure “word” tokens (letters/digits/_) use word-boundaries.
     * Symbols or mixed tokens (e.g. "*", "@admin") use non-word lookarounds.
     * </p>
     *
     * @param input the text to sanitize; may be null or empty
     * @return a non-null sanitized string
     */
    @Transactional(readOnly = true)
    public String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String result = input;

        // Load all your preloaded SQL keywords/phrases
        List<SensitiveWord> words = sensitiveWordRepository.findAll();

        for (SensitiveWord w : words) {
            String raw = w.getWord();
            String regex = WordUtils.buildBoundaryRegex(raw);
            // mask length = number of characters in the keyword/phrase
            String mask = "*".repeat(raw.length());
            result = result.replaceAll(regex, mask);
        }

        return result;
    }
}