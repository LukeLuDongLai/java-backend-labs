package com.lukeludonglai.eventflow.search;

import com.lukeludonglai.eventflow.domain.EventCategory;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record EventSearchCriteria (
        String keyword,
        EventCategory category,
        ZonedDateTime from,
        ZonedDateTime to,
        BigDecimal maxPrice,
        EventSort sort
) {
    public EventSearchCriteria{
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Price must not be negative");
        }
        if ( from != null && to != null && from().isAfter(to)){
            throw new IllegalArgumentException("\"from\" date must not be \"after\" date");
        }
    }
}
