package com.example.receiptprocessor.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.service.ReceiptService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/receipts")
@RequiredArgsConstructor
public class ReceiptController {
    private static final Logger logger = LoggerFactory.getLogger(ReceiptController.class);
    private final ReceiptService receiptService;
    
    @PostMapping("/process")
    public ResponseEntity<Map<String, String>> processReceipt(@RequestBody Receipt receipt) {
        logger.info("Received request to process receipt for retailer: {}", receipt.getRetailer());
        String id = receiptService.processReceipt(receipt);
        logger.info("Successfully processed receipt. Generated ID: {}", id);
        return ResponseEntity.ok(Map.of("id", id));
    }
    
    @GetMapping("/{id}/points")
    public ResponseEntity<Map<String, Integer>> getPoints(@PathVariable("id") String id) {
        logger.info("Received request to calculate points for receipt ID: {}", id);
        int points = receiptService.calculatePoints(id);
        logger.info("Calculated {} points for receipt ID: {}", points, id);
        return ResponseEntity.ok(Map.of("points", points));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
