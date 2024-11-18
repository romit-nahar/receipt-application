package com.example.receiptprocessor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReceiptProcessorApplicationTest {

    @Test
    @DisplayName("Verify application context loads successfully")
    void testContextLoads() {
    }

    @Test
    @DisplayName("Verify main method runs successfully")
    void mainMethodRunsSuccessfully() {
        String[] args = {};
        assertDoesNotThrow(() -> ReceiptProcessorApplication.main(args));
    }
}
