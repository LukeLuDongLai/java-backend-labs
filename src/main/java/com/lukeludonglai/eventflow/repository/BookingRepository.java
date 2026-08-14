package com.lukeludonglai.eventflow.repository;
import com.lukeludonglai.eventflow.domain.Booking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(UUID id);
    List<Booking> findAll();
}
