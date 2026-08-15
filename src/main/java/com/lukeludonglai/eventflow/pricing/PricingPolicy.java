package com.lukeludonglai.eventflow.pricing;

import com.lukeludonglai.eventflow.domain.Event;

import java.time.Instant;

public interface PricingPolicy {
    PriceQuote calculate(
            Event event,
            int quantity,
            Instant bookingTime
    );
}
