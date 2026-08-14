package com.lukeludonglai.eventflow.repository.inmemory;

import static org.junit.jupiter.api.Assertions.*;

import com.lukeludonglai.eventflow.domain.Booking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class InMemoryBookingRepositoryTest {
    private InMemoryBookingRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBookingRepository();
    }

    @Test
    void shouldReturnSavedBooking() {
        Booking booking = createBooking();

        Booking savedBooking = repository.save(booking);

        assertSame(booking, savedBooking);
    }

    @Test
    void shouldFindBookingByIdAfterSaving() {
        Booking booking = createBooking();
        repository.save(booking);

        Optional<Booking> result = repository.findById(booking.getId());

        assertTrue(result.isPresent());
        assertSame(booking, result.get());
    }

    @Test
    void shouldReturnEmptyWhenBookingDoesNotExist() {
        UUID unknownId = UUID.randomUUID();

        Optional<Booking> result = repository.findById(unknownId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnAllSavedBookings() {
        Booking firstBooking = createBooking();
        Booking secondBooking = createBooking();

        repository.save(firstBooking);
        repository.save(secondBooking);

        List<Booking> result = repository.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(firstBooking));
        assertTrue(result.contains(secondBooking));
    }

    @Test
    void shouldReturnEmptyListWhenNoBookingsAreSaved() {
        List<Booking> result = repository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotExposeInternalCollection() {
        Booking booking = createBooking();
        repository.save(booking);

        List<Booking> result = repository.findAll();

        result.clear();

        assertEquals(1, repository.findAll().size());
        assertTrue(repository.findById(booking.getId()).isPresent());
    }

    private Booking createBooking() {
        return new Booking(
                UUID.randomUUID(),
                "customer@example.com",
                2,
                new BigDecimal("40.00")
        );
    }
}