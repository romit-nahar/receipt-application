package com.example.receiptprocessor.controller;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.service.ReceiptService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ReceiptController.class)
class ReceiptControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceiptService receiptService;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private Receipt morningReceipt;

    @BeforeEach
    void setUp() throws IOException {
        morningReceipt = loadReceiptFromJson("/mockdata/morning-receipt.json");
    }

    private Receipt loadReceiptFromJson(String jsonPath) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(jsonPath)) {
            return objectMapper.readValue(is, Receipt.class);
        }
    }

    @Test
    @DisplayName("Process receipt should return generated ID")
    void processReceipt() throws Exception {
        // Given
        String mockId = "123e4567-e89b-12d3-a456-426614174000";
        when(receiptService.processReceipt(any(Receipt.class))).thenReturn(mockId);

        // When/Then
        mockMvc.perform(post("/receipts/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(morningReceipt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockId));
    }

    @Test
    @DisplayName("Get points should return calculated points for valid ID")
    void getPointsForValidId() throws Exception {
        // Given
        String receiptId = "123e4567-e89b-12d3-a456-426614174000";
        int points = 100;
        when(receiptService.calculatePoints(receiptId)).thenReturn(points);

        // When/Then
        mockMvc.perform(get("/receipts/{id}/points", receiptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(points));
    }

    @Test
    @DisplayName("Get points with invalid ID should return 400 status and error message")
    void getPointsWithInvalidId() throws Exception {
        // Given
        String invalidId = "invalid-id";
        when(receiptService.calculatePoints(invalidId)).thenThrow(new IllegalArgumentException("Receipt not found"));

        // When/Then
        mockMvc.perform(get("/receipts/{id}/points", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Receipt not found"));
    }
}