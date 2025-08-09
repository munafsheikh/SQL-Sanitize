package org.example.sqlsanitize.repository;

import org.example.sqlsanitize.model.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for performing CRUD and query operations on
 * {@link SensitiveWord} entities.
 * <p>
 * Extends {@link JpaRepository} to inherit standard JPA methods
 * for saving, deleting, and finding entities by their primary key.
 * </p>
 */
@Repository
public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {
    /**
     * Checks whether a {@link SensitiveWord} with the exact given word
     * (ignoring case differences) already exists in the database.
     *
     * @param word the word to check for (case-insensitive)
     * @return {@code true} if at least one entry has the same word
     * (ignoring case), {@code false} otherwise
     * @throws IllegalArgumentException if {@code word} is {@code null}
     */
    boolean existsByWordIgnoreCase(String word);

    /**
     * Attempts to find a single {@link SensitiveWord} by its word value,
     * ignoring case differences.
     *
     * @param word the exact word to look up (case-insensitive)
     * @return an {@link Optional} containing the matching entity,
     * or empty if no match is found
     * @throws IllegalArgumentException if {@code word} is {@code null}
     */
    Optional<SensitiveWord> findByWordIgnoreCase(String word);
}