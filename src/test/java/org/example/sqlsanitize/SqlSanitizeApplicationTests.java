package org.example.sqlsanitize;

import org.example.sqlsanitize.repository.SensitiveWordRepository;
import org.example.sqlsanitize.service.SensitiveWordService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * Boots the Spring context to ensure basic wiring is OK.
 * Uses the 'test' profile to avoid bringing up a real DataSource.
 */
@ActiveProfiles("test")
@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
        }
)
class SqlSanitizeApplicationTests {

    @MockBean
    SensitiveWordService sensitiveWordService;
    @MockBean
    SensitiveWordRepository sensitiveWordRepository;

    @Test
    void contextLoads() {
    }
}
