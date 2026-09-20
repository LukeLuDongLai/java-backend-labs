package com.lukeludonglai.eventflow.concurrency.hibernate;

import com.lukeludonglai.eventflow.database.JpaEntityManagerFactory;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import com.lukeludonglai.eventflow.exception.InsufficientTicketsException;
import com.lukeludonglai.eventflow.persistence.BookingPersistence;
import com.lukeludonglai.eventflow.persistence.hibernate.HibernateBookingPersistence;
import com.lukeludonglai.eventflow.pricing.PriceQuote;
import com.lukeludonglai.eventflow.pricing.PricingPolicy;
import com.lukeludonglai.eventflow.repository.BookingRepository;
import com.lukeludonglai.eventflow.repository.EventRepository;
import com.lukeludonglai.eventflow.repository.hibernate.HibernateBookingRepository;
import com.lukeludonglai.eventflow.repository.hibernate.HibernateEventRepository;
import com.lukeludonglai.eventflow.service.BookingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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

public class HibernateBookingConcurrencyTest {

    private static final Instant NOW =
            Instant.parse("2026-09-20T10:00:00Z");

    private static EntityManagerFactory emf;

    private EventRepository eventRepository;
    private BookingRepository bookingRepository;
    private BookingPersistence bookingPersistence;
    private BookingService bookingService;

    @BeforeAll
    static void startHibernate() {
        emf = JpaEntityManagerFactory.create();
    }

    @AfterAll
    static void stopHibernate() {
        emf.close();
    }

    @BeforeEach
    void setUp() {
        eventRepository =
                new HibernateEventRepository(emf);

        bookingRepository =
                new HibernateBookingRepository(emf);

        bookingPersistence =
                new HibernateBookingPersistence(emf);

        PricingPolicy pricingPolicy =
                new NoDiscountPricingPolicy();

        Clock clock =
                Clock.fixed(NOW, ZoneOffset.UTC);

        bookingService = new BookingService(
                eventRepository,
                bookingRepository,
                pricingPolicy,
                clock,
                bookingPersistence
        );
    }

    @AfterEach
    void cleanUp() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.createQuery("delete from Booking")
                    .executeUpdate();

            em.createQuery("delete from Event")
                    .executeUpdate();

            tx.commit();

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    @Test
    void shouldNotOversellWhenManyUsersBookConcurrently()
            throws Exception {

        int capacity = 5;
        int bookingAttempts = 20;

        Event event = createPublishedEvent(capacity);

        eventRepository.save(event);

        ExecutorService executor =
                Executors.newFixedThreadPool(bookingAttempts);

        CountDownLatch readyGate =
                new CountDownLatch(bookingAttempts);

        CountDownLatch startGate =
                new CountDownLatch(1);

        List<Future<BookingResult>> futures =
                new ArrayList<>();

        try {
            for (int i = 0; i < bookingAttempts; i++) {

                int customerNumber = i;

                Future<BookingResult> future =
                        executor.submit(() -> {

                            readyGate.countDown();

                            startGate.await();

                            try {
                                bookingService.createBooking(
                                        event.getId(),
                                        "customer"
                                                + customerNumber
                                                + "@example.com",
                                        1
                                );

                                return BookingResult.SUCCESS;

                            } catch (InsufficientTicketsException e) {

                                return BookingResult.SOLD_OUT;

                            } catch (RuntimeException e) {

                                if (isOptimisticLockFailure(e)) {
                                    return BookingResult.CONFLICT;
                                }

                                throw e;
                            }
                        });

                futures.add(future);
            }

            assertTrue(
                    readyGate.await(
                            10,
                            TimeUnit.SECONDS
                    ),
                    "All booking threads should become ready"
            );

            startGate.countDown();

            int successfulBookings = 0;
            int soldOutBookings = 0;
            int conflicts = 0;

            for (Future<BookingResult> future : futures) {

                BookingResult result = future.get();

                switch (result) {
                    case SUCCESS -> successfulBookings++;
                    case SOLD_OUT -> soldOutBookings++;
                    case CONFLICT -> conflicts++;
                }
            }

            Event storedEvent = eventRepository
                    .findById(event.getId())
                    .orElseThrow();

            int storedBookings =
                    bookingRepository.findAll().size();

            int successfulCount = successfulBookings;
            int soldOutCount = soldOutBookings;
            int conflictCount = conflicts;

            assertAll(
                    () -> assertTrue(
                            successfulCount <= capacity,
                            "Successful bookings must never exceed capacity"
                    ),

                    () -> assertTrue(
                            storedEvent.getAvailableTickets() >= 0,
                            "Inventory must never become negative"
                    ),

                    () -> assertEquals(
                            capacity - successfulCount,
                            storedEvent.getAvailableTickets(),
                            "Inventory must match successful bookings"
                    ),

                    () -> assertEquals(
                            successfulCount,
                            storedBookings,
                            "Only successful bookings should be stored"
                    ),

                    () -> assertEquals(
                            bookingAttempts,
                            successfulCount
                                    + soldOutCount
                                    + conflictCount,
                            "Every attempt should have one result"
                    )
            );

            System.out.println(
                    "Success: " + successfulCount
                            + ", Sold out: " + soldOutCount
                            + ", Conflicts: " + conflictCount
                            + ", Remaining: "
                            + storedEvent.getAvailableTickets()
            );

        } finally {
            executor.shutdown();

            assertTrue(
                    executor.awaitTermination(
                            10,
                            TimeUnit.SECONDS
                    ),
                    "Executor should terminate"
            );
        }
    }

    private boolean isOptimisticLockFailure(
            Throwable throwable
    ) {
        Throwable current = throwable;

        while (current != null) {

            if (current instanceof OptimisticLockException) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    private Event createPublishedEvent(int capacity) {
        ZonedDateTime startsAt =
                NOW.plusSeconds(30L * 24 * 60 * 60)
                        .atZone(
                                ZoneId.of("Europe/Madrid")
                        );

        Event event = new Event(
                "Hibernate Concurrency Test Event",
                EventCategory.TECH,
                startsAt,
                new BigDecimal("20.00"),
                capacity
        );

        event.publish();

        return event;
    }

    private enum BookingResult {
        SUCCESS,
        SOLD_OUT,
        CONFLICT
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
