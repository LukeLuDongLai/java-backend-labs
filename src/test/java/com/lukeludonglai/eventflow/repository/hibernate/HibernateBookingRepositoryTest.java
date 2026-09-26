package com.lukeludonglai.eventflow.repository.hibernate;

import com.lukeludonglai.eventflow.database.JpaEntityManagerFactory;
import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.BookingStatus;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.helper.TestDataHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HibernateBookingRepositoryTest {
    private static EntityManagerFactory emf;
    private static HibernateBookingRepository repository;

    @BeforeAll
    static void setUp() {
        emf = JpaEntityManagerFactory.create();
        repository =
                new HibernateBookingRepository(emf);
    }

    @AfterAll
    static void tearDown() {
        emf.close();
    }

    @Test
    void shouldSaveAndFindBookingById() {
        Event event = TestDataHelper.createDefaultEvent();
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try{
            transaction.begin();
            em.persist(event);
            transaction.commit();
        }finally {
            em.close();
        }

        Booking booking = new Booking(
                event,
                "customer@example.com",
                2,
                new BigDecimal("40.00")
        );

        repository.save(booking);

        Booking storedBooking = repository
                .findById(booking.getId())
                .orElseThrow();

        assertEquals(
                booking.getId(),
                storedBooking.getId()
        );

        assertEquals(
                "customer@example.com",
                storedBooking.getCustomerEmail()
        );

        assertEquals(
                2,
                storedBooking.getQuantity()
        );

        assertEquals(
                BookingStatus.CONFIRMED,
                storedBooking.getStatus()
        );

        assertEquals(
                0,
                new BigDecimal("40.00")
                        .compareTo(
                                storedBooking.getTotalPrice()
                        )
        );
    }
}