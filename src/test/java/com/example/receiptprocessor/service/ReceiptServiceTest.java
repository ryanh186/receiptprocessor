package com.example.receiptprocessor.service;

import com.example.receiptprocessor.model.Item;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.repository.ReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptServiceTest {

    private ReceiptServiceImpl service;

    @BeforeEach
    void setUp() {
        ReceiptRepository repo = new ReceiptRepository();
        service = new ReceiptServiceImpl(repo);
    }

    private Receipt makeReceipt(String retailer,
                                String date,
                                String time,
                                Object[][] items,
                                String total) {
        Receipt r = new Receipt();
        r.setRetailer(retailer);
        r.setPurchaseDate(LocalDate.parse(date));
        r.setPurchaseTime(LocalTime.parse(time));
        List<Item> itemList = List.of(items).stream().map(arr -> {
            Item it = new Item();
            it.setShortDescription((String)arr[0]);
            it.setPrice((String)arr[1]);
            return it;
        }).toList();
        r.setItems(itemList);
        r.setTotal(total);
        return r;
    }

    @Test
    void testBasicRules() {
        Receipt r = makeReceipt(
            "Target",
            "2022-01-01",
            "13:01",
            new Object[][] {
                { "Mountain Dew 12PK", "6.49" },
                { "Emils Cheese Pizza", "12.25" },
                { "Knorr Creamy Chicken", "1.26" },
                { "Doritos Nacho Cheese", "3.35" },
                { "   Klarbrunn 12-PK 12 FL OZ  ", "12.00" }
            },
            "35.35"
        );

        String id = service.processReceipt(r);
        int pts = service.getPoints(id);
        assertEquals(28, pts, "Should match example total of 28 points");
    }

    @Test
    void testAllRulesApplied() {
        // M&M Corner Market on 2022-03-20 at 14:33
        Receipt r = makeReceipt(
            "M&M Corner Market",
            "2022-03-20",
            "14:33",
            new Object[][] {
                { "Gatorade", "2.25" },
                { "Gatorade", "2.25" },
                { "Gatorade", "2.25" },
                { "Gatorade", "2.25" }
            },
            "9.00"
        );

        String id = service.processReceipt(r);
        int pts = service.getPoints(id);
        assertEquals(109, pts, "Should match example total of 109 points");
    }

    @Test
    void testNotFound() {
        assertThrows(
            RuntimeException.class,
            () -> service.getPoints("nonexistent-id"),
            "Requesting unknown ID should throw ReceiptNotFoundException"
        );
    }
}
