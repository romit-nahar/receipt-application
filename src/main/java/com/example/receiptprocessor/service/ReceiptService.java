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
        // Rule 1: One point for every alphanumeric character in the retailer name
        totalPoints += ruleEngine.calculateRetailerNamePoints(receipt.getRetailer());
        // Rule 2: 50 points if the total is a round dollar amount with no cents
        totalPoints += ruleEngine.calculateRoundDollarPoints(total);
        // Rule 3: 25 points if the total is a multiple of 0.25
        totalPoints += ruleEngine.calculateQuarterMultiplePoints(total);
        // Rule 4: 5 points for every two items on the receipt
        totalPoints += ruleEngine.calculateItemPairPoints(receipt.getItems());
        // Rule 5: If the trimmed length of the item description is a multiple of 3, multiply the price by 0.2 and round up to the nearest integer
        //         The result is the number of points earned
        AtomicInteger itemPoints = new AtomicInteger(0);
        receipt.getItems().forEach(item -> 
            itemPoints.addAndGet(ruleEngine.calculateItemDescriptionPoints(item)));
        totalPoints += itemPoints.get();

        // Rule 6: 6 points if the day in the purchase date is odd
        totalPoints += ruleEngine.calculatePurchaseDatePoints(receipt.getPurchaseDate());
        // Rule 7: 10 points if the time of purchase is after 2:00pm and before 4:00pm
        totalPoints += ruleEngine.calculatePurchaseTimePoints(receipt.getPurchaseTime());
        
        logger.info("Total points calculated for receipt {}: {}", id, totalPoints);
        return totalPoints;
    }
}