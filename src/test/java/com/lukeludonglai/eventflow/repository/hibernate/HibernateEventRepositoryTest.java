package com.lukeludonglai.eventflow.repository.hibernate;

import com.lukeludonglai.eventflow.database.JpaEntityManagerFactory;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import com.lukeludonglai.eventflow.domain.EventStatus;
import com.lukeludonglai.eventflow.helper.TestDataHelper;
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
        Event event = TestDataHelper.createDefaultEvent();

        repository.save(event);

        Event storedEvent = repository
                .findById(event.getId())
                .orElseThrow();

        assertEquals(
                event.getId(),
                storedEvent.getId()
        );

        assertEquals(
                "Test Event",
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
}