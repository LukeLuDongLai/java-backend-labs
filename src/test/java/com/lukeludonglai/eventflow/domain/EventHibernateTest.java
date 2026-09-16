package com.lukeludonglai.eventflow.domain;

import com.lukeludonglai.eventflow.database.JpaEntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventHibernateTest {
    private static EntityManagerFactory emf;

    @BeforeAll
    static void setUp() {
        emf = JpaEntityManagerFactory.create();
    }

    @AfterAll
    static void tearDown() {
        emf.close();
    }

    @Test
    void shouldPersistAndLoadEvent() {
        Event event = new Event(
                "Barcelona Java Meetup",
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

        UUID eventId = event.getId();

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(event);
            tx.commit();

            // Remove managed instance from persistence context
            // so the next find really loads it again.
            em.clear();

            Event storedEvent =
                    em.find(Event.class, eventId);

            assertNotNull(storedEvent);
            assertEquals(eventId, storedEvent.getId());
            assertEquals(
                    "Barcelona Java Meetup",
                    storedEvent.getTitle()
            );
            assertEquals(
                    EventCategory.TECH,
                    storedEvent.getCategory()
            );
            assertEquals(
                    50,
                    storedEvent.getAvailableTickets()
            );
            assertEquals(
                    0,
                    new BigDecimal("20.00")
                            .compareTo(storedEvent.getUnitPrice())
            );

        } finally {
            em.close();
        }
    }
}