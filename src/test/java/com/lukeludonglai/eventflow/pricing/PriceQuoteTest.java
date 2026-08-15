package com.lukeludonglai.eventflow.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PriceQuoteTest {
    @Test
    void shouldCreateValidPriceQuote() {
        PriceQuote quote = createValidQuote();

        assertAll(
                () -> assertEquals(new BigDecimal("20.00"), quote.unitPrice()),
                () -> assertEquals(5, quote.quantity()),
                () -> assertEquals(new BigDecimal("100.00"), quote.subtotal()),
                () -> assertEquals(new BigDecimal("0.15"), quote.discountRate()),
                () -> assertEquals(new BigDecimal("15.00"), quote.discountAmount()),
                () -> assertEquals(new BigDecimal("1.50"), quote.bookingFee()),
                () -> assertEquals(new BigDecimal("86.50"), quote.finalTotal())
        );
    }

    @Test
    void shouldRejectNullUnitPrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        null,
                        5,
                        new BigDecimal("100.00"),
                        new BigDecimal("0.15"),
                        new BigDecimal("15.00"),
                        new BigDecimal("1.50"),
                        new BigDecimal("86.50")
                )
        );
    }

    @Test
    void shouldRejectNonPositiveQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        new BigDecimal("20.00"),
                        0,
                        new BigDecimal("100.00"),
                        new BigDecimal("0.15"),
                        new BigDecimal("15.00"),
                        new BigDecimal("1.50"),
                        new BigDecimal("86.50")
                )
        );
    }

    @Test
    void shouldRejectNullSubtotal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        new BigDecimal("20.00"),
                        5,
                        null,
                        new BigDecimal("0.15"),
                        new BigDecimal("15.00"),
                        new BigDecimal("1.50"),
                        new BigDecimal("86.50")
                )
        );
    }

    @Test
    void shouldRejectNullDiscountRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        new BigDecimal("20.00"),
                        5,
                        new BigDecimal("100.00"),
                        null,
                        new BigDecimal("15.00"),
                        new BigDecimal("1.50"),
                        new BigDecimal("86.50")
                )
        );
    }

    @Test
    void shouldRejectNullDiscountAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        new BigDecimal("20.00"),
                        5,
                        new BigDecimal("100.00"),
                        new BigDecimal("0.15"),
                        null,
                        new BigDecimal("1.50"),
                        new BigDecimal("86.50")
                )
        );
    }

    @Test
    void shouldRejectNullBookingFee() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        new BigDecimal("20.00"),
                        5,
                        new BigDecimal("100.00"),
                        new BigDecimal("0.15"),
                        new BigDecimal("15.00"),
                        null,
                        new BigDecimal("86.50")
                )
        );
    }

    @Test
    void shouldRejectNullFinalTotal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        new BigDecimal("20.00"),
                        5,
                        new BigDecimal("100.00"),
                        new BigDecimal("0.15"),
                        new BigDecimal("15.00"),
                        new BigDecimal("1.50"),
                        null
                )
        );
    }

    @Test
    void shouldRejectNegativeUnitPrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        new BigDecimal("-0.01"),
                        5,
                        new BigDecimal("100.00"),
                        new BigDecimal("0.15"),
                        new BigDecimal("15.00"),
                        new BigDecimal("1.50"),
                        new BigDecimal("86.50")
                )
        );
    }

    @Test
    void shouldRejectNegativeSubtotal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        new BigDecimal("20.00"),
                        5,
                        new BigDecimal("-0.01"),
                        new BigDecimal("0.15"),
                        new BigDecimal("15.00"),
                        new BigDecimal("1.50"),
                        new BigDecimal("86.50")
                )
        );
    }

    @Test
    void shouldRejectNegativeDiscountRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        new BigDecimal("20.00"),
                        5,
                        new BigDecimal("100.00"),
                        new BigDecimal("-0.01"),
                        new BigDecimal("15.00"),
                        new BigDecimal("1.50"),
                        new BigDecimal("86.50")
                )
        );
    }

    @Test
    void shouldRejectDiscountRateGreaterThanOne() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        new BigDecimal("20.00"),
                        5,
                        new BigDecimal("100.00"),
                        new BigDecimal("1.01"),
                        new BigDecimal("15.00"),
                        new BigDecimal("1.50"),
                        new BigDecimal("86.50")
                )
        );
    }

    @Test
    void shouldRejectNegativeDiscountAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        new BigDecimal("20.00"),
                        5,
                        new BigDecimal("100.00"),
                        new BigDecimal("0.15"),
                        new BigDecimal("-0.01"),
                        new BigDecimal("1.50"),
                        new BigDecimal("86.50")
                )
        );
    }

    @Test
    void shouldRejectNegativeBookingFee() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        new BigDecimal("20.00"),
                        5,
                        new BigDecimal("100.00"),
                        new BigDecimal("0.15"),
                        new BigDecimal("15.00"),
                        new BigDecimal("-0.01"),
                        new BigDecimal("86.50")
                )
        );
    }

    @Test
    void shouldRejectNegativeFinalTotal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PriceQuote(
                        new BigDecimal("20.00"),
                        5,
                        new BigDecimal("100.00"),
                        new BigDecimal("0.15"),
                        new BigDecimal("15.00"),
                        new BigDecimal("1.50"),
                        new BigDecimal("-0.01")
                )
        );
    }

    private PriceQuote createValidQuote() {
        return new PriceQuote(
                new BigDecimal("20.00"),
                5,
                new BigDecimal("100.00"),
                new BigDecimal("0.15"),
                new BigDecimal("15.00"),
                new BigDecimal("1.50"),
                new BigDecimal("86.50")
        );
    }
}