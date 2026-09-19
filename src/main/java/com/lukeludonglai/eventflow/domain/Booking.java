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

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "event_id",
            nullable = false
    )
    private Event event;

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
            Event event,
            String email,
            int quantity,
            BigDecimal totalPrice
            ){
            if (event == null) {
                throw new IllegalArgumentException("Event must not be null");
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

            this.event = event;
            this.customerEmail = email.strip();
            this.quantity = quantity;
            this.totalPrice = totalPrice;
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
        return event.getId();
    }

    public Event getEvent(){
        return event;
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
