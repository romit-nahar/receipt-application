package com.example.receiptprocessor.model;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Receipt {
    private String retailer;
    private String purchaseDate;
    private String purchaseTime;
    private List<Item> items;
    private String total;
}
