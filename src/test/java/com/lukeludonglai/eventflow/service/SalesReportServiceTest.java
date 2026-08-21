package com.lukeludonglai.eventflow.service;

import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.BookingStatus;
import com.lukeludonglai.eventflow.repository.BookingRepository;
import com.lukeludonglai.eventflow.repository.EventRepository;
import com.lukeludonglai.eventflow.repository.inmemory.InMemoryBookingRepository;
import com.lukeludonglai.eventflow.repository.inmemory.InMemoryEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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

    @Test
    void shouldReturnZeroWhenNoBookingsExist() {
        int totalTicketsSold =
                salesReportService.getTotalTicketsSold();

        assertEquals(0, totalTicketsSold);
    }

    @Test
    void shouldSumConfirmedBookingQuantities() {
        Booking firstBooking = createBooking(2);
        Booking secondBooking = createBooking(5);

        bookingRepository.save(firstBooking);
        bookingRepository.save(secondBooking);

        int totalTicketsSold =
                salesReportService.getTotalTicketsSold();

        assertEquals(7, totalTicketsSold);
    }

    @Test
    void shouldExcludeCancelledBookingsFromTotalTicketsSold() {
        Booking confirmedBooking = createBooking(3);
        Booking cancelledBooking = createBooking(4);

        cancelledBooking.cancel();

        bookingRepository.save(confirmedBooking);
        bookingRepository.save(cancelledBooking);

        int totalTicketsSold =
                salesReportService.getTotalTicketsSold();

        assertEquals(3, totalTicketsSold);
    }

    @Test
    void shouldReturnZeroWhenAllBookingsAreCancelled() {
        Booking firstBooking = createBooking(2);
        Booking secondBooking = createBooking(5);

        firstBooking.cancel();
        secondBooking.cancel();

        bookingRepository.save(firstBooking);
        bookingRepository.save(secondBooking);

        int totalTicketsSold =
                salesReportService.getTotalTicketsSold();

        assertEquals(0, totalTicketsSold);
    }

    private Booking createBooking(int quantity) {
        return new Booking(
                UUID.randomUUID(),
                "customer@example.com",
                quantity,
                new BigDecimal("50.00")
        );
    }
}