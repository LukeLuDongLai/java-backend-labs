package com.lukeludonglai.eventflow.repository.jdbc;

import com.lukeludonglai.eventflow.database.JdbcConnectionFactory;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import com.lukeludonglai.eventflow.domain.EventStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JdbcEventRepositoryTest {
    private JdbcConnectionFactory connectionFactory;
    private JdbcEventRepository repository;

    private final List<UUID> createdEventIds =
            new ArrayList<>();

    @BeforeEach
    void setUp() {
        connectionFactory = new JdbcConnectionFactory(
                System.getenv("DB_URL"),
                System.getenv("DB_USER"),
                System.getenv("DB_PASSWORD")
        );

        repository = new JdbcEventRepository(connectionFactory);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        String sql = """
            DELETE FROM events
            WHERE id = ?
            """;

        try (Connection connection =
                     connectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (UUID id : createdEventIds) {
                statement.setObject(1, id);
                statement.addBatch();
            }

            statement.executeBatch();
        }

        createdEventIds.clear();
    }

    @Test
    void shouldSaveAndFindEventById() {
        Event event = createEvent(
                "JDBC Test Event",
                EventCategory.TECH
        );

        createdEventIds.add(event.getId());

        repository.save(event);

        Event result = repository.findById(event.getId())
                .orElseThrow();

        assertAll(
                () -> assertEquals(event.getId(), result.getId()),
                () -> assertEquals(event.getTitle(), result.getTitle()),
                () -> assertEquals(event.getCategory(), result.getCategory()),
                () -> assertEquals(event.getStatus(), result.getStatus()),
                () -> assertEquals(event.getCapacity(), result.getCapacity()),
                () -> assertEquals(
                        event.getAvailableTickets(),
                        result.getAvailableTickets()
                ),
                () -> assertEquals(
                        0,
                        event.getUnitPrice()
                                .compareTo(result.getUnitPrice())
                )
        );
    }

    @Test
    void shouldUpdateExistingEvent() {
        Event event = createEvent(
                "JDBC Update Test",
                EventCategory.TECH
        );
        createdEventIds.add(event.getId());

        repository.save(event);

        event.publish();
        event.reserveTickets(3);

        repository.save(event);

        Event updated = repository.findById(event.getId())
                .orElseThrow();

        assertAll(
                () -> assertEquals(
                        EventStatus.PUBLISHED,
                        updated.getStatus()
                ),
                () -> assertEquals(
                        47,
                        updated.getAvailableTickets()
                )
        );
    }

    @Test
    void shouldReturnSavedEventsFromFindAll() {
        Event firstEvent = createEvent(
                "First JDBC Event",
                EventCategory.TECH
        );

        Event secondEvent = createEvent(
                "Second JDBC Event",
                EventCategory.MUSIC
        );

        repository.save(firstEvent);
        repository.save(secondEvent);

        createdEventIds.add(firstEvent.getId());
        createdEventIds.add(secondEvent.getId());

        List<Event> events = repository.findAll();

        assertTrue(
                events.stream()
                        .anyMatch(event ->
                                event.getId().equals(firstEvent.getId())
                        )
        );

        assertTrue(
                events.stream()
                        .anyMatch(event ->
                                event.getId().equals(secondEvent.getId())
                        )
        );
    }

    private Event createEvent(
            String title,
            EventCategory category
    ) {
        return new Event(
                title,
                category,
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
                50
        );
    }
}