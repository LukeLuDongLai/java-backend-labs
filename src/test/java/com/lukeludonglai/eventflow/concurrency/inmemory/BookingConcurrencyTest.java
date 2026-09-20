package com.lukeludonglai.eventflow.concurrency.inmemory;

import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.BookingStatus;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import com.lukeludonglai.eventflow.exception.BookingAlreadyCancelledException;
import com.lukeludonglai.eventflow.exception.InsufficientTicketsException;
import com.lukeludonglai.eventflow.persistence.BookingPersistence;
import com.lukeludonglai.eventflow.persistence.inMemory.InMemoryBookingPersistence;
import com.lukeludonglai.eventflow.pricing.PriceQuote;
import com.lukeludonglai.eventflow.pricing.PricingPolicy;
import com.lukeludonglai.eventflow.repository.BookingRepository;
import com.lukeludonglai.eventflow.repository.EventRepository;
import com.lukeludonglai.eventflow.repository.inmemory.InMemoryBookingRepository;
import com.lukeludonglai.eventflow.repository.inmemory.InMemoryEventRepository;
import com.lukeludonglai.eventflow.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class BookingConcurrencyTest {
    private static final Instant NOW =
            Instant.parse("2026-09-01T10:00:00Z");

    private EventRepository eventRepository;
    private BookingRepository bookingRepository;
    private BookingService bookingService;
    private BookingPersistence bookingPersistence;

    @BeforeEach
    void setUp() {
        eventRepository = new InMemoryEventRepository();
        bookingRepository = new InMemoryBookingRepository();
        bookingPersistence = new InMemoryBookingPersistence(eventRepository,bookingRepository);

        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        PricingPolicy pricingPolicy =
                new NoDiscountPricingPolicy();

        bookingService = new BookingService(
                eventRepository,
                bookingRepository,
                pricingPolicy,
                clock,
                bookingPersistence
        );
    }

    @Test
    void shouldPreventOversellingWhenManyUsersBookAtTheSameTime()
            throws Exception {

        int capacity = 20;
        int bookingAttempts = 100;

        Event event = createPublishedEvent(capacity);
        eventRepository.save(event);

        ExecutorService executor =
                Executors.newFixedThreadPool(bookingAttempts);

        CountDownLatch readyGate =
                new CountDownLatch(bookingAttempts);

        CountDownLatch startGate =
                new CountDownLatch(1);

        List<Future<Boolean>> futures =
                new ArrayList<>();

        try {
            for (int i = 0; i < bookingAttempts; i++) {

                int customerNumber = i;

                Future<Boolean> future = executor.submit(() -> {

                    readyGate.countDown();

                    // All threads wait here until the test opens the gate.
                    startGate.await();

                    try {
                        bookingService.createBooking(
                                event.getId(),
                                "customer" + customerNumber + "@example.com",
                                1
                        );

                        return true;

                    } catch (InsufficientTicketsException e) {
                        return false;
                    }
                });

                futures.add(future);
            }

            // Wait until all 100 threads are ready.
            assertTrue(
                    readyGate.await(5, TimeUnit.SECONDS),
                    "All booking threads should become ready"
            );

            // Let all threads start competing at roughly the same time.
            startGate.countDown();

            int successfulBookings = 0;
            int failedBookings = 0;

            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    successfulBookings++;
                } else {
                    failedBookings++;
                }
            }

            int successfulCount = successfulBookings;
            int failedCount = failedBookings;

            assertAll(
                    () -> assertEquals(
                            20,
                            successfulCount,
                            "Exactly 20 bookings should succeed"
                    ),
                    () -> assertEquals(
                            80,
                            failedCount,
                            "Remaining bookings should fail"
                    ),
                    () -> assertEquals(
                            0,
                            event.getAvailableTickets(),
                            "Inventory should end at zero"
                    ),
                    () -> assertEquals(
                            20,
                            bookingRepository.findAll().size(),
                            "Only successful bookings should be stored"
                    )
            );

        } finally {
            executor.shutdown();

            assertTrue(
                    executor.awaitTermination(
                            5,
                            TimeUnit.SECONDS
                    ),
                    "Executor should terminate"
            );
        }
    }

    @Test
    void shouldRestoreInventoryOnlyOnceWhenSameBookingIsCancelledConcurrently()
            throws Exception {

        Event event = createPublishedEvent(20);
        eventRepository.save(event);

        Booking booking = bookingService.createBooking(
                event.getId(),
                "customer@example.com",
                5
        );

        Event savedEvent = eventRepository.findById(event.getId()).orElseThrow();

        assertEquals(
                15,
                savedEvent.getAvailableTickets()
        );

        int cancellationAttempts = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(cancellationAttempts);

        CountDownLatch readyGate =
                new CountDownLatch(cancellationAttempts);

        CountDownLatch startGate =
                new CountDownLatch(1);

        List<Future<Boolean>> futures =
                new ArrayList<>();

        try {
            for (int i = 0; i < cancellationAttempts; i++) {

                Future<Boolean> future = executor.submit(() -> {

                    readyGate.countDown();

                    startGate.await();

                    try {
                        bookingService.cancelBooking(
                                booking.getId()
                        );

                        return true;

                    } catch (BookingAlreadyCancelledException e) {
                        return false;
                    }
                });

                futures.add(future);
            }

            assertTrue(
                    readyGate.await(5, TimeUnit.SECONDS),
                    "All cancellation threads should become ready"
            );

            startGate.countDown();

            int successfulCancellations = 0;
            int rejectedCancellations = 0;

            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    successfulCancellations++;
                } else {
                    rejectedCancellations++;
                }
            }

            int successfulCount = successfulCancellations;
            int failedCount = rejectedCancellations;

            assertAll(
                    () -> assertEquals(
                            1,
                            successfulCount,
                            "Only one cancellation should succeed"
                    ),
                    () -> assertEquals(
                            19,
                            failedCount,
                            "Other cancellation attempts should be rejected"
                    ),
                    () -> assertEquals(
                            BookingStatus.CANCELLED,
                            booking.getStatus()
                    ),
                    () -> assertEquals(
                            20,
                            event.getAvailableTickets(),
                            "Inventory must be restored exactly once"
                    )
            );

        } finally {
            executor.shutdown();

            assertTrue(
                    executor.awaitTermination(
                            5,
                            TimeUnit.SECONDS
                    ),
                    "Executor should terminate"
            );
        }
    }

    private Event createPublishedEvent(int capacity) {
        ZonedDateTime startsAt =
                NOW.plusSeconds(30L * 24 * 60 * 60)
                        .atZone(
                                ZoneId.of("Europe/Madrid")
                        );

        Event event = new Event(
                "Barcelona Java Meetup",
                EventCategory.TECH,
                startsAt,
                new BigDecimal("20.00"),
                capacity
        );

        event.publish();

        return event;
    }

    private static class NoDiscountPricingPolicy
            implements PricingPolicy {

        @Override
        public PriceQuote calculate(
                Event event,
                int quantity,
                Instant bookingTime
        ) {
            BigDecimal subtotal =
                    event.getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(quantity)
                            );

            return new PriceQuote(
                    event.getUnitPrice(),
                    quantity,
                    subtotal,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    subtotal
            );
        }
    }
}
