package org.example.sqlsanitize.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sqlsanitize.api.ApiCode;
import org.example.sqlsanitize.dto.SanitizeRequestDTO;
import org.example.sqlsanitize.dto.SqlSanitizeWordDTO;
import org.example.sqlsanitize.model.SensitiveWord;
import org.example.sqlsanitize.service.SensitiveWordService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensitiveWordController.class)
@ActiveProfiles("test")
class SensitiveWordControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    SensitiveWordService service;

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void getAll_returnsListOrNoContentCodeInBody() throws Exception {
        Mockito.when(service.getAllSensitiveWords())
                .thenReturn(List.of(new SensitiveWord(1L, "select")));

        mockMvc.perform(get("/api/sensitive-words"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiCode.OK.getId()))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].word").value("select"));
    }

    @Test
    void add_createsWord_returnsOkAndApiCodeCreatedInBody() throws Exception {
        Mockito.when(service.add(eq("order by")))
                .thenReturn(new SensitiveWord(10L, "order by"));

        var body = new SqlSanitizeWordDTO("order by");

        mockMvc.perform(post("/api/sensitive-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isOk()) // 200 because controller doesn't set @ResponseStatus(CREATED)
                .andExpect(jsonPath("$.code").value(ApiCode.CREATED.getId()))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.word").value("order by"));
    }

    @Test
    void update_updatesWord_returnsOk() throws Exception {
        Mockito.when(service.update(eq(5L), eq("select * from")))
                .thenReturn(new SensitiveWord(5L, "select * from"));

        var body = new SqlSanitizeWordDTO("select * from");

        mockMvc.perform(put("/api/sensitive-words/{id}", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiCode.OK.getId()))
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.word").value("select * from"));
    }

    @Test
    void deleteById_returnsOkAndNullData() throws Exception {
        mockMvc.perform(delete("/api/sensitive-words/{id}", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiCode.OK.getId()))
                .andExpect(jsonPath("$.data").doesNotExist()); // data is null
    }

    @Test
    void sanitize_returnsSanitizedString() throws Exception {
        Mockito.when(service.sanitize(eq("Select * from t order by name")))
                .thenReturn("****** * from t ******** name");

        var body = new SanitizeRequestDTO("Select * from t order by name");

        mockMvc.perform(post("/api/sensitive-words/sanitize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiCode.OK.getId()))
                .andExpect(jsonPath("$.data").value("****** * from t ******** name"));
    }
}
