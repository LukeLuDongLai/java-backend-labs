package com.lukeludonglai.eventflow.persistence.inMemory;

import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.persistence.BookingPersistence;
import com.lukeludonglai.eventflow.repository.BookingRepository;
import com.lukeludonglai.eventflow.repository.EventRepository;


public class InMemoryBookingPersistence implements BookingPersistence {
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public InMemoryBookingPersistence(
            EventRepository eventRepository,
            BookingRepository bookingRepository
    ) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void saveCreatedBooking(
            Event event,
            Booking booking
    ) {
        eventRepository.save(event);
        bookingRepository.save(booking);
    }

    @Override
    public void saveCancelledBooking(
            Event event,
            Booking booking
    ) {
        eventRepository.save(event);
        bookingRepository.save(booking);
    }
}
