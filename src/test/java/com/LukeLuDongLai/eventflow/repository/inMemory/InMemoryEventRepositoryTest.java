package com.LukeLuDongLai.eventflow.repository.inMemory;

import com.LukeLuDongLai.eventflow.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
class InMemoryEventRepositoryTest {
    private InMemoryEventRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEventRepository();
    }

    private Event createEvent() {
        return new Event(
                "Barcelona Java Meetup",
                EventCategory.TECH,
                ZonedDateTime.of(
                        2026, 9, 20,
                        18, 0, 0, 0,
                        ZoneId.of("Europe/Madrid")
                ),
                new BigDecimal("20.00"),
                50
        );
    }

    @Test
    void shouldFindEventById() {
        Event event = createEvent();
        repository.save(event);
        Optional<Event> result = repository.findById(event.getId());

        assertTrue(result.isPresent());
        assertEquals(event, result.get());
    }

    @Test
    void shouldReturnEmptyWhenEventDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        Optional<Event> result = repository.findById(unknownId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindAllEvents() {
        Event event1 = createEvent();
        Event event2 = new Event(
                "Montserrat Hiking",
                EventCategory.HIKING,
                ZonedDateTime.of(
                        2026, 8, 20,
                        9, 0, 0, 0,
                        ZoneId.of("Europe/Madrid")
                ),
                new BigDecimal("2.00"),
                30
        );
        repository.save(event1);
        repository.save(event2);
        List<Event> events = repository.findAll();

        assertEquals(2, events.size());
        assertTrue(events.contains(event1));
        assertTrue(events.contains(event2));
    }

    @Test
    void shouldReturnEmptyListWhenNoEventsAreSaved() {
        List<Event> events = repository.findAll();

        assertTrue(events.isEmpty());
    }

    @Test
    void shouldNotExposeInternalCollection() {
        Event event = createEvent();
        repository.save(event);

        List<Event> result = repository.findAll();

        result.clear();

        assertTrue(repository.findAll().contains(event));
    }
}