package com.example.receiptprocessor.rules;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.receiptprocessor.model.Item;

@Component
public class RuleEngine {
    private static final Logger logger = LoggerFactory.getLogger(RuleEngine.class);

    public int calculateRetailerNamePoints(String retailerName) {
        int points = retailerName.replaceAll("[^A-Za-z0-9]", "").length();
        logger.info("Retailer name '{}' earned {} points for alphanumeric characters", retailerName, points);
        return points;
    }

    public int calculateRoundDollarPoints(Double total) {
        if (total == Math.floor(total)) {
            logger.info("Receipt total ${} is a round dollar amount, earned 50 points", total);
            return 50;
        }
        logger.info("Receipt total ${} is not a round dollar amount, earned 0 points", total);
        return 0;
    }

    public int calculateQuarterMultiplePoints(Double total) {
        if (total % 0.25 == 0) {
            logger.info("Receipt total ${} is a multiple of 0.25, earned 25 points", total);
            return 25;
        }
        logger.info("Receipt total ${} is not a multiple of 0.25, earned 0 points", total);
        return 0;
    }

    public int calculateItemPairPoints(List<Item> items) {
        int pairs = items.size() / 2;
        int points = pairs * 5;
        logger.info("Receipt has {} items ({} pairs), earned {} points", items.size(), pairs, points);
        return points;
    }

    public int calculateItemDescriptionPoints(Item item) {
        String trimmedDesc = item.getShortDescription().trim();
        if (trimmedDesc.length() % 3 == 0) {
            double price = Double.parseDouble(item.getPrice());
            int points = (int) Math.ceil(price * 0.2);
            logger.info("Item '{}' description length ({}) is a multiple of 3, price ${}, earned {} points", 
                trimmedDesc, trimmedDesc.length(), price, points);
            return points;
        }
        logger.info("Item '{}' description length ({}) is not a multiple of 3, earned 0 points", 
            trimmedDesc, trimmedDesc.length());
        return 0;
    }

    public int calculatePurchaseDatePoints(String purchaseDate) {
        LocalDate date = LocalDate.parse(purchaseDate);
        if (date.getDayOfMonth() % 2 != 0) {
            logger.info("Purchase date {} has odd day, earned 6 points", purchaseDate);
            return 6;
        }
        logger.info("Purchase date {} has even day, earned 0 points", purchaseDate);
        return 0;
    }

    public int calculatePurchaseTimePoints(String purchaseTime) {
        LocalTime time = LocalTime.parse(purchaseTime);
        LocalTime start = LocalTime.of(14, 0);
        LocalTime end = LocalTime.of(16, 0);
        
        if (time.isAfter(start) && time.isBefore(end)) {
            logger.info("Purchase time {} is between 2:00 PM and 4:00 PM, earned 10 points", purchaseTime);
            return 10;
        }
        logger.info("Purchase time {} is not between 2:00 PM and 4:00 PM, earned 0 points", purchaseTime);
        return 0;
    }
}
