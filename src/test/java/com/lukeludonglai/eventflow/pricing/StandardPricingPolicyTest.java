package com.lukeludonglai.eventflow.pricing;

import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;


import static org.junit.jupiter.api.Assertions.*;

class StandardPricingPolicyTest {
    private StandardPricingPolicy pricingPolicy;
    private Event event;

    @BeforeEach
    void setUp() {
        pricingPolicy = new StandardPricingPolicy();

        event = new Event(
                "Barcelona Java Meetup",
                EventCategory.TECH,
                ZonedDateTime.of(
                        2026,
                        9,
                        20,
                        18,
                        0,
                        0,
                        0,
                        ZoneId.of("Europe/Madrid")
                ),
                new BigDecimal("20.00"),
                100
        );
    }

    @Test
    void shouldCalculatePriceWithoutDiscount() {
        Instant bookingTime =
                event.getStartsAt()
                        .minusDays(10)
                        .toInstant();

        PriceQuote quote =
                pricingPolicy.calculate(event, 1, bookingTime);

        assertAll(
                () -> assertEquals(
                        new BigDecimal("20.00"),
                        quote.unitPrice()
                ),
                () -> assertEquals(
                        1,
                        quote.quantity()
                ),
                () -> assertEquals(
                        new BigDecimal("20.00"),
                        quote.subtotal()
                ),
                () -> assertEquals(
                        new BigDecimal("0"),
                        quote.discountRate(),
                        "Discount amount should be zero when no discount applies"
                ),
                () -> assertEquals(
                        new BigDecimal("0.00"),
                        quote.discountAmount()
                ),
                () -> assertEquals(
                        new BigDecimal("1.50"),
                        quote.bookingFee()
                ),
                () -> assertEquals(
                        new BigDecimal("21.50"),
                        quote.finalTotal()
                )
        );
    }

    @Test
    void shouldNotApplyEarlyDiscountThirteenDaysBeforeEvent() {
        Instant bookingTime =
                event.getStartsAt()
                        .minusDays(13)
                        .toInstant();

        PriceQuote quote =
                pricingPolicy.calculate(event, 1, bookingTime);

        assertEquals(
                0,
                quote.discountRate().compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void shouldApplyEarlyDiscountExactlyFourteenDaysBeforeEvent() {
        Instant bookingTime =
                event.getStartsAt()
                        .minusDays(14)
                        .toInstant();

        PriceQuote quote =
                pricingPolicy.calculate(event, 1, bookingTime);

        assertEquals(
                0,
                quote.discountRate()
                        .compareTo(new BigDecimal("0.10"))
        );

        assertEquals(
                0,
                quote.discountAmount()
                        .compareTo(new BigDecimal("2.00"))
        );

        assertEquals(
                0,
                quote.finalTotal()
                        .compareTo(new BigDecimal("19.50"))
        );
    }

    @Test
    void shouldNotApplyGroupDiscountForFourTickets() {
        Instant bookingTime =
                event.getStartsAt()
                        .minusDays(5)
                        .toInstant();

        PriceQuote quote =
                pricingPolicy.calculate(event, 4, bookingTime);

        assertEquals(
                0,
                quote.discountRate().compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void shouldApplyGroupDiscountForFiveTickets() {
        Instant bookingTime =
                event.getStartsAt()
                        .minusDays(5)
                        .toInstant();

        PriceQuote quote =
                pricingPolicy.calculate(event, 5, bookingTime);

        assertEquals(
                0,
                quote.discountRate()
                        .compareTo(new BigDecimal("0.08"))
        );

        assertEquals(
                0,
                quote.subtotal()
                        .compareTo(new BigDecimal("100.00"))
        );

        assertEquals(
                0,
                quote.discountAmount()
                        .compareTo(new BigDecimal("8.00"))
        );

        assertEquals(
                0,
                quote.finalTotal()
                        .compareTo(new BigDecimal("93.50"))
        );
    }

    @Test
    void shouldCapCombinedDiscountAtFifteenPercent() {
        Instant bookingTime =
                event.getStartsAt()
                        .minusDays(20)
                        .toInstant();

        PriceQuote quote =
                pricingPolicy.calculate(event, 5, bookingTime);

        assertEquals(
                0,
                quote.discountRate()
                        .compareTo(new BigDecimal("0.15"))
        );

        assertEquals(
                0,
                quote.discountAmount()
                        .compareTo(new BigDecimal("15.00"))
        );

        assertEquals(
                0,
                quote.finalTotal()
                        .compareTo(new BigDecimal("86.50"))
        );
    }

    @Test
    void shouldApplyBookingFeeOnlyOnceForMultipleTickets() {
        Instant bookingTime =
                event.getStartsAt()
                        .minusDays(5)
                        .toInstant();

        PriceQuote quote =
                pricingPolicy.calculate(event, 3, bookingTime);

        assertEquals(
                0,
                quote.bookingFee()
                        .compareTo(new BigDecimal("1.50"))
        );

        assertEquals(
                0,
                quote.finalTotal()
                        .compareTo(new BigDecimal("61.50"))
        );
    }

    @Test
    void shouldRejectNullEvent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pricingPolicy.calculate(
                        null,
                        1,
                        Instant.now()
                )
        );
    }

    @Test
    void shouldRejectNullBookingTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pricingPolicy.calculate(
                        event,
                        1,
                        null
                )
        );
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pricingPolicy.calculate(
                        event,
                        0,
                        Instant.now()
                )
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pricingPolicy.calculate(
                        event,
                        -1,
                        Instant.now()
                )
        );
    }
}