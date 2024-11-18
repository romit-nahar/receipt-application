package com.example.receiptprocessor.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.receiptprocessor.model.Item;
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
        while(receipts.containsKey(id)) {
            id = UUID.randomUUID().toString();
        }
        receipts.put(id, receipt);
        // Using in memory storage for now. This can be migrated to DB whenever required.
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
        // Rule 1
        totalPoints += ruleEngine.calculateRetailerNamePoints(receipt.getRetailer());
        // Rule 2
        totalPoints += ruleEngine.calculateRoundDollarPoints(total);
        // Rule 3
        totalPoints += ruleEngine.calculateQuarterMultiplePoints(total);
        // Rule 4
        totalPoints += ruleEngine.calculateItemPairPoints(receipt.getItems());
        // Rule 5
        // We can use an AtomicInteger here to make it thread safe while scaling the app
        for (Item item : receipt.getItems()) {
            totalPoints += ruleEngine.calculateItemDescriptionPoints(item);
        }

        // Rule 6
        totalPoints += ruleEngine.calculatePurchaseDatePoints(receipt.getPurchaseDate());
        // Rule 7
        totalPoints += ruleEngine.calculatePurchaseTimePoints(receipt.getPurchaseTime());
        
        logger.info("Total points calculated for receipt {}: {}", id, totalPoints);
        return totalPoints;
    }
}