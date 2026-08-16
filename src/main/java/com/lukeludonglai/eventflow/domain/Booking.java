package com.lukeludonglai.eventflow.domain;

import com.lukeludonglai.eventflow.exception.BookingAlreadyCancelledException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class Booking {
    private UUID id;
    private UUID eventId;
    private String customerEmail;
    private int quantity;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private Instant createdAt;

    public Booking (
            UUID eventId,
            String email,
            int quantity,
            BigDecimal totalPrice
            ){
            if (eventId == null) {
                throw new IllegalArgumentException("Event ID must not be null");
            }
            if (email == null || email.isBlank()){
                throw new IllegalArgumentException("Email must not be blank");
            }
            if(quantity <= 0){
                throw new IllegalArgumentException("Booking quantity must be positive");
            }
            if (totalPrice == null){
                throw new IllegalArgumentException("Total price must not be null");
            }
            if (totalPrice.compareTo(BigDecimal.ZERO) < 0){
                throw new IllegalArgumentException("Total price must not be negative");
            }

            this.id = UUID.randomUUID();
            this.status = BookingStatus.CONFIRMED;
            this.createdAt = Instant.now();

            this.eventId = eventId;
            this.customerEmail = email.strip();
            this.quantity = quantity;
            this.totalPrice = totalPrice;
    }

    //service functions
    public void cancel(){
        if (this.status == BookingStatus.CANCELLED){
            throw new BookingAlreadyCancelledException(this.id);
        }
        this.status = BookingStatus.CANCELLED;
    }


    //getters
    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
