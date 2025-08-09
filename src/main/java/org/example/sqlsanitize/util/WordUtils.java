package org.example.sqlsanitize.util;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Small helper utilities for dealing with sensitive words/phrases:
 * <ul>
 *   <li>Normalize user input (trim + lowercase)</li>
 *   <li>Build a safe, case-insensitive regex that matches whole words or full phrases</li>
 * </ul>
 */
public final class WordUtils {

    /** Matches strings made only of letters/digits/underscore (i.e., what \w covers). */
    private static final Pattern WORD_ONLY_PATTERN = Pattern.compile("^\\w+$");

    /** Case-insensitive flag for the regex. */
    private static final String CASE_INSENSITIVE_FLAG = "(?i)";

    /** Word boundary token for pure word matches. */
    private static final String WORD_BOUNDARY = "\\b";

    /**
     * Look-behind that says: the char before the match must be a non-word char (space, punctuation, etc.) or start of text.
     * Helps us match a whole phrase like "order by" but not the "order" inside "preorder".
     */
    private static final String LOOKAROUND_PREFIX = "(?<=\\W|^)";

    /**
     * Look-ahead that says: the char after the match must be a non-word char or end of text.
     * Same idea as above, but on the right side of the match.
     */
    private static final String LOOKAROUND_SUFFIX = "(?=\\W|$)";

    private WordUtils() { }

    /**
     * Validate that input is non-null and not blank, then trim and lowercase it.
     *
     * @param rawInput raw word/phrase from the client
     * @return trimmed, lowercase version of the input
     * @throws IllegalArgumentException if input is null or blank after trimming
     */
    public static String validateAndNormalize(String rawInput) {
        if (Objects.isNull(rawInput)) {
            throw new IllegalArgumentException("Word must not be null");
        }
        String trimmedInput = rawInput.trim();
        if (trimmedInput.isEmpty()) {
            throw new IllegalArgumentException("Word must not be blank");
        }
        return trimmedInput.toLowerCase();
    }

    /**
     * Build a regex that matches the given term exactly (not partials), case-insensitively.
     * <ul>
     *   <li><strong>Pure word</strong> (letters/digits/_): use {@code \b... \b} so "select" won’t match "selected".</li>
     *   <li><strong>Phrase or symbols</strong> (spaces, "*", etc.): use lookarounds so we still match the whole term,
     *       e.g. "order by" or "*" only when it stands alone (surrounded by non-word chars or edges).</li>
     * </ul>
     * All special characters in the term are escaped via {@link Pattern#quote(String)} to avoid regex surprises.
     *
     * @param term the exact word/phrase to match (e.g., "SELECT", "ORDER BY", "*", "select * from")
     * @return a regex string you can pass to {@code String.replaceAll} or {@code Pattern.compile}
     */
    public static String buildBoundaryRegex(String term) {
        String quotedTerm = Pattern.quote(term);

        // Simple case: just a single "word" (letters/digits/_)
        if (WORD_ONLY_PATTERN.matcher(term).matches()) {
            return CASE_INSENSITIVE_FLAG + WORD_BOUNDARY + quotedTerm + WORD_BOUNDARY;
        }

        // Otherwise: phrases/symbols; anchor with non-word or string edges
        return CASE_INSENSITIVE_FLAG + LOOKAROUND_PREFIX + quotedTerm + LOOKAROUND_SUFFIX;
    }
}
