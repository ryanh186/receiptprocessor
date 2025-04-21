package com.example.receiptprocessor.controller;

import com.example.receiptprocessor.exception.ReceiptNotFoundException;
import com.example.receiptprocessor.model.Item;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.service.ReceiptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;  // use MockBean
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceiptController.class)
class ReceiptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceiptService receiptService;

    @Autowired
    private ObjectMapper objectMapper;

    private Receipt validReceipt;
    private String generatedId;

    @BeforeEach
    void setUp() {
        // build a valid Receipt
        validReceipt = new Receipt();
        validReceipt.setRetailer("Test Store");
        validReceipt.setPurchaseDate(LocalDate.of(2025, 4, 20));
        validReceipt.setPurchaseTime(LocalTime.of(15, 0));
        Item item = new Item();
        item.setShortDescription("Sample Item");
        item.setPrice("10.00");
        validReceipt.setItems(List.of(item));
        validReceipt.setTotal("10.00");

        generatedId = UUID.randomUUID().toString();
    }

    @Test
    void postProcess_ShouldReturnId() throws Exception {
        given(receiptService.processReceipt(any(Receipt.class))).willReturn(generatedId);

        mockMvc.perform(post("/receipts/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validReceipt)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(generatedId));
    }

    @Test
    void postProcess_InvalidPayload_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/receipts/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("The receipt is invalid. Please verify input."));
    }

    @Test
    void getPoints_ExistingId_ShouldReturnPoints() throws Exception {
        given(receiptService.getPoints(generatedId)).willReturn(42);

        mockMvc.perform(get("/receipts/{id}/points", generatedId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.points").value(42));
    }

    @Test
    void getPoints_NonexistentId_ShouldReturn404() throws Exception {
        given(receiptService.getPoints("bad-id"))
            .willThrow(new ReceiptNotFoundException("No receipt found for that ID."));

        mockMvc.perform(get("/receipts/{id}/points", "bad-id"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("No receipt found for that ID."));
    }
}
