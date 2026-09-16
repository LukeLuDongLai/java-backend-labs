package com.lukeludonglai.eventflow.repository.hibernate;

import com.lukeludonglai.eventflow.database.JpaEntityManagerFactory;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import com.lukeludonglai.eventflow.domain.EventStatus;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;


class HibernateEventRepositoryTest {
    private static EntityManagerFactory emf;
    private static HibernateEventRepository repository;

    @BeforeAll
    static void setUp() {
        emf = JpaEntityManagerFactory.create();
        repository =
                new HibernateEventRepository(emf);
    }

    @AfterAll
    static void tearDown() {
        emf.close();
    }

    @Test
    void shouldSaveAndFindEventById() {
        Event event = createEvent();

        repository.save(event);

        Event storedEvent = repository
                .findById(event.getId())
                .orElseThrow();

        assertEquals(
                event.getId(),
                storedEvent.getId()
        );

        assertEquals(
                "Hibernate Meetup",
                storedEvent.getTitle()
        );

        assertEquals(
                EventStatus.DRAFT,
                storedEvent.getStatus()
        );

        assertEquals(
                50,
                storedEvent.getAvailableTickets()
        );
    }

    private Event createEvent() {
        return new Event(
                "Hibernate Meetup",
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
                50
        );
    }
}