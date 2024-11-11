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

    // Rule 1: One point for every alphanumeric character in the retailer name
    public int calculateRetailerNamePoints(String retailerName) {
        int points = retailerName.replaceAll("[^A-Za-z0-9]", "").length();
        logger.debug("Retailer name '{}' earned {} points for alphanumeric characters", retailerName, points);
        return points;
    }

    // Rule 2: 50 points if the total is a round dollar amount with no cents
    public int calculateRoundDollarPoints(Double total) {
        // No points awarded if total is 0
        if (total == Math.floor(total) && total > 0) {
            logger.debug("Receipt total ${} is a round dollar amount, earned 50 points", total);
            return 50;
        }
        logger.debug("Receipt total ${} is not a round dollar amount, earned 0 points", total);
        return 0;
    }

    // Rule 3: 25 points if the total is a multiple of 0.25
    public int calculateQuarterMultiplePoints(Double total) {
        // No points awarded if total is 0
        if (total % 0.25 == 0 && total > 0) {
            logger.debug("Receipt total ${} is a multiple of 0.25, earned 25 points", total);
            return 25;
        }
        logger.debug("Receipt total ${} is not a multiple of 0.25, earned 0 points", total);
        return 0;
    }

    // Rule 4: 5 points for every two items on the receipt
    public int calculateItemPairPoints(List<Item> items) {
        int pairs = items.size() / 2;
        int points = pairs * 5;
        logger.debug("Receipt has {} items ({} pairs), earned {} points", items.size(), pairs, points);
        return points;
    }

    // Rule 5: If the trimmed length of the item description is a multiple of 3, multiply the price by 0.2 and round up to the nearest integer
    //         The result is the number of points earned
    public int calculateItemDescriptionPoints(Item item) {
        String trimmedDesc = item.getShortDescription().trim();
        if (trimmedDesc.length() % 3 == 0) {
            double price = Double.parseDouble(item.getPrice());
            int points = (int) Math.ceil(price * 0.2);
            logger.debug("Item '{}' description length ({}) is a multiple of 3, price ${}, earned {} points", 
                trimmedDesc, trimmedDesc.length(), price, points);
            return points;
        }
        logger.debug("Item '{}' description length ({}) is not a multiple of 3, earned 0 points", 
            trimmedDesc, trimmedDesc.length());
        return 0;
    }

    // Rule 6: 6 points if the day in the purchase date is odd
    public int calculatePurchaseDatePoints(String purchaseDate) {
        LocalDate date = LocalDate.parse(purchaseDate);
        if (date.getDayOfMonth() % 2 != 0) {
            logger.debug("Purchase date {} has odd day, earned 6 points", purchaseDate);
            return 6;
        }
        logger.debug("Purchase date {} has even day, earned 0 points", purchaseDate);
        return 0;
    }

    // Rule 7: 10 points if the time of purchase is after 2:00pm and before 4:00pm
    public int calculatePurchaseTimePoints(String purchaseTime) {
        LocalTime time = LocalTime.parse(purchaseTime);
        LocalTime start = LocalTime.of(14, 0);
        LocalTime end = LocalTime.of(16, 0);
        
        if (time.isAfter(start) && time.isBefore(end)) {
            logger.debug("Purchase time {} is between 2:00 PM and 4:00 PM, earned 10 points", purchaseTime);
            return 10;
        }
        logger.debug("Purchase time {} is not between 2:00 PM and 4:00 PM, earned 0 points", purchaseTime);
        return 0;
    }
}
