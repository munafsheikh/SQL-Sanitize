package org.example.sqlsanitize.service;

import lombok.RequiredArgsConstructor;
import org.example.sqlsanitize.model.SensitiveWord;
import org.example.sqlsanitize.repository.SensitiveWordRepository;
import org.example.sqlsanitize.util.WordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Handles the main logic for working with sensitive words/phrases.
 *
 * <p>Includes:</p>
 * <ul>
 *   <li>Listing all stored words/phrases in alphabetical order</li>
 *   <li>Adding new ones with validation and duplicate checks</li>
 *   <li>Updating existing ones by ID</li>
 *   <li>Deleting by ID</li>
 *   <li>Masking them out of any given text</li>
 * </ul>
 *
 * <p>Words are normalized (trimmed and lowercased) before saving.
 * Matching in text is case-insensitive and uses safe regex boundaries so only whole matches are replaced.</p>
 */
@Service
@RequiredArgsConstructor
public class SensitiveWordService {

    private final SensitiveWordRepository sensitiveWordRepository;

    /**
     * Returns all stored sensitive words/phrases, sorted alphabetically (ignoring case).
     */
    @Transactional(readOnly = true)
    public List<SensitiveWord> getAllSensitiveWords() {
        List<SensitiveWord> words = sensitiveWordRepository.findAll();
        words.sort(Comparator.comparing(SensitiveWord::getWord, String.CASE_INSENSITIVE_ORDER));
        return words;
    }

    /**
     * Adds a new sensitive word or phrase.
     *
     * <p>Trims and lowercases the input, then checks for duplicates (case-insensitive).</p>
     *
     * @param word the word or phrase to add, e.g. "select", "order by"
     * @return the saved entity with its generated ID
     * @throws IllegalArgumentException if input is null or blank
     * @throws IllegalStateException if it already exists
     */
    @Transactional
    public SensitiveWord add(String word) {
        String normalizedWord = WordUtils.validateAndNormalize(word);

        if (sensitiveWordRepository.existsByWordIgnoreCase(normalizedWord)) {
            throw new IllegalStateException("Word/Phrase already exists: " + normalizedWord);
        }

        SensitiveWord sWord = new SensitiveWord();
        sWord.setWord(normalizedWord);
        return sensitiveWordRepository.save(sWord);
    }

    /**
     * Updates an existing sensitive word/phrase by ID.
     *
     * <p>Also trims, lowercases, and checks for duplicates (ignoring case).
     * The existing entity’s ID stays the same.</p>
     *
     * @param id the ID to update
     * @param word the new value to store
     * @return the updated entity
     * @throws IllegalArgumentException if ID is null or value is invalid
     * @throws NoSuchElementException if no entry with this ID exists
     * @throws IllegalStateException if the new value already exists elsewhere
     */
    @Transactional
    public SensitiveWord update(Long id, String word) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }

        SensitiveWord existing = sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No word found with ID " + id));

        String normalized = WordUtils.validateAndNormalize(word);

        sensitiveWordRepository.findByWordIgnoreCase(normalized)
                .filter(sWord -> !sWord.getId().equals(id))
                .ifPresent(sWord -> {
                    throw new IllegalStateException("Duplicate word: " + normalized);
                });

        existing.setWord(normalized);
        return sensitiveWordRepository.save(existing);
    }

    /**
     * Deletes a sensitive word/phrase by ID.
     *
     * <p>Does nothing if the ID doesn’t exist.</p>
     *
     * @param id the ID to delete
     * @throws IllegalArgumentException if ID is null
     */
    @Transactional
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        sensitiveWordRepository.deleteById(id);
    }

    /**
     * Masks any stored sensitive words/phrases in the given text.
     *
     * <p>Matching is case-insensitive. Whole words use \b boundaries;
     * phrases or special characters use lookarounds so they’re matched exactly,
     * without touching similar words.</p>
     *
     * @param input the text to sanitize; returns it as-is if null/empty
     * @return sanitized text with matches replaced by asterisks (same length as the match)
     */
    @Transactional(readOnly = true)
    public String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String result = input;

        for (SensitiveWord w : sensitiveWordRepository.findAll()) {
            String raw = w.getWord();
            String regex = WordUtils.buildBoundaryRegex(raw);
            String mask = "*".repeat(raw.length());
            result = result.replaceAll(regex, mask);
        }

        return result;
    }
}
