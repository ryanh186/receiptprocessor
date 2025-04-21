package com.example.receiptprocessor.controller;

import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.service.ReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {

    private final ReceiptService service;
    public ReceiptController(ReceiptService service) {
        this.service = service;
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String,String>> process(
        @Valid @RequestBody Receipt receipt
    ) {
        String id = service.processReceipt(receipt);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/{id}/points")
    public ResponseEntity<Map<String,Integer>> points(@PathVariable String id) {
        int pts = service.getPoints(id);
        return ResponseEntity.ok(Map.of("points", pts));
    }
}
