package org.example.sqlsanitize.service;

import org.example.sqlsanitize.model.SensitiveWord;
import org.example.sqlsanitize.repository.SensitiveWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensitiveWordServiceTest {

    @Mock
    SensitiveWordRepository repo;

    SensitiveWordService service;

    @BeforeEach
    void setUp() {
        service = new SensitiveWordService(repo);
    }

    @Test
    void getAllSensitiveWords_sortsCaseInsensitive() {
        when(repo.findAll()).thenReturn(new java.util.ArrayList<>(List.of(
                new SensitiveWord(2L, "select"),
                new SensitiveWord(1L, "ORDER BY"),
                new SensitiveWord(3L, "*")
        )));

        List<SensitiveWord> result = service.getAllSensitiveWords();

        assertEquals(3, result.size());
        assertEquals("*", result.get(0).getWord());
        assertEquals("ORDER BY", result.get(1).getWord());
        assertEquals("select", result.get(2).getWord());
    }

    @Test
    void add_insertsNormalized_whenNotDuplicate() {
        when(repo.existsByWordIgnoreCase("select")).thenReturn(false);
        when(repo.save(any(SensitiveWord.class)))
                .thenAnswer(inv -> {
                    SensitiveWord w = inv.getArgument(0);
                    w.setId(10L);
                    return w;
                });

        SensitiveWord saved = service.add("  SELECT  ");

        ArgumentCaptor<SensitiveWord> captor = ArgumentCaptor.forClass(SensitiveWord.class);
        verify(repo).save(captor.capture());
        assertEquals("select", captor.getValue().getWord());
        assertEquals(10L, saved.getId());
        assertEquals("select", saved.getWord());
    }

    @Test
    void add_duplicate_throws() {
        when(repo.existsByWordIgnoreCase("select")).thenReturn(true);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.add("select"));
        assertTrue(ex.getMessage().toLowerCase().contains("exists"));
        verify(repo, never()).save(any());
    }

    @Test
    void update_success() {
        when(repo.findById(5L)).thenReturn(Optional.of(new SensitiveWord(5L, "old")));
        when(repo.findByWordIgnoreCase("order by")).thenReturn(Optional.empty());
        when(repo.save(any(SensitiveWord.class))).thenAnswer(inv -> inv.getArgument(0));

        SensitiveWord updated = service.update(5L, "  OrDeR By ");

        assertEquals(5L, updated.getId());
        assertEquals("order by", updated.getWord());
        InOrder inOrder = inOrder(repo);
        inOrder.verify(repo).findById(5L);
        inOrder.verify(repo).findByWordIgnoreCase("order by");
        inOrder.verify(repo).save(any(SensitiveWord.class));
    }

    @Test
    void update_notFound_throws() {
        when(repo.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.update(9L, "select"));
    }

    @Test
    void update_duplicateOther_throws() {
        when(repo.findById(5L)).thenReturn(Optional.of(new SensitiveWord(5L, "any")));
        when(repo.findByWordIgnoreCase("select"))
                .thenReturn(Optional.of(new SensitiveWord(99L, "select")));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.update(5L, "select"));
        assertTrue(ex.getMessage().toLowerCase().contains("duplicate"));
        verify(repo, never()).save(any());
    }

    @Test
    void deleteById_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteById(null));
        verify(repo, never()).deleteById(any());
    }

    @Test
    void sanitize_masksWordsPhrasesAndSymbols() {
        when(repo.findAll()).thenReturn(List.of(
                new SensitiveWord(1L, "select"),
                new SensitiveWord(2L, "order by"),
                new SensitiveWord(3L, "*")
        ));

        String out = service.sanitize("Select * from t order by name");

        assertEquals("****** * from t ******** name", out);
    }

    @Test
    void sanitize_nullOrEmpty_returnsAsIs() {
        assertNull(service.sanitize(null));
        assertEquals("", service.sanitize(""));
    }
}
