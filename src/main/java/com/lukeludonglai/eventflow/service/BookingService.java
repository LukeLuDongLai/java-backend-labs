package com.lukeludonglai.eventflow.service;

import com.lukeludonglai.eventflow.domain.*;
import com.lukeludonglai.eventflow.exception.BookingAlreadyCancelledException;
import com.lukeludonglai.eventflow.exception.BookingNotFoundException;
import com.lukeludonglai.eventflow.exception.EventNotBookableException;
import com.lukeludonglai.eventflow.exception.EventNotFoundException;
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

    public BookingService(
            EventRepository eventRepository,
            BookingRepository bookingRepository,
            PricingPolicy pricingPolicy,
            Clock clock
    ){
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.pricingPolicy = pricingPolicy;
        this.clock = clock;
    }

    public Booking createBooking(
            UUID eventId,
            String customerEmail,
            int quantity
    ){
        if (quantity <= 0 || quantity >10){
            throw new IllegalArgumentException("Quantity must be between one and ten");
        }

        Event event = eventRepository.findById(eventId).orElseThrow(()-> new EventNotFoundException(eventId));

        if (!event.isPublished()){
            throw new EventNotBookableException(eventId, "Event is not published");
        }

        Instant now = Instant.now(clock);
        if (event.getStartsAt().toInstant().isBefore(now)) {
            throw new EventNotBookableException(eventId, "Event has already begun");
        }

        PriceQuote quote = pricingPolicy.calculate(event, quantity, now);

        event.reserveTickets(quantity);

        Booking booking = new Booking(eventId, customerEmail, quantity, quote.finalTotal());

        bookingRepository.save(booking);
        eventRepository.save(event);

        return booking;
    }

    public Booking cancelBooking(UUID bookingId){
        Booking booking = this.bookingRepository.findById(bookingId).orElseThrow(()-> new BookingNotFoundException(bookingId));
        booking.cancel();
        Event event = eventRepository.findById(booking.getEventId()).orElseThrow(()->new EventNotFoundException(booking.getEventId()));
        event.releaseTickets(booking.getQuantity());
        eventRepository.save(event);
        bookingRepository.save(booking);

        return booking;
    }
}
