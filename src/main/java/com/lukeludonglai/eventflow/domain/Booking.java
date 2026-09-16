package com.lukeludonglai.eventflow.domain;

import com.lukeludonglai.eventflow.exception.BookingAlreadyCancelledException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import jakarta.persistence.*;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    private UUID id;

    @Column(
            name = "event_Id",
            nullable = false
    )
    private UUID eventId;

    @Column(
            name = "customer_email",
            nullable = false,
            length = 320
    )
    private String customerEmail;

    @Column(nullable = false)
    private int quantity;

    @Column(
            name = "total_price",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    protected Booking(){

    }

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

    private Booking (
           UUID id,
           UUID eventId,
           String customerEmail,
           int quantity,
           BigDecimal totalPrice,
           BookingStatus status,
           Instant createdAt
    ){
        if (id == null){
            throw new IllegalArgumentException("Booking ID must not be null");
        }
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID must not be null");
        }
        if (customerEmail == null || customerEmail.isBlank()){
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
        if (status == null) {
            throw new IllegalArgumentException("Booking status must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Created time must not be null");
        }

        this.id = id;
        this.eventId =eventId;
        this.customerEmail = customerEmail;
        this.quantity =quantity;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    // For JDBC
    public static Booking rehydrate(
            UUID id,
            UUID eventId,
            String customerEmail,
            int quantity,
            BigDecimal totalPrice,
            BookingStatus status,
            Instant createdAt
    ){
        return new Booking(
                id,
                eventId,
                customerEmail,
                quantity,
                totalPrice,
                status,
                createdAt
        );
    }

    //service functions
    public synchronized void cancel(){
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
