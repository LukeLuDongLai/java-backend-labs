package com.lukeludonglai.eventflow.persistence.hibernate;

import com.lukeludonglai.eventflow.database.JpaEntityManagerFactory;
import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.BookingStatus;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import com.lukeludonglai.eventflow.repository.hibernate.HibernateBookingRepository;
import com.lukeludonglai.eventflow.repository.hibernate.HibernateEventRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.lukeludonglai.eventflow.helper.TestDataHelper.createPublishedEvent;
import static org.junit.jupiter.api.Assertions.*;

class HibernateBookingPersistenceTest {
    private static EntityManagerFactory emf;

    private HibernateEventRepository eventRepository;
    private HibernateBookingRepository bookingRepository;
    private HibernateBookingPersistence bookingPersistence;

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
    }

    @AfterEach
    void cleanUp() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Delete child rows first.
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
    void shouldPersistCreatedBookingAndUpdatedInventory() {
        Event event = createPublishedEvent(10);

        eventRepository.save(event);

        event.reserveTickets(3);

        Booking booking = new Booking(
                event,
                "customer@example.com",
                3,
                new BigDecimal("60.00")
        );

        bookingPersistence.saveCreatedBooking(
                event,
                booking
        );

        Event storedEvent = eventRepository
                .findById(event.getId())
                .orElseThrow();

        Booking storedBooking = bookingRepository
                .findById(booking.getId())
                .orElseThrow();

        assertAll(
                () -> assertEquals(
                        7,
                        storedEvent.getAvailableTickets()
                ),
                () -> assertEquals(
                        booking.getId(),
                        storedBooking.getId()
                ),
                () -> assertEquals(
                        BookingStatus.CONFIRMED,
                        storedBooking.getStatus()
                )
        );
    }

    @Test
    void shouldPersistCancelledBookingAndRestoreInventory() {
        Event event = createPublishedEvent(10);

        eventRepository.save(event);

        event.reserveTickets(3);

        Booking booking = new Booking(
                event,
                "customer@example.com",
                3,
                new BigDecimal("60.00")
        );

        bookingPersistence.saveCreatedBooking(
                event,
                booking
        );

        // Reload them so this is closer to the real application flow.
        Event eventToCancel = eventRepository
                .findById(event.getId())
                .orElseThrow();

        Booking bookingToCancel = bookingRepository
                .findById(booking.getId())
                .orElseThrow();

        bookingToCancel.cancel();

        eventToCancel.releaseTickets(
                bookingToCancel.getQuantity()
        );

        bookingPersistence.saveCancelledBooking(
                eventToCancel,
                bookingToCancel
        );

        Event storedEvent = eventRepository
                .findById(event.getId())
                .orElseThrow();

        Booking storedBooking = bookingRepository
                .findById(booking.getId())
                .orElseThrow();

        assertAll(
                () -> assertEquals(
                        10,
                        storedEvent.getAvailableTickets()
                ),
                () -> assertEquals(
                        BookingStatus.CANCELLED,
                        storedBooking.getStatus()
                )
        );
    }

    @Test
    void shouldRollbackWhenCreatedBookingPersistenceFails() {
        Event event = createPublishedEvent(10);

        eventRepository.save(event);

        event.reserveTickets(1);

        Booking booking = new Booking(
                event,
                "customer@example.com",
                1,
                new BigDecimal("20.00")
        );

        bookingPersistence.saveCreatedBooking(
                event,
                booking
        );

        // Database inventory is now 9.
        //
        // Modify the detached Event again.
        event.reserveTickets(1);

        // Reusing the same Booking causes the second persistence
        // attempt to fail because the Booking already exists.
        assertThrows(
                RuntimeException.class,
                () -> bookingPersistence.saveCreatedBooking(
                        event,
                        booking
                )
        );

        Event storedEvent = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertEquals(
                9,
                storedEvent.getAvailableTickets(),
                "Failed transaction must not leave partial changes"
        );
    }

}