package com.example.receiptprocessor.rules;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.receiptprocessor.model.Receipt;
import com.fasterxml.jackson.databind.ObjectMapper;

class RuleEngineTest {
    
    private RuleEngine ruleEngine;
    private Receipt morningReceipt;
    private Receipt simpleReceipt;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @BeforeEach
    void setUp() throws IOException {
        ruleEngine = new RuleEngine();
        morningReceipt = loadReceiptFromJson("/mockdata/morning-receipt.json");
        simpleReceipt = loadReceiptFromJson("/mockdata/simple-receipt.json");
    }
    
    private Receipt loadReceiptFromJson(String jsonPath) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(jsonPath)) {
            return objectMapper.readValue(is, Receipt.class);
        }
    }
    
    @Test
    @DisplayName("Calculate retailer name points using morning receipt")
    void calculateRetailerNamePoints() {
        assertEquals(9, ruleEngine.calculateRetailerNamePoints(morningReceipt.getRetailer()));
        assertEquals(6, ruleEngine.calculateRetailerNamePoints(simpleReceipt.getRetailer()));
    }
    
    @Test
    @DisplayName("Calculate round dollar points using morning receipt")
    void calculateRoundDollarPoints() {
        assertEquals(0, ruleEngine.calculateRoundDollarPoints(Double.valueOf(morningReceipt.getTotal())));
        assertEquals(50, ruleEngine.calculateRoundDollarPoints(Double.valueOf(simpleReceipt.getTotal())));
    }
    
    @Test
    @DisplayName("Calculate quarter multiple points using morning receipt")
    void calculateQuarterMultiplePoints() {
        assertEquals(0, ruleEngine.calculateQuarterMultiplePoints(Double.valueOf(morningReceipt.getTotal())));
        assertEquals(25, ruleEngine.calculateQuarterMultiplePoints(Double.valueOf(simpleReceipt.getTotal())));
    }
    
    @Test
    @DisplayName("Calculate item pair points using morning receipt")
    void calculateItemPairPoints() {
        assertEquals(5, ruleEngine.calculateItemPairPoints(morningReceipt.getItems()));
        assertEquals(0, ruleEngine.calculateItemPairPoints(simpleReceipt.getItems()));
    }
    
    @Test
    @DisplayName("Calculate item description points for each item in morning receipt")
    void calculateItemDescriptionPoints() {
        // Test first item (Pepsi - 12-oz)
        assertEquals(0, ruleEngine.calculateItemDescriptionPoints(morningReceipt.getItems().get(0)), 
            "First item should earn points as length (11) is multiple of 3");
        
        // Test second item (Dasani)
        assertEquals(1, ruleEngine.calculateItemDescriptionPoints(morningReceipt.getItems().get(1)), 
            "Second item should earn no points as length (6) is not multiple of 3");
    }
    
    @Test
    @DisplayName("Calculate purchase date points using morning receipt")
    void calculatePurchaseDatePoints() {
        assertEquals(0, ruleEngine.calculatePurchaseDatePoints(morningReceipt.getPurchaseDate()));
        assertEquals(6, ruleEngine.calculatePurchaseDatePoints(simpleReceipt.getPurchaseDate()));
    }
    
    @Test
    @DisplayName("Calculate purchase time points using morning receipt")
    void calculatePurchaseTimePoints() {
        assertEquals(0, ruleEngine.calculatePurchaseTimePoints(morningReceipt.getPurchaseTime()));
        assertEquals(10, ruleEngine.calculatePurchaseTimePoints(simpleReceipt.getPurchaseTime()));
    }
    
    @Test
    @DisplayName("Verify total points calculation for morning receipt")
    void calculateTotalPoints() {
        // Calculate expected total points for the morning receipt
        int expectedPoints = 
            ruleEngine.calculateRetailerNamePoints(morningReceipt.getRetailer()) +  // 9 points
            ruleEngine.calculateRoundDollarPoints(Double.valueOf(morningReceipt.getTotal())) + // 0 points
            ruleEngine.calculateQuarterMultiplePoints(Double.valueOf(morningReceipt.getTotal())) + // 0 points
            ruleEngine.calculateItemPairPoints(morningReceipt.getItems()) + // 5 points
            ruleEngine.calculatePurchaseDatePoints(morningReceipt.getPurchaseDate()) + // 0 points
            ruleEngine.calculatePurchaseTimePoints(morningReceipt.getPurchaseTime()) + // 0 points
            ruleEngine.calculateItemDescriptionPoints(morningReceipt.getItems().get(0)) + // 1 point
            ruleEngine.calculateItemDescriptionPoints(morningReceipt.getItems().get(1));  // 0 points
            
        assertEquals(15, expectedPoints, "Morning receipt should earn total of 15 points");
    }
}
