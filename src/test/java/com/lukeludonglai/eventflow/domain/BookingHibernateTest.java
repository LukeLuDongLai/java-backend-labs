package com.lukeludonglai.eventflow.domain;

import com.lukeludonglai.eventflow.database.JpaEntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookingHibernateTest {
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
    void shouldPersistAndLoadBooking() {
        UUID eventId = UUID.randomUUID();

        Booking booking = new Booking(
                eventId,
                "customer@example.com",
                3,
                new BigDecimal("42.50")
        );

        UUID bookingId = booking.getId();

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(booking);
            tx.commit();

            em.clear();

            Booking storedBooking =
                    em.find(Booking.class, bookingId);

            assertNotNull(storedBooking);
            assertEquals(
                    bookingId,
                    storedBooking.getId()
            );
            assertEquals(
                    eventId,
                    storedBooking.getEventId()
            );
            assertEquals(
                    "customer@example.com",
                    storedBooking.getCustomerEmail()
            );
            assertEquals(
                    3,
                    storedBooking.getQuantity()
            );
            assertEquals(
                    BookingStatus.CONFIRMED,
                    storedBooking.getStatus()
            );
            assertEquals(
                    0,
                    new BigDecimal("42.50")
                            .compareTo(
                                    storedBooking.getTotalPrice()
                            )
            );

        } finally {
            em.close();
        }
    }
}