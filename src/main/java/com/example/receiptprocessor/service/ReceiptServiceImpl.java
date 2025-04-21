package com.example.receiptprocessor.service;

import com.example.receiptprocessor.exception.ReceiptNotFoundException;
import com.example.receiptprocessor.model.Item;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.repository.ReceiptRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class ReceiptServiceImpl implements ReceiptService {
    private final ReceiptRepository repo;

    public ReceiptServiceImpl(ReceiptRepository repo) {
        this.repo = repo;
    }

    @Override
    public String processReceipt(Receipt receipt) {
        int points = calculatePoints(receipt);
        String id = UUID.randomUUID().toString();
        repo.save(id, points);
        return id;
    }

    @Override
    public int getPoints(String receiptId) {
        Integer pts = repo.findPointsById(receiptId);
        if (pts == null) {
            throw new ReceiptNotFoundException("No receipt found for that ID.");
        }
        return pts;
    }

    private int calculatePoints(Receipt r) {
        int points = 0;
    
        // 1) 1 point per alphanumeric character in retailer
        for (char c : r.getRetailer().toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                points++;
            }
        }
    
        // parse total once
        BigDecimal total = new BigDecimal(r.getTotal());
    
        // 2) +50 if total is a whole dollar (no cents)
        if (total.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            points += 50;
        }
    
        // 3) +25 if total is a multiple of 0.25
        if (total.remainder(new BigDecimal("0.25")).compareTo(BigDecimal.ZERO) == 0) {
            points += 25;
        }
    
        // 4) +5 points for every two items
        int itemCount = r.getItems().size();
        points += (itemCount / 2) * 5;
    
        // 5) Item‑description rule
        for (Item item : r.getItems()) {
            String desc = item.getShortDescription().trim();
            if (desc.length() % 3 == 0) {
                int bonus = new BigDecimal(item.getPrice())
                    .multiply(new BigDecimal("0.20"))
                    .setScale(0, RoundingMode.UP)
                    .intValue();
                points += bonus;
            }
        }
    
        // 6) +6 if the purchase date’s day is odd
        if (r.getPurchaseDate().getDayOfMonth() % 2 == 1) {
            points += 6;
        }
    
        // 7) +10 if purchase time after 14:00 and before 16:00
        int hour = r.getPurchaseTime().getHour();
        int minute = r.getPurchaseTime().getMinute();
        if ((hour > 14 || (hour == 14 && minute > 0))
         && (hour < 16 || (hour == 16 && minute == 0))) {
            points += 10;
        }
    
        return points;
    }    
}
