package com.example.receiptprocessor.service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.rules.RuleEngine;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private RuleEngine ruleEngine;

    private ReceiptService receiptService;
    private Receipt morningReceipt;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        receiptService = new ReceiptService(ruleEngine);
        morningReceipt = loadReceiptFromJson("/mockdata/morning-receipt.json");
    }

    private Receipt loadReceiptFromJson(String jsonPath) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(jsonPath)) {
            return objectMapper.readValue(is, Receipt.class);
        }
    }

    @Test
    @DisplayName("Process receipt should generate UUID and store receipt")
    void testProcessReceipt() {
        // When
        String id = receiptService.processReceipt(morningReceipt);

        // Then
        assertNotNull(id);
        assertTrue(id.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    @DisplayName("Process receipt should handle UUID collisions")
    void testProcessReceiptWithCollision() throws Exception {
        // Given
        String existingId = "550e8400-e29b-41d4-a716-446655440000";
        
        // Use reflection to access the private receipts map
        Field receiptsField = ReceiptService.class.getDeclaredField("receipts");
        receiptsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Receipt> receipts = (Map<String, Receipt>) receiptsField.get(receiptService);
        
        // Add a receipt with known UUID to force a collision
        receipts.put(existingId, morningReceipt);
        
        // When
        String newId = receiptService.processReceipt(morningReceipt);

        // Then
        assertNotNull(newId);
        assertNotEquals(existingId, newId);
        assertTrue(newId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        assertEquals(2, receipts.size());
    }

    @Test
    @DisplayName("Calculate points should sum all rule points for morning receipt")
    void testCalculatePoints() {
        String id = receiptService.processReceipt(morningReceipt);

        int points = receiptService.calculatePoints(id);
        int expectedPoints = 
            ruleEngine.calculateRetailerNamePoints(morningReceipt.getRetailer()) +
            ruleEngine.calculateRoundDollarPoints(Double.valueOf(morningReceipt.getTotal())) +
            ruleEngine.calculateQuarterMultiplePoints(Double.valueOf(morningReceipt.getTotal())) +
            ruleEngine.calculateItemPairPoints(morningReceipt.getItems()) +
            morningReceipt.getItems().stream()
                .mapToInt(ruleEngine::calculateItemDescriptionPoints)
                .sum() +
            ruleEngine.calculatePurchaseDatePoints(morningReceipt.getPurchaseDate()) +
            ruleEngine.calculatePurchaseTimePoints(morningReceipt.getPurchaseTime());
        
        assertEquals(expectedPoints, points);
    }

    @Test
    @DisplayName("Calculate points with invalid ID should throw exception")
    void calculatePointsWithInvalidId() {
        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> receiptService.calculatePoints("invalid-id")
        );
        assertEquals("Receipt not found", exception.getMessage());
    }
}