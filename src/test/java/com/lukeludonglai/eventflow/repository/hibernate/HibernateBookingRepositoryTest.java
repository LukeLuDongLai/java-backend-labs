package com.lukeludonglai.eventflow.repository.hibernate;

import com.lukeludonglai.eventflow.database.JpaEntityManagerFactory;
import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.BookingStatus;
import jakarta.persistence.EntityManagerFactory;
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
        Booking booking = new Booking(
                UUID.randomUUID(),
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