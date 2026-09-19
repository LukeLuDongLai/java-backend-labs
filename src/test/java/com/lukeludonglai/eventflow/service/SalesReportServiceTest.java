package com.lukeludonglai.eventflow.service;

import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import com.lukeludonglai.eventflow.exception.EventNotFoundException;
import com.lukeludonglai.eventflow.helper.TestDataHelper;
import com.lukeludonglai.eventflow.report.EventSalesSummary;
import com.lukeludonglai.eventflow.repository.BookingRepository;
import com.lukeludonglai.eventflow.repository.EventRepository;
import com.lukeludonglai.eventflow.repository.inmemory.InMemoryBookingRepository;
import com.lukeludonglai.eventflow.repository.inmemory.InMemoryEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SalesReportServiceTest {
    private BookingRepository bookingRepository;
    private EventRepository eventRepository;
    private SalesReportService salesReportService;

    @BeforeEach
    void setUp() {
        bookingRepository = new InMemoryBookingRepository();
        eventRepository = new InMemoryEventRepository();

        salesReportService = new SalesReportService(
                bookingRepository,
                eventRepository
        );
    }

    @Nested
    class TotalTicketsSold {

        @Test
        void shouldReturnZeroWhenNoBookingsExist() {
            int result = salesReportService.getTotalTicketsSold();

            assertEquals(0, result);
        }

        @Test
        void shouldSumConfirmedBookingQuantities() {
            Event event1 = createEvent("Test event 1", EventCategory.HIKING);
            Event event2 = createEvent("Test event 2", EventCategory.MUSIC);

            eventRepository.save(event1);
            eventRepository.save(event2);


            Booking firstBooking = createBooking(
                    event1,
                    2,
                    "40.00"
            );

            Booking secondBooking = createBooking(
                    event2,
                    5,
                    "100.00"
            );

            bookingRepository.save(firstBooking);
            bookingRepository.save(secondBooking);

            int result = salesReportService.getTotalTicketsSold();

            assertEquals(7, result);
        }

        @Test
        void shouldExcludeCancelledBookings() {
            Event event = TestDataHelper.createDefaultEvent();
            eventRepository.save(event);

            Booking confirmedBooking = createBooking(
                    event,
                    3,
                    "60.00"
            );

            Booking cancelledBooking = createBooking(
                    event,
                    4,
                    "80.00"
            );

            cancelledBooking.cancel();

            bookingRepository.save(confirmedBooking);
            bookingRepository.save(cancelledBooking);

            int result = salesReportService.getTotalTicketsSold();

            assertEquals(3, result);
        }

        @Test
        void shouldReturnZeroWhenAllBookingsAreCancelled() {
            Event event = TestDataHelper.createDefaultEvent();
            eventRepository.save(event);

            Booking firstBooking = createBooking(
                    event,
                    2,
                    "40.00"
            );

            Booking secondBooking = createBooking(
                    event,
                    5,
                    "100.00"
            );

            firstBooking.cancel();
            secondBooking.cancel();

            bookingRepository.save(firstBooking);
            bookingRepository.save(secondBooking);

            int result = salesReportService.getTotalTicketsSold();

            assertEquals(0, result);
        }
    }

    @Nested
    class RevenueByCategory {

        @Test
        void shouldReturnEmptyMapWhenNoBookingsExist() {
            Map<EventCategory, BigDecimal> result =
                    salesReportService.getRevenueByCategory();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldCalculateRevenueForSingleCategory() {
            Event techEvent = createEvent(
                    "Barcelona Java Meetup",
                    EventCategory.TECH
            );

            eventRepository.save(techEvent);

            bookingRepository.save(
                    createBooking(
                            techEvent,
                            2,
                            "50.00"
                    )
            );

            Map<EventCategory, BigDecimal> result =
                    salesReportService.getRevenueByCategory();

            assertEquals(1, result.size());

            assertBigDecimalEquals(
                    "50.00",
                    result.get(EventCategory.TECH)
            );
        }

        @Test
        void shouldSumRevenueForMultipleBookingsInSameCategory() {
            Event javaEvent = createEvent(
                    "Barcelona Java Meetup",
                    EventCategory.TECH
            );

            Event springEvent = createEvent(
                    "Spring Conference",
                    EventCategory.TECH
            );

            eventRepository.save(javaEvent);
            eventRepository.save(springEvent);

            bookingRepository.save(
                    createBooking(
                            javaEvent,
                            2,
                            "50.00"
                    )
            );

            bookingRepository.save(
                    createBooking(
                            springEvent,
                            3,
                            "75.00"
                    )
            );

            Map<EventCategory, BigDecimal> result =
                    salesReportService.getRevenueByCategory();

            assertEquals(1, result.size());

            assertBigDecimalEquals(
                    "125.00",
                    result.get(EventCategory.TECH)
            );
        }

        @Test
        void shouldGroupRevenueByDifferentCategories() {
            Event techEvent = createEvent(
                    "Barcelona Java Meetup",
                    EventCategory.TECH
            );

            Event musicEvent = createEvent(
                    "Barcelona Music Festival",
                    EventCategory.MUSIC
            );

            eventRepository.save(techEvent);
            eventRepository.save(musicEvent);

            bookingRepository.save(
                    createBooking(
                            techEvent,
                            2,
                            "60.00"
                    )
            );

            bookingRepository.save(
                    createBooking(
                            musicEvent,
                            3,
                            "120.00"
                    )
            );

            Map<EventCategory, BigDecimal> result =
                    salesReportService.getRevenueByCategory();

            assertEquals(2, result.size());

            assertBigDecimalEquals(
                    "60.00",
                    result.get(EventCategory.TECH)
            );

            assertBigDecimalEquals(
                    "120.00",
                    result.get(EventCategory.MUSIC)
            );
        }

        @Test
        void shouldExcludeCancelledBookingsFromRevenue() {
            Event event = createEvent(
                    "Barcelona Java Meetup",
                    EventCategory.TECH
            );

            eventRepository.save(event);

            Booking confirmedBooking = createBooking(
                    event,
                    2,
                    "50.00"
            );

            Booking cancelledBooking = createBooking(
                    event,
                    3,
                    "90.00"
            );

            cancelledBooking.cancel();

            bookingRepository.save(confirmedBooking);
            bookingRepository.save(cancelledBooking);

            Map<EventCategory, BigDecimal> result =
                    salesReportService.getRevenueByCategory();

            assertEquals(1, result.size());

            assertBigDecimalEquals(
                    "50.00",
                    result.get(EventCategory.TECH)
            );
        }

        @Test
        void shouldReturnEmptyMapWhenAllBookingsAreCancelled() {
            Event event = createEvent(
                    "Barcelona Java Meetup",
                    EventCategory.TECH
            );

            eventRepository.save(event);

            Booking firstBooking = createBooking(
                    event,
                    2,
                    "50.00"
            );

            Booking secondBooking = createBooking(
                    event,
                    3,
                    "75.00"
            );

            firstBooking.cancel();
            secondBooking.cancel();

            bookingRepository.save(firstBooking);
            bookingRepository.save(secondBooking);

            Map<EventCategory, BigDecimal> result =
                    salesReportService.getRevenueByCategory();

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldThrowWhenBookingReferencesUnknownEvent() {
            Booking booking = createBooking(
                    TestDataHelper.createDefaultEvent(),
                    2,
                    "50.00"
            );

            bookingRepository.save(booking);

            assertThrows(
                    EventNotFoundException.class,
                    () -> salesReportService.getRevenueByCategory()
            );
        }
    }

    @Nested
    class TopEventsByTicketsSold {

        @Test
        void shouldReturnEmptyListWhenNoBookingsExist() {
            List<EventSalesSummary> result =
                    salesReportService.getTopEventsByTicketsSold();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldCreateSalesSummaryForSingleEvent() {
            Event event = createEvent(
                    "Barcelona Java Meetup",
                    EventCategory.TECH
            );

            eventRepository.save(event);

            bookingRepository.save(
                    createBooking(
                            event,
                            2,
                            "50.00"
                    )
            );

            bookingRepository.save(
                    createBooking(
                            event,
                            3,
                            "75.00"
                    )
            );

            List<EventSalesSummary> result =
                    salesReportService.getTopEventsByTicketsSold();

            assertEquals(1, result.size());

            EventSalesSummary summary = result.getFirst();

            assertAll(
                    () -> assertEquals(
                            event.getId(),
                            summary.eventId()
                    ),
                    () -> assertEquals(
                            "Barcelona Java Meetup",
                            summary.eventTitle()
                    ),
                    () -> assertEquals(
                            5,
                            summary.ticketsSold()
                    ),
                    () -> assertBigDecimalEquals(
                            "125.00",
                            summary.revenue()
                    )
            );
        }

        @Test
        void shouldExcludeCancelledBookingsFromEventSalesSummary() {
            Event event = createEvent(
                    "Barcelona Java Meetup",
                    EventCategory.TECH
            );

            eventRepository.save(event);

            Booking confirmedBooking = createBooking(
                    event,
                    2,
                    "50.00"
            );

            Booking cancelledBooking = createBooking(
                    event,
                    5,
                    "100.00"
            );

            cancelledBooking.cancel();

            bookingRepository.save(confirmedBooking);
            bookingRepository.save(cancelledBooking);

            List<EventSalesSummary> result =
                    salesReportService.getTopEventsByTicketsSold();

            assertEquals(1, result.size());

            EventSalesSummary summary = result.getFirst();

            assertEquals(
                    2,
                    summary.ticketsSold()
            );

            assertBigDecimalEquals(
                    "50.00",
                    summary.revenue()
            );
        }

        @Test
        void shouldSortEventsByTicketsSoldDescending() {
            Event firstEvent = createEvent(
                    "Java Meetup",
                    EventCategory.TECH
            );

            Event secondEvent = createEvent(
                    "Music Festival",
                    EventCategory.MUSIC
            );

            Event thirdEvent = createEvent(
                    "Sports Event",
                    EventCategory.SPORTS
            );

            eventRepository.save(firstEvent);
            eventRepository.save(secondEvent);
            eventRepository.save(thirdEvent);

            bookingRepository.save(
                    createBooking(
                            firstEvent,
                            3,
                            "60.00"
                    )
            );

            bookingRepository.save(
                    createBooking(
                            secondEvent,
                            8,
                            "160.00"
                    )
            );

            bookingRepository.save(
                    createBooking(
                            thirdEvent,
                            5,
                            "100.00"
                    )
            );

            List<EventSalesSummary> result =
                    salesReportService.getTopEventsByTicketsSold();

            assertEquals(3, result.size());

            assertEquals(
                    secondEvent.getId(),
                    result.get(0).eventId()
            );

            assertEquals(
                    thirdEvent.getId(),
                    result.get(1).eventId()
            );

            assertEquals(
                    firstEvent.getId(),
                    result.get(2).eventId()
            );
        }

        @Test
        void shouldReturnOnlyTopThreeEvents() {
            Event firstEvent = createEvent(
                    "Event 1",
                    EventCategory.TECH
            );

            Event secondEvent = createEvent(
                    "Event 2",
                    EventCategory.MUSIC
            );

            Event thirdEvent = createEvent(
                    "Event 3",
                    EventCategory.SPORTS
            );

            Event fourthEvent = createEvent(
                    "Event 4",
                    EventCategory.ART
            );

            eventRepository.save(firstEvent);
            eventRepository.save(secondEvent);
            eventRepository.save(thirdEvent);
            eventRepository.save(fourthEvent);

            bookingRepository.save(
                    createBooking(
                            firstEvent,
                            10,
                            "200.00"
                    )
            );

            bookingRepository.save(
                    createBooking(
                            secondEvent,
                            8,
                            "160.00"
                    )
            );

            bookingRepository.save(
                    createBooking(
                            thirdEvent,
                            6,
                            "120.00"
                    )
            );

            bookingRepository.save(
                    createBooking(
                            fourthEvent,
                            2,
                            "40.00"
                    )
            );

            List<EventSalesSummary> result =
                    salesReportService.getTopEventsByTicketsSold();

            assertEquals(3, result.size());

            assertEquals(
                    firstEvent.getId(),
                    result.get(0).eventId()
            );

            assertEquals(
                    secondEvent.getId(),
                    result.get(1).eventId()
            );

            assertEquals(
                    thirdEvent.getId(),
                    result.get(2).eventId()
            );

            assertFalse(
                    result.stream()
                            .anyMatch(summary ->
                                    summary.eventId().equals(
                                            fourthEvent.getId()
                                    )
                            )
            );
        }

        @Test
        void shouldCombineMultipleBookingsForSameEvent() {
            Event event = createEvent(
                    "Barcelona Java Meetup",
                    EventCategory.TECH
            );

            eventRepository.save(event);

            bookingRepository.save(
                    createBooking(
                            event,
                            2,
                            "40.00"
                    )
            );

            bookingRepository.save(
                    createBooking(
                            event,
                            4,
                            "80.00"
                    )
            );

            bookingRepository.save(
                    createBooking(
                            event,
                            3,
                            "60.00"
                    )
            );

            EventSalesSummary summary =
                    salesReportService
                            .getTopEventsByTicketsSold()
                            .getFirst();

            assertEquals(
                    9,
                    summary.ticketsSold()
            );

            assertBigDecimalEquals(
                    "180.00",
                    summary.revenue()
            );
        }
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
                        9,
                        20,
                        18,
                        0,
                        0,
                        0,
                        ZoneId.of("Europe/Madrid")
                ),
                new BigDecimal("20.00"),
                100
        );
    }

    private Booking createBooking(
            Event event,
            int quantity,
            String totalPrice
    ) {
        return new Booking(
                event,
                "customer@example.com",
                quantity,
                new BigDecimal(totalPrice)
        );
    }

    private void assertBigDecimalEquals(
            String expected,
            BigDecimal actual
    ) {
        assertNotNull(actual);

        assertEquals(
                0,
                actual.compareTo(
                        new BigDecimal(expected)
                )
        );
    }
}