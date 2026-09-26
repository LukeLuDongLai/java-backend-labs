package com.lukeludonglai.eventflow.domain;

import com.lukeludonglai.eventflow.exception.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {
    @Id
    private UUID id;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(
            nullable = false,
            length = 50
    )
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private EventCategory category;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private EventStatus status;

    @Column(
            name = "starts_at",
            nullable = false
    )
    private ZonedDateTime startsAt;

    @Column(
            name = "unit_price",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int capacity;

    @Column(
            name = "available_tickets",
            nullable = false
    )
    private int availableTickets;

    public Event(
            String title,
            EventCategory category,
            ZonedDateTime startAt,
            BigDecimal unitPrice,
            int capacity
    ){

        if (title == null || title.isBlank()){
            throw new IllegalArgumentException("Event title must not be blank");
        }
        if (category == null){
            throw  new IllegalArgumentException("Event category must not be null");
        }
        if (startAt == null){
            throw new IllegalArgumentException("Event start time must not be null");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price must not be null");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Unit price must not be negative");
        }
        if (capacity < 0){
            throw new IllegalArgumentException("Capacity must not be negative");
        }

        // default attribute
        this.id = UUID.randomUUID();
        this.status = EventStatus.DRAFT;
        this.availableTickets = capacity;
        // verified attribute
        this.title = title.strip();
        this.category = category;
        this.startsAt = startAt;
        this.unitPrice = unitPrice;
        this.capacity = capacity;
    }

    protected Event(){
        // Required by JPA
    }


    //Service functions
    public void publish() {
        this.status = EventStatus.PUBLISHED;
    }

    public boolean isPublished(){
        return this.status == EventStatus.PUBLISHED;
    }

    public synchronized void reserveTickets(int quantity) {
        if (quantity <= 0){
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > this.availableTickets){
            throw new InsufficientTicketsException(quantity, this.availableTickets);
        }
        this.availableTickets -= quantity;
    }

    public synchronized void releaseTickets(int quantity){
        if (quantity <= 0){
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.availableTickets += quantity;
    }

    //Getters
    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public EventCategory getCategory() {
        return category;
    }

    public EventStatus getStatus() {
        return status;
    }

    public ZonedDateTime getStartsAt() {
        return startsAt;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getCapacity() {
        return capacity;
    }

    public synchronized int getAvailableTickets() {
        return availableTickets;
    }

    public long getVersion(){ return version; }
}
