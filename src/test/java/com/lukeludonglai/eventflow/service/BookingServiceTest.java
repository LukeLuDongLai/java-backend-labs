package com.lukeludonglai.eventflow.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.lukeludonglai.eventflow.domain.*;
import com.lukeludonglai.eventflow.exception.*;
import com.lukeludonglai.eventflow.pricing.*;
import com.lukeludonglai.eventflow.repository.*;
import com.lukeludonglai.eventflow.repository.inmemory.*;

class BookingServiceTest {
    private static final Instant NOW =
            Instant.parse("2026-09-01T10:00:00Z");

    private static final BigDecimal FIXED_TOTAL =
            new BigDecimal("42.50");

    private EventRepository eventRepository;
    private BookingRepository bookingRepository;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        eventRepository = new InMemoryEventRepository();
        bookingRepository = new InMemoryBookingRepository();

        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        PricingPolicy pricingPolicy =
                new FixedPricingPolicy(FIXED_TOTAL);

        bookingService = new BookingService(
                eventRepository,
                bookingRepository,
                pricingPolicy,
                clock
        );
    }

    @Test
    void shouldCreateBookingForPublishedFutureEvent() {
        Event event = createPublishedFutureEvent(50);
        eventRepository.save(event);

        Booking booking = bookingService.createBooking(
                event.getId(),
                "customer@example.com",
                3
        );

        assertAll(
                () -> assertNotNull(booking),
                () -> assertNotNull(booking.getId()),
                () -> assertEquals(
                        event.getId(),
                        booking.getEventId()
                ),
                () -> assertEquals(
                        "customer@example.com",
                        booking.getCustomerEmail()
                ),
                () -> assertEquals(
                        3,
                        booking.getQuantity()
                ),
                () -> assertEquals(
                        BookingStatus.CONFIRMED,
                        booking.getStatus()
                ),
                () -> assertEquals(
                        0,
                        booking.getTotalPrice()
                                .compareTo(FIXED_TOTAL),
                        "Booking total must come from PricingPolicy"
                )
        );
    }

    @Test
    void shouldReduceEventInventoryAfterSuccessfulBooking() {
        Event event = createPublishedFutureEvent(50);
        eventRepository.save(event);

        bookingService.createBooking(
                event.getId(),
                "customer@example.com",
                3
        );

        assertEquals(
                47,
                event.getAvailableTickets()
        );
    }

    @Test
    void shouldStoreCreatedBookingInRepository() {
        Event event = createPublishedFutureEvent(50);
        eventRepository.save(event);

        Booking booking = bookingService.createBooking(
                event.getId(),
                "customer@example.com",
                3
        );

        Booking storedBooking = bookingRepository
                .findById(booking.getId())
                .orElseThrow();

        assertSame(booking, storedBooking);
    }

    @Test
    void shouldPersistUpdatedEventAfterBooking() {
        Event event = createPublishedFutureEvent(50);
        eventRepository.save(event);

        bookingService.createBooking(
                event.getId(),
                "customer@example.com",
                3
        );

        Event storedEvent = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertEquals(
                47,
                storedEvent.getAvailableTickets()
        );
    }

    @Test
    void shouldRejectBookingWhenEventDoesNotExist() {
        UUID unknownEventId = UUID.randomUUID();

        assertThrows(
                EventNotFoundException.class,
                () -> bookingService.createBooking(
                        unknownEventId,
                        "customer@example.com",
                        1
                )
        );
    }

    @Test
    void shouldRejectBookingWhenEventIsDraft() {
        Event event = createFutureEvent(50);

        // event.publish()
        eventRepository.save(event);

        assertThrows(
                EventNotBookableException.class,
                () -> bookingService.createBooking(
                        event.getId(),
                        "customer@example.com",
                        1
                )
        );
    }

    @Test
    void shouldRejectBookingWhenEventHasAlreadyStarted() {
        Event event = createPastEvent(50);
        event.publish();
        eventRepository.save(event);

        assertThrows(
                EventNotBookableException.class,
                () -> bookingService.createBooking(
                        event.getId(),
                        "customer@example.com",
                        1
                )
        );
    }

    @Test
    void shouldRejectZeroQuantity() {
        Event event = createPublishedFutureEvent(50);
        eventRepository.save(event);

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(
                        event.getId(),
                        "customer@example.com",
                        0
                )
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {
        Event event = createPublishedFutureEvent(50);
        eventRepository.save(event);

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(
                        event.getId(),
                        "customer@example.com",
                        -1
                )
        );
    }

    @Test
    void shouldRejectQuantityGreaterThanTen() {
        Event event = createPublishedFutureEvent(50);
        eventRepository.save(event);

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(
                        event.getId(),
                        "customer@example.com",
                        11
                )
        );
    }

    @Test
    void shouldRejectBookingWhenInventoryIsInsufficient() {
        Event event = createPublishedFutureEvent(3);
        eventRepository.save(event);

        assertThrows(
                InsufficientTicketsException.class,
                () -> bookingService.createBooking(
                        event.getId(),
                        "customer@example.com",
                        5
                )
        );

        assertEquals(
                3,
                event.getAvailableTickets(),
                "Inventory must remain unchanged when booking fails"
        );
    }

    private Event createPublishedFutureEvent(int capacity) {
        Event event = createFutureEvent(capacity);
        event.publish();
        return event;
    }

    private Event createFutureEvent(int capacity) {
        return new Event(
                "Barcelona Java Meetup",
                EventCategory.TECH,
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
                capacity
        );
    }

    private Event createPastEvent(int capacity) {
        return new Event(
                "Past Java Meetup",
                EventCategory.TECH,
                ZonedDateTime.of(
                        2026,
                        8,
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
    }

    @Test
    void shouldCancelExistingBooking() {
        Event event = createPublishedFutureEvent(50);
        eventRepository.save(event);

        Booking booking = bookingService.createBooking(
                event.getId(),
                "customer@example.com",
                3
        );

        Booking cancelledBooking =
                bookingService.cancelBooking(booking.getId());

        assertEquals(
                BookingStatus.CANCELLED,
                cancelledBooking.getStatus()
        );
    }

    @Test
    void shouldRestoreEventInventoryAfterCancellation() {
        Event event = createPublishedFutureEvent(50);
        eventRepository.save(event);

        Booking booking = bookingService.createBooking(
                event.getId(),
                "customer@example.com",
                3
        );

        assertEquals(47, event.getAvailableTickets());

        bookingService.cancelBooking(booking.getId());

        assertEquals(
                50,
                event.getAvailableTickets(),
                "Cancelled booking should restore reserved tickets"
        );
    }

    @Test
    void shouldRejectCancellationWhenBookingDoesNotExist() {
        UUID unknownBookingId = UUID.randomUUID();

        assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.cancelBooking(unknownBookingId)
        );
    }

    @Test
    void shouldRejectCancellingSameBookingTwice() {
        Event event = createPublishedFutureEvent(50);
        eventRepository.save(event);

        Booking booking = bookingService.createBooking(
                event.getId(),
                "customer@example.com",
                3
        );

        bookingService.cancelBooking(booking.getId());

        assertThrows(
                BookingAlreadyCancelledException.class,
                () -> bookingService.cancelBooking(booking.getId())
        );
    }

    @Test
    void shouldPersistCancelledBooking() {
        Event event = createPublishedFutureEvent(50);
        eventRepository.save(event);

        Booking booking = bookingService.createBooking(
                event.getId(),
                "customer@example.com",
                3
        );

        bookingService.cancelBooking(booking.getId());

        Booking storedBooking = bookingRepository
                .findById(booking.getId())
                .orElseThrow();

        assertEquals(
                BookingStatus.CANCELLED,
                storedBooking.getStatus()
        );
    }

    @Test
    void shouldPersistRestoredEventInventoryAfterCancellation() {
        Event event = createPublishedFutureEvent(50);
        eventRepository.save(event);

        Booking booking = bookingService.createBooking(
                event.getId(),
                "customer@example.com",
                3
        );

        bookingService.cancelBooking(booking.getId());

        Event storedEvent = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertEquals(
                50,
                storedEvent.getAvailableTickets()
        );
    }

    @Test
    void shouldRejectCancellationWhenRelatedEventDoesNotExist() {
        UUID missingEventId = UUID.randomUUID();

        Booking booking = new Booking(
                missingEventId,
                "customer@example.com",
                3,
                new BigDecimal("42.50")
        );

        bookingRepository.save(booking);

        assertThrows(
                EventNotFoundException.class,
                () -> bookingService.cancelBooking(booking.getId())
        );
    }

    private static class FixedPricingPolicy implements PricingPolicy {

        private final BigDecimal finalTotal;

        private FixedPricingPolicy(BigDecimal finalTotal) {
            this.finalTotal = finalTotal;
        }

        @Override
        public PriceQuote calculate(
                Event event,
                int quantity,
                Instant bookingTime
        ) {
            BigDecimal unitPrice = event.getUnitPrice();

            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(quantity)
            );

            BigDecimal discountRate = BigDecimal.ZERO;
            BigDecimal discountAmount = BigDecimal.ZERO;
            BigDecimal bookingFee = BigDecimal.ZERO;

            return new PriceQuote(
                    unitPrice,
                    quantity,
                    subtotal,
                    discountRate,
                    discountAmount,
                    bookingFee,
                    finalTotal
            );
        }
    }
}