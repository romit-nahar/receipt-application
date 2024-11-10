package com.example.receiptprocessor.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.rules.RuleEngine;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReceiptService {
    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);
    private final RuleEngine ruleEngine;
    private final Map<String, Receipt> receipts = new ConcurrentHashMap<>();
    
    public String processReceipt(Receipt receipt) {
        String id = UUID.randomUUID().toString();
        receipts.put(id, receipt);
        logger.info("Processed receipt with ID: {}", id);
        return id;
    }
    
    public int calculatePoints(String id) {
        Receipt receipt = receipts.get(id);
        if (receipt == null) {
            logger.error("Receipt not found for ID: {}", id);
            throw new IllegalArgumentException("Receipt not found");
        }
        
        logger.info("Calculating points for receipt ID: {}", id);
        int totalPoints = 0;
        Double total = Double.valueOf(receipt.getTotal());
        
        // Apply each rule and sum up points
        totalPoints += ruleEngine.calculateRetailerNamePoints(receipt.getRetailer());
        // No points earned based on total if zero
        if (total > 0) {
            totalPoints += ruleEngine.calculateRoundDollarPoints(total);
            totalPoints += ruleEngine.calculateQuarterMultiplePoints(total);
        }
        
        totalPoints += ruleEngine.calculateItemPairPoints(receipt.getItems());
        
        // Calculate points for each item description
        AtomicInteger itemPoints = new AtomicInteger(0);
        receipt.getItems().forEach(item -> 
            itemPoints.addAndGet(ruleEngine.calculateItemDescriptionPoints(item)));
        totalPoints += itemPoints.get();

        totalPoints += ruleEngine.calculatePurchaseDatePoints(receipt.getPurchaseDate());
        totalPoints += ruleEngine.calculatePurchaseTimePoints(receipt.getPurchaseTime());
        
        logger.info("Total points calculated for receipt {}: {}", id, totalPoints);
        return totalPoints;
    }
}