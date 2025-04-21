package com.example.receiptprocessor.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ReceiptRepository {
    // maps receipt ID → calculated points
    private final Map<String,Integer> store = new ConcurrentHashMap<>();

    public void save(String id, int points) {
        store.put(id, points);
    }

    public Integer findPointsById(String id) {
        return store.get(id);
    }
}
