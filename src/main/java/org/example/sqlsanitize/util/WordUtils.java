package org.example.sqlsanitize.util;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Util class that can be used to validate and normalize words,
 * and a boundary regex builder to catch SQL words and characters.
 */
public final class WordUtils {

    /** The \w covers letters, digits, underscore (A–Z, a–z, 0–9, _). */
    private static final Pattern WORD_ONLY_PATTERN = Pattern.compile("^\\w+$");

    /** Flag to say that we want to test ignoring case sensitivity */
    private static final String CASE_INSENSITIVE_FLAG = "(?i)";

    /** This is to show the boundary of a word */
    private static final String WORD_BOUNDARY = "\\b";

    /**
     * The below regex is to ensure that we cater for the following scenarios:
     * 1. Whole words only – e.g. it will match “SELECT” in “SELECT * FROM” but not the “SELECT” inside “UNSELECTED”.
     * 2. Multi-word phrases – e.g. it will match “ORDER BY” as one block, not “ORDER” and “BY” separately.
     * 3. Symbols and punctuation – e.g. it will still match “*” when surrounded by spaces.
     * <p>
     Backwards check for a non-word character (space) or start of the text. */
    private static final String LOOKAROUND_PREFIX = "(?<=\\W|^)";

    /** Forward check for a non-word character or end of the text. */
    private static final String LOOKAROUND_SUFFIX = "(?=\\W|$)";

    private WordUtils() { }

    /**
     * Validates that the given raw input is non-null, non-blank,
     * then returns it trimmed and lower-cased.
     *
     * @param rawInput the raw word input from the client
     * @return a trimmed, lower-case version of {@code rawInput}
     * @throws IllegalArgumentException if {@code rawInput} is null or blank (after trimming)
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


    // TODO: (TEST) try to break buildBoundaryRegex as it might not cater for all scenarios yet
    /**
     * Builds a regex that matches exactly the given SQL keyword or phrase:
     * <ul>
     *   <li>If {@code term} is purely letters/digits/underscore (e.g. "SELECT"),
     *       we use \b…\b.</li>
     *   <li>Otherwise (spaces, symbols inside), we use look-arounds to ensure that we match the whole phrase.</li>
     * </ul>
     *
     * @param term the exact keyword or phrase, e.g. "ORDER BY" or "SELECT * FROM"
     * @return a case-insensitive regex that matches only that term
     */
    public static String buildBoundaryRegex(String term) {
        String quotedTerm = Pattern.quote(term);

        if (WORD_ONLY_PATTERN.matcher(term).matches()) {
            // If there are no symbols or weird characters to cater for
            return CASE_INSENSITIVE_FLAG
                    + WORD_BOUNDARY
                    + quotedTerm
                    + WORD_BOUNDARY;
        }

        // If we are dealing with phrases or terms that contain symbols
        return CASE_INSENSITIVE_FLAG
                + LOOKAROUND_PREFIX
                + quotedTerm
                + LOOKAROUND_SUFFIX;
    }
}
