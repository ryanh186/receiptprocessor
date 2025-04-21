package com.example.receiptprocessor.service;

import com.example.receiptprocessor.model.Receipt;

public interface ReceiptService {
    /** Calculate & store points; return the generated receipt ID */
    String processReceipt(Receipt receipt);
    /** Lookup points by receipt ID; throw if not found */
    int getPoints(String receiptId);
}
