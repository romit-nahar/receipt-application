package com.example.receiptprocessor.service;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    // TODO: Check for mock config in spring boot apps for RuleEngine
    @Test
    @DisplayName("Calculate points should sum all rule points for morning receipt")
    void testCalculatePoints() {
        // Given
        // TODO: Declare receipts map global using a config file
        String id = receiptService.processReceipt(morningReceipt);
        // when(ruleEngine.calculateRetailerNamePoints(anyString())).thenReturn(9);
        // when(ruleEngine.calculateRoundDollarPoints(anyDouble())).thenReturn(0);
        // when(ruleEngine.calculateQuarterMultiplePoints(anyDouble())).thenReturn(0);
        // when(ruleEngine.calculateItemPairPoints(anyList())).thenReturn(5);
        // when(ruleEngine.calculateItemDescriptionPoints(any(Item.class))).thenReturn(0);
        // when(ruleEngine.calculatePurchaseDatePoints(anyString())).thenReturn(6);
        // when(ruleEngine.calculatePurchaseTimePoints(anyString())).thenReturn(10);

        // When
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
        
        // Then
        assertEquals(expectedPoints, points);
    //     verify(ruleEngine).calculateRetailerNamePoints(morningReceipt.getRetailer());
    //     verify(ruleEngine).calculateRoundDollarPoints(Double.valueOf(morningReceipt.getTotal()));
    //     verify(ruleEngine).calculateQuarterMultiplePoints(Double.valueOf(morningReceipt.getTotal()));
    //     verify(ruleEngine).calculateItemPairPoints(morningReceipt.getItems());
    //     verify(ruleEngine, times(2)).calculateItemDescriptionPoints(any(Item.class));
    //     verify(ruleEngine).calculatePurchaseDatePoints(morningReceipt.getPurchaseDate());
    //     verify(ruleEngine).calculatePurchaseTimePoints(morningReceipt.getPurchaseTime());
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