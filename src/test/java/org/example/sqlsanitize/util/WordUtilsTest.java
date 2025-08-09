package org.example.sqlsanitize.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordUtilsTest {

    @Test
    void validateAndNormalize_trimsAndLowercases() {
        assertEquals("select", WordUtils.validateAndNormalize("  SELECT  "));
    }

    @Test
    void validateAndNormalize_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> WordUtils.validateAndNormalize(null));
    }

    @Test
    void validateAndNormalize_blank_throws() {
        assertThrows(IllegalArgumentException.class, () -> WordUtils.validateAndNormalize("   "));
    }

    @Test
    void buildBoundaryRegex_word_usesWordBoundaries() {
        String r = WordUtils.buildBoundaryRegex("SELECT");
        assertTrue(r.startsWith("(?i)"));
        assertTrue(r.contains("\\b"));
    }

    @Test
    void buildBoundaryRegex_phrase_usesLookarounds() {
        String r = WordUtils.buildBoundaryRegex("ORDER BY");
        assertTrue(r.startsWith("(?i)"));
        assertTrue(r.contains("(?<=\\W|^)"));
        assertTrue(r.contains("(?=\\W|$)"));
    }

    @Test
    void buildBoundaryRegex_symbol_usesLookarounds() {
        String r = WordUtils.buildBoundaryRegex("*");
        assertTrue(r.contains("(?<=\\W|^)"));
        assertTrue(r.contains("(?=\\W|$)"));
    }
}
