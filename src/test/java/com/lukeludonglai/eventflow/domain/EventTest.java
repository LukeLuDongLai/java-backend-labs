package com.lukeludonglai.eventflow.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {
    @Test
    void shouldCreateEventWithValidData(){
        Event event = new Event(
                "Barcelona Java Meetup",
                EventCategory.TECH,
                ZonedDateTime.of(
                        2026,9,20,
                        15,30,0,0,
                        ZoneId.of("Europe/Madrid")
                ),
                new BigDecimal("5.00"),
                100
        );
        assertEquals("Barcelona Java Meetup", event.getTitle());
        assertEquals(EventCategory.TECH, event.getCategory());
        assertEquals(new BigDecimal("5.00"), event.getUnitPrice());
        assertEquals(100, event.getCapacity());
        assertEquals(100,event.getAvailableTickets());
        assertEquals(EventStatus.DRAFT, event.getStatus());
        assertNotNull(event.getId());
    }

    @Test
    void shouldRejectBlankTitle(){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Event(
                        "   ",
                        EventCategory.TECH,
                        ZonedDateTime.now(),
                        new BigDecimal("20.00"),
                        50
                )
        );
    }

    @Test
    void shouldStripWhitespaceFromTitle(){
        Event event = new Event(
                "   Barcelona Java Meetup   ",
                EventCategory.TECH,
                ZonedDateTime.now(),
                new BigDecimal("20.00"),
                50
        );
        assertEquals("Barcelona Java Meetup", event.getTitle());
    }

    @Test
    void shouldRejectNegativePrice(){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Event(
                        "Barcelona Java Meetup",
                        EventCategory.TECH,
                        ZonedDateTime.now(),
                        new BigDecimal("-2.00"),
                        50
                )
        );
    }

    @Test
    void shouldRejectNegativeCapacity(){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Event(
                        "Barcelona Java Meetup",
                        EventCategory.TECH,
                        ZonedDateTime.now(),
                        new BigDecimal("2.00"),
                        -50
                )
        );
    }

    @Test
    void shouldRejectNullCategory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Event(
                        "Barcelona Java Meetup",
                        null,
                        ZonedDateTime.of(
                                2026, 9, 20,
                                18, 0, 0, 0,
                                ZoneId.of("Europe/Madrid")
                        ),
                        new BigDecimal("20.00"),
                        50
                )
        );
    }

    @Test
    void shouldRejectNullStartTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Event(
                        "Barcelona Java Meetup",
                        EventCategory.TECH,
                        null,
                        new BigDecimal("20.00"),
                        50
                )
        );
    }
}
