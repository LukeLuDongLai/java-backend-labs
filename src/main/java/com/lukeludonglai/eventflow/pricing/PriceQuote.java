package com.lukeludonglai.eventflow.pricing;

import java.math.BigDecimal;

public record PriceQuote (
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal,
        BigDecimal discountRate,
        BigDecimal discountAmount,
        BigDecimal bookingFee,
        BigDecimal finalTotal
){
    public PriceQuote{
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price must not be null");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (subtotal == null) {
            throw new IllegalArgumentException("Subtotal must not be null");
        }

        if (discountRate == null) {
            throw new IllegalArgumentException("Discount rate must not be null");
        }

        if (discountAmount == null) {
            throw new IllegalArgumentException("Discount amount must not be null");
        }

        if (bookingFee == null) {
            throw new IllegalArgumentException("Booking fee must not be null");
        }

        if (finalTotal == null) {
            throw new IllegalArgumentException("Final total must not be null");
        }

        if (unitPrice.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Unit price must not be negative");
        }

        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtotal must not be negative");
        }

        if (discountRate.compareTo(BigDecimal.ZERO) < 0
                || discountRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "Discount rate must be between 0 and 1"
            );
        }

        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Discount amount must not be negative"
            );
        }

        if (bookingFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Booking fee must not be negative"
            );
        }

        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Final total must not be negative"
            );
        }
    }
}




