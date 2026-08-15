package com.lukeludonglai.eventflow.pricing;

import com.lukeludonglai.eventflow.domain.Event;

import java.math.BigDecimal;
import java.time.Duration;
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
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }

        if (bookingTime == null) {
            throw new IllegalArgumentException("Booking time must not be null");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // subtotal
        BigDecimal unitPrice = event.getUnitPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        // early discount
        BigDecimal earlyDiscount = BigDecimal.ZERO;
        int daysAhead = (int) Duration.between(bookingTime,event.getStartsAt().toInstant()).toDays();
        if (daysAhead >= EARLY_BOOKING_DAYS) earlyDiscount = EARLY_BOOKING_DISCOUNT;

        // group discount
        BigDecimal groupDiscount = BigDecimal.ZERO;
        if (quantity >= GROUP_MINIMUM_QUANTITY) groupDiscount = GROUP_DISCOUNT;

        // discount cap
        BigDecimal discountRate = MAX_DISCOUNT.min(earlyDiscount.add(groupDiscount));

        // discount amount
        BigDecimal discountAmount = subtotal.multiply(discountRate);

        // final total
        BigDecimal finalTotal = subtotal.subtract(discountAmount).add(BOOKING_FEE);
        return new PriceQuote(event.getUnitPrice(), quantity, subtotal, discountRate, discountAmount, BOOKING_FEE, finalTotal);
    }
}
