package com.lukeludonglai.eventflow.service;

import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import com.lukeludonglai.eventflow.repository.EventRepository;
import com.lukeludonglai.eventflow.repository.inmemory.InMemoryEventRepository;
import com.lukeludonglai.eventflow.search.EventSearchCriteria;
import com.lukeludonglai.eventflow.search.EventSort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventSearchServiceTest {

    private EventRepository eventRepository;
    private EventSearchService searchService;

    private Event javaMeetup;
    private Event springConference;
    private Event musicFestival;

    @BeforeEach
    void setUp() {
        eventRepository = new InMemoryEventRepository();
        searchService = new EventSearchService(eventRepository);

        javaMeetup = createEvent(
                "Barcelona Java Meetup",
                EventCategory.TECH,
                date(2026, 9, 10),
                "20.00"
        );

        springConference = createEvent(
                "Spring Barcelona Conference",
                EventCategory.TECH,
                date(2026, 9, 20),
                "35.00"
        );

        musicFestival = createEvent(
                "Barcelona Music Festival",
                EventCategory.MUSIC,
                date(2026, 9, 15),
                "50.00"
        );
        eventRepository.save(javaMeetup);
        eventRepository.save(springConference);
        eventRepository.save(musicFestival);

        javaMeetup.publish();
        springConference.publish();
        musicFestival.publish();
    }

    @Test
    void shouldReturnPublishedEvents() {
        List<Event> result = searchService.search(
                emptyCriteria()
        );

        assertEquals(3, result.size());
    }

    @Test
    void shouldExcludeDraftEvents() {
        Event draftEvent = createEvent(
                "Draft Java Event",
                EventCategory.TECH,
                date(2026, 9, 25),
                "10.00"
        );

        eventRepository.save(draftEvent);

        List<Event> result = searchService.search(
                emptyCriteria()
        );

        assertFalse(result.contains(draftEvent));
    }

    @Test
    void shouldSearchTitleByPartialKeywordIgnoringCase() {
        EventSearchCriteria criteria =
                new EventSearchCriteria(
                        "JAVA",
                        null,
                        null,
                        null,
                        null,
                        EventSort.DATE_ASC
                );

        List<Event> result = searchService.search(criteria);

        assertEquals(1, result.size());
        assertTrue(result.contains(javaMeetup));
    }

    @Test
    void shouldFilterByCategory() {
        EventSearchCriteria criteria =
                new EventSearchCriteria(
                        null,
                        EventCategory.TECH,
                        null,
                        null,
                        null,
                        EventSort.DATE_ASC
                );

        List<Event> result = searchService.search(criteria);

        assertEquals(2, result.size());
        assertTrue(result.contains(javaMeetup));
        assertTrue(result.contains(springConference));
        assertFalse(result.contains(musicFestival));
    }

    @Test
    void shouldIncludeEventOnFromBoundary() {
        EventSearchCriteria criteria =
                new EventSearchCriteria(
                        null,
                        null,
                        javaMeetup.getStartsAt(),
                        null,
                        null,
                        EventSort.DATE_ASC
                );

        List<Event> result = searchService.search(criteria);

        assertTrue(result.contains(javaMeetup));
    }

    @Test
    void shouldExcludeEventBeforeFromBoundary() {
        EventSearchCriteria criteria =
                new EventSearchCriteria(
                        null,
                        null,
                        date(2026, 9, 15),
                        null,
                        null,
                        EventSort.DATE_ASC
                );

        List<Event> result = searchService.search(criteria);

        assertFalse(result.contains(javaMeetup));
        assertTrue(result.contains(musicFestival));
        assertTrue(result.contains(springConference));
    }

    @Test
    void shouldIncludeEventOnToBoundary() {
        EventSearchCriteria criteria =
                new EventSearchCriteria(
                        null,
                        null,
                        null,
                        javaMeetup.getStartsAt(),
                        null,
                        EventSort.DATE_ASC
                );

        List<Event> result = searchService.search(criteria);

        assertTrue(result.contains(javaMeetup));
    }

    @Test
    void shouldFilterEventsAboveMaximumPrice() {
        EventSearchCriteria criteria =
                new EventSearchCriteria(
                        null,
                        null,
                        null,
                        null,
                        new BigDecimal("35.00"),
                        EventSort.DATE_ASC
                );

        List<Event> result = searchService.search(criteria);

        assertEquals(2, result.size());

        assertTrue(result.contains(javaMeetup));

        // Exactly €35 must still be included.
        assertTrue(result.contains(springConference));

        assertFalse(result.contains(musicFestival));
    }

    @Test
    void shouldSortEventsByDateAscending() {
        EventSearchCriteria criteria =
                new EventSearchCriteria(
                        null,
                        null,
                        null,
                        null,
                        null,
                        EventSort.DATE_ASC
                );

        List<Event> result = searchService.search(criteria);

        assertEquals(javaMeetup, result.get(0));
        assertEquals(musicFestival, result.get(1));
        assertEquals(springConference, result.get(2));
    }

    @Test
    void shouldSortEventsByDateDescending() {
        EventSearchCriteria criteria =
                new EventSearchCriteria(
                        null,
                        null,
                        null,
                        null,
                        null,
                        EventSort.DATE_DESC
                );

        List<Event> result = searchService.search(criteria);

        assertEquals(springConference, result.get(0));
        assertEquals(musicFestival, result.get(1));
        assertEquals(javaMeetup, result.get(2));
    }

    @Test
    void shouldSortEventsByPriceAscending() {
        EventSearchCriteria criteria =
                new EventSearchCriteria(
                        null,
                        null,
                        null,
                        null,
                        null,
                        EventSort.PRICE_ASC
                );

        List<Event> result = searchService.search(criteria);

        assertEquals(javaMeetup, result.get(0));
        assertEquals(springConference, result.get(1));
        assertEquals(musicFestival, result.get(2));
    }

    @Test
    void shouldSortEventsByPriceDescending() {
        EventSearchCriteria criteria =
                new EventSearchCriteria(
                        null,
                        null,
                        null,
                        null,
                        null,
                        EventSort.PRICE_DESC
                );

        List<Event> result = searchService.search(criteria);

        assertEquals(musicFestival, result.get(0));
        assertEquals(springConference, result.get(1));
        assertEquals(javaMeetup, result.get(2));
    }

    @Test
    void shouldCombineMultipleSearchFilters() {
        EventSearchCriteria criteria =
                new EventSearchCriteria(
                        "java",
                        EventCategory.TECH,
                        null,
                        null,
                        new BigDecimal("30.00"),
                        EventSort.PRICE_ASC
                );

        List<Event> result = searchService.search(criteria);

        assertEquals(1, result.size());
        assertEquals(javaMeetup, result.getFirst());
    }

    @Test
    void shouldReturnEmptyListWhenNothingMatches() {
        EventSearchCriteria criteria =
                new EventSearchCriteria(
                        "python",
                        null,
                        null,
                        null,
                        null,
                        EventSort.DATE_ASC
                );

        List<Event> result = searchService.search(criteria);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectNullCriteria() {
        assertThrows(
                IllegalArgumentException.class,
                () -> searchService.search(null)
        );
    }

    private EventSearchCriteria emptyCriteria() {
        return new EventSearchCriteria(
                null,
                null,
                null,
                null,
                null,
                EventSort.DATE_ASC
        );
    }

    private Event createEvent(
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

    private ZonedDateTime date(
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