package com.lukeludonglai.eventflow.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {
    @Test
    void shouldCreateBookingWithValidData() {
        UUID eventId = UUID.randomUUID();

        Booking booking = new Booking(
                eventId,
                "customer@example.com",
                3,
                new BigDecimal("60.00")
        );

        assertNotNull(booking.getId());
        assertEquals(eventId, booking.getEventId());
        assertEquals("customer@example.com", booking.getCustomerEmail());
        assertEquals(3, booking.getQuantity());
        assertEquals(new BigDecimal("60.00"), booking.getTotalPrice());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertNotNull(booking.getCreatedAt());
    }

    @Test
    void shouldRejectNullEventId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Booking(
                        null,
                        "customer@example.com",
                        3,
                        new BigDecimal("60.00")
                )
        );
    }

    @Test
    void shouldRejectNullEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Booking(
                        UUID.randomUUID(),
                        null,
                        3,
                        new BigDecimal("60.00")
                )
        );
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Booking(
                        UUID.randomUUID(),
                        "   ",
                        3,
                        new BigDecimal("60.00")
                )
        );
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Booking(
                        UUID.randomUUID(),
                        "customer@example.com",
                        0,
                        new BigDecimal("60.00")
                )
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Booking(
                        UUID.randomUUID(),
                        "customer@example.com",
                        -1,
                        new BigDecimal("60.00")
                )
        );
    }

    @Test
    void shouldRejectNullTotalPrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Booking(
                        UUID.randomUUID(),
                        "customer@example.com",
                        3,
                        null
                )
        );
    }

    @Test
    void shouldRejectNegativeTotalPrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Booking(
                        UUID.randomUUID(),
                        "customer@example.com",
                        3,
                        new BigDecimal("-0.01")
                )
        );
    }

    @Test
    void shouldAllowZeroTotalPrice() {
        Booking booking = new Booking(
                UUID.randomUUID(),
                "customer@example.com",
                1,
                BigDecimal.ZERO
        );

        assertEquals(BigDecimal.ZERO, booking.getTotalPrice());
    }

    @Test
    void shouldStripWhitespaceFromEmail() {
        Booking booking = new Booking(
                UUID.randomUUID(),
                "   customer@example.com   ",
                3,
                new BigDecimal("60.00")
        );

        assertEquals(
                "customer@example.com",
                booking.getCustomerEmail()
        );
    }
}