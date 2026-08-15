package com.lukeludonglai.eventflow.pricing;

import com.lukeludonglai.eventflow.domain.Event;

import java.math.BigDecimal;
import java.time.Instant;

public class StandardPricingPolicy implements PricingPolicy{
    private static final BigDecimal EARLY_BOOKING_DISCOUNT = new BigDecimal("0.10") ;
    private static final BigDecimal GROUP_DISCOUNT = new BigDecimal("0.08");
    private static final BigDecimal MAX_DISCOUNT = new BigDecimal("0.15");
    private static final BigDecimal BOOKING_FEE = new BigDecimal("1.50");
    private static final int EARLY_BOOKING_DAYS = 14;
    private static final int GROUP_MINIMUM_QUANTITY = 5;

    @Override
    public PriceQuote calculate(Event event, int quantity, Instant bookingTime) {
        // validation

        // subtotal

        // early discount

        // group discount

        // discount cap

        // discount amount

        // final total

        return null;
    }
}
