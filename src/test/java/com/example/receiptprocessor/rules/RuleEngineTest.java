package com.example.receiptprocessor.rules;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;

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
    void testCalculateRetailerNamePoints() {
        assertEquals(9, ruleEngine.calculateRetailerNamePoints(morningReceipt.getRetailer()));
        assertEquals(6, ruleEngine.calculateRetailerNamePoints(simpleReceipt.getRetailer()));
    }
    
    @Test
    @DisplayName("Calculate round dollar points using morning receipt")
    void testCalculateRoundDollarPoints() {
        assertEquals(0, ruleEngine.calculateRoundDollarPoints(Double.valueOf(morningReceipt.getTotal())));
        assertEquals(50, ruleEngine.calculateRoundDollarPoints(Double.valueOf(simpleReceipt.getTotal())));

        // No points awarded when total is zero
        assertEquals(0, ruleEngine.calculateRoundDollarPoints(0.00));
    }
    
    @Test
    @DisplayName("Calculate quarter multiple points using morning receipt")
    void testCalculateQuarterMultiplePoints() {
        assertEquals(0, ruleEngine.calculateQuarterMultiplePoints(Double.valueOf(morningReceipt.getTotal())));
        assertEquals(25, ruleEngine.calculateQuarterMultiplePoints(Double.valueOf(simpleReceipt.getTotal())));

        // No points awarded when total is zero
        assertEquals(0, ruleEngine.calculateQuarterMultiplePoints(0.00));
    }
    
    @Test
    @DisplayName("Calculate item pair points using morning receipt")
    void testCalculateItemPairPoints() {
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
    void testCalculatePurchaseDatePoints() {
        assertEquals(0, ruleEngine.calculatePurchaseDatePoints(morningReceipt.getPurchaseDate()));
        assertEquals(6, ruleEngine.calculatePurchaseDatePoints(simpleReceipt.getPurchaseDate()));
    }
    
    @Test
    @DisplayName("Calculate purchase time points using morning receipt")
    void testCalculatePurchaseTimePoints() {
        assertEquals(0, ruleEngine.calculatePurchaseTimePoints(morningReceipt.getPurchaseTime()));
        assertEquals(10, ruleEngine.calculatePurchaseTimePoints(simpleReceipt.getPurchaseTime()));
    }
}
