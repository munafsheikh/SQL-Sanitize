package org.example.sqlsanitize.bt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sqlsanitize.model.SensitiveWord;
import org.example.sqlsanitize.repository.SensitiveWordRepository;
import org.example.sqlsanitize.util.WordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveWordSeeder implements CommandLineRunner {

    private final SensitiveWordRepository repository;

    @Value("${seed.words.file:classpath:sql_sensitive_list.txt}")
    private Resource seedFile;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (seedFile == null || !seedFile.exists()) {
            log.warn("Seed file not found or not provided; skipping seeding.");
            return;
        }

        try (InputStream in = seedFile.getInputStream()) {
            List<String> terms = objectMapper.readValue(in, new TypeReference<>() {
            });
            int inserted = 0, skipped = 0;

            for (String raw : terms) {
                String normalized = WordUtils.validateAndNormalize(raw);

                if (repository.existsByWordIgnoreCase(normalized)) {
                    skipped++;
                    continue;
                }

                SensitiveWord w = new SensitiveWord();
                w.setWord(normalized);
                repository.save(w);
                inserted++;
            }

            log.info("SensitiveWord seeding complete: inserted={}, skipped={} (from {}).",
                    inserted, skipped, seedFile.getDescription());
        }
    }
}
