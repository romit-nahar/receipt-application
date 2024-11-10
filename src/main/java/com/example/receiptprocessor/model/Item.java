package com.example.receiptprocessor.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Item {
    private String shortDescription;
    private String price;
}
