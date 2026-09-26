package com.lukeludonglai.eventflow.helper;

import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TestDataHelper {

    public static Event createDefaultEvent() {
        return new Event(
                "Test Event",
                EventCategory.TECH,
                ZonedDateTime.now().plusDays(30),
                new BigDecimal("20.00"),
                50
        );
    }

    public static Event createPublishedEvent(int capacity) {
        Event event = new Event(
                "Test Event",
                EventCategory.TECH,
                ZonedDateTime.of(
                        2026,
                        12,
                        20,
                        18,
                        0,
                        0,
                        0,
                        ZoneId.of("Europe/Madrid")
                ),
                new BigDecimal("20.00"),
                capacity
        );

        event.publish();

        return event;
    }

    public static Event createEvent(
            String title,
            EventCategory category,
            ZonedDateTime startsAt,
            String price
    ) {
        return new Event(
                title,
                category,
                startsAt,
                new BigDecimal(price),
                100
        );
    }

    public static ZonedDateTime date(
            int year,
            int month,
            int day
    ) {
        return ZonedDateTime.of(
                year,
                month,
                day,
                18,
                0,
                0,
                0,
                ZoneId.of("Europe/Madrid")
        );
    }
}
