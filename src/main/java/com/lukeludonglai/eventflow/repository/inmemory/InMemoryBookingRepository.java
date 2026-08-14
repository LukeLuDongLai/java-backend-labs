package com.lukeludonglai.eventflow.repository.inmemory;

import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.repository.BookingRepository;

import java.util.*;

public class InMemoryBookingRepository implements BookingRepository {
    private final Map<UUID,Booking> bookings = new HashMap<>();

    @Override
    public Booking save(Booking booking) {
        bookings.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(UUID id) {
        return Optional.ofNullable(bookings.get(id));
    }

    @Override
    public List<Booking> findAll() {
        return new ArrayList<>(bookings.values());
    }
}
