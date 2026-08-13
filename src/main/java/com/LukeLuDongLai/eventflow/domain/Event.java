package com.LukeLuDongLai.eventflow.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public class Event {
    private UUID id;
    private String title;
    private EventCategory category;
    private EventStatus status;
    private ZonedDateTime startsAt;
    private BigDecimal unitPrice;
    private int capacity;
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

    public int getAvailableTickets() {
        return availableTickets;
    }
}
