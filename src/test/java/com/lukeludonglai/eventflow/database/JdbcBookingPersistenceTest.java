package com.lukeludonglai.eventflow.database;

import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import com.lukeludonglai.eventflow.persistence.jdbc.JdbcBookingPersistence;
import com.lukeludonglai.eventflow.repository.jdbc.JdbcBookingRepository;
import com.lukeludonglai.eventflow.repository.jdbc.JdbcEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class JdbcBookingPersistenceTest {

    private JdbcConnectionFactory connectionFactory;
    private JdbcEventRepository eventRepository;
    private JdbcBookingRepository bookingRepository;
    private JdbcBookingPersistence persistence;

    @BeforeEach
    void setUp() {
        connectionFactory = new JdbcConnectionFactory(
                System.getenv("DB_URL"),
                System.getenv("DB_USER"),
                System.getenv("DB_PASSWORD")
        );

        eventRepository =
                new JdbcEventRepository(connectionFactory);

        bookingRepository =
                new JdbcBookingRepository(connectionFactory);

        persistence =
                new JdbcBookingPersistence(connectionFactory);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        try (Connection connection =
                     connectionFactory.getConnection();
             Statement statement =
                     connection.createStatement()) {

            statement.executeUpdate(
                    "DELETE FROM bookings"
            );

            statement.executeUpdate(
                    "DELETE FROM events"
            );
        }
    }

    @Test
    void shouldPersistEventAndBookingInSameTransaction() {
        Event event = createEvent(10);
        event.publish();

        eventRepository.save(event);

        event.reserveTickets(3);

        Booking booking = new Booking(
                event.getId(),
                "customer@example.com",
                3,
                new BigDecimal("61.50")
        );

        persistence.saveCreatedBooking(
                event,
                booking
        );

        Event storedEvent = eventRepository
                .findById(event.getId())
                .orElseThrow();

        Booking storedBooking = bookingRepository
                .findById(booking.getId())
                .orElseThrow();

        assertAll(
                () -> assertEquals(
                        7,
                        storedEvent.getAvailableTickets()
                ),
                () -> assertEquals(
                        booking.getId(),
                        storedBooking.getId()
                ),
                () -> assertEquals(
                        event.getId(),
                        storedBooking.getEventId()
                )
        );
    }

    @Test
    void shouldRollbackEventUpdateWhenBookingInsertFails() {
        Event event = createEvent(10);
        event.publish();

        eventRepository.save(event);

        // Java object: 10 → 7
        event.reserveTickets(3);

        UUID missingEventId =
                UUID.randomUUID();

        Booking invalidBooking = new Booking(
                missingEventId,
                "customer@example.com",
                3,
                new BigDecimal("61.50")
        );

        assertThrows(
                RuntimeException.class,
                () -> persistence.saveCreatedBooking(
                        event,
                        invalidBooking
                )
        );

        Event storedEvent = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertEquals(
                10,
                storedEvent.getAvailableTickets(),
                "Event inventory should be rolled back"
        );

        assertTrue(
                bookingRepository
                        .findById(invalidBooking.getId())
                        .isEmpty()
        );
    }

    private Event createEvent(int capacity) {
        return new Event(
                "JDBC Transaction Test Event",
                EventCategory.TECH,
                ZonedDateTime.now(
                        ZoneId.of("Europe/Madrid")
                ).plusDays(30),
                new BigDecimal("20.00"),
                capacity
        );
    }
}
