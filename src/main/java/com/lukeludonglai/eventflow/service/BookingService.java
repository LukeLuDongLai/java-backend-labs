package com.lukeludonglai.eventflow.service;

import com.lukeludonglai.eventflow.domain.*;
import com.lukeludonglai.eventflow.exception.BookingAlreadyCancelledException;
import com.lukeludonglai.eventflow.exception.BookingNotFoundException;
import com.lukeludonglai.eventflow.exception.EventNotBookableException;
import com.lukeludonglai.eventflow.exception.EventNotFoundException;
import com.lukeludonglai.eventflow.persistence.BookingPersistence;
import com.lukeludonglai.eventflow.pricing.*;
import com.lukeludonglai.eventflow.repository.*;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class BookingService {
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final PricingPolicy pricingPolicy;
    private final Clock clock;
    private final BookingPersistence bookingPersistence;

    public BookingService(
            EventRepository eventRepository,
            BookingRepository bookingRepository,
            PricingPolicy pricingPolicy,
            Clock clock,
            BookingPersistence bookingPersistence
    ){
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.pricingPolicy = pricingPolicy;
        this.clock = clock;
        this.bookingPersistence = bookingPersistence;
    }

    public Booking createBooking(
            UUID eventId,
            String customerEmail,
            int quantity
    ){
        if (eventId == null) {
            throw new IllegalArgumentException(
                    "Event ID must not be null"
            );
        }

        if (quantity <= 0 || quantity >10){
            throw new IllegalArgumentException("Quantity must be between one and ten");
        }

        Event event = eventRepository.findById(eventId).orElseThrow(()-> new EventNotFoundException(eventId));

        if (!event.isPublished()){
            throw new EventNotBookableException(eventId, "Event is not published");
        }

        Instant now = Instant.now(clock);
        if (!event.getStartsAt().toInstant().isAfter(now)) {
            throw new EventNotBookableException(eventId, "Event has already begun");
        }

        PriceQuote quote = pricingPolicy.calculate(event, quantity, now);

        event.reserveTickets(quantity);

        Booking booking = new Booking(eventId, customerEmail, quantity, quote.finalTotal());

        bookingPersistence.saveCreatedBooking(event, booking);

        return booking;
    }

    public Booking cancelBooking(UUID bookingId){
        if (bookingId == null) {
            throw new IllegalArgumentException(
                    "Booking ID must not be null"
            );
        }

        Booking booking = this.bookingRepository.findById(bookingId).orElseThrow(()-> new BookingNotFoundException(bookingId));
        Event event = eventRepository.findById(booking.getEventId()).orElseThrow(()->new EventNotFoundException(booking.getEventId()));

        booking.cancel();
        event.releaseTickets(booking.getQuantity());

        bookingPersistence.saveCancelledBooking(event, booking);

        return booking;
    }
}
